package com.bompotis.netcheck.service;

import com.bompotis.netcheck.data.entity.CertificateEntity;
import com.bompotis.netcheck.data.entity.DomainCheckEntity;
import com.bompotis.netcheck.data.entity.DomainEntity;
import com.bompotis.netcheck.data.entity.ProtocolCheckEntity;
import com.bompotis.netcheck.data.repository.DomainCheckRepository;
import com.bompotis.netcheck.data.repository.DomainRepository;
import com.bompotis.netcheck.service.dto.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

/**
 * Created by Kyriakos Bompotis on 30/11/18.
 */
@Service
public class DomainService {

    private final DomainRepository domainRepository;

    private final DomainCheckRepository domainCheckRepository;

    @Autowired
    public DomainService(DomainRepository domainRepository, DomainCheckRepository domainCheckRepository) {
        this.domainRepository = domainRepository;
        this.domainCheckRepository = domainCheckRepository;
    }

    public DomainCheckDto check(String domain) throws IOException {
        return new DomainCheckDto.Builder(domain)
                .withCurrentHttpCheck()
                .withCurrentHttpsCheck()
                .build();
    }


    public void storeResult(DomainCheckDto domainCheckDto) {
        var domainCheckEntityBuilder = convertToDomainCheckEntity(domainCheckDto);
        var domainEntityOptional = domainRepository.findById(domainCheckDto.getDomain());
        domainEntityOptional.ifPresentOrElse(
                domainCheckEntityBuilder::domainEntity,
                () -> domainCheckEntityBuilder.domainEntity(new DomainEntity.Builder().domain(domainCheckDto.getDomain()).build())
        );
        domainCheckRepository.save(domainCheckEntityBuilder.build());
    }

    public DomainCheckEntity.Builder convertToDomainCheckEntity(DomainCheckDto domainCheckDto) {
        var domainCheckEntityBuilder = new DomainCheckEntity.Builder()
                .domain(domainCheckDto.getDomain())
                .httpResponseTimeNs(domainCheckDto.getHttpCheckDto().getResponseTimeNs())
                .timeCheckedOn(domainCheckDto.getHttpCheckDto().getTimeCheckedOn())
                .httpsResponseTimeNs(domainCheckDto.getHttpsCheckDto().getHttpCheckDto().getResponseTimeNs());

        domainCheckEntityBuilder.protocolCheckEntities(Set.of(
                domainCheckDto.getHttpCheckDto().toProtocolCheckEntity(),
                domainCheckDto.getHttpsCheckDto().getHttpCheckDto().toProtocolCheckEntity()
        ));


        var certificateEntities = new HashSet<CertificateEntity>();
        if(Optional.ofNullable(domainCheckDto.getHttpsCheckDto().getIssuerCertificate()).isPresent()) {
            certificateEntities.add(domainCheckDto.getHttpsCheckDto().getIssuerCertificate().toCertificateEntity());
        }
        if(Optional.ofNullable(domainCheckDto.getHttpsCheckDto().getCaCertificates()).isPresent()) {
            domainCheckDto.getHttpsCheckDto().getCaCertificates().forEach((cert) -> certificateEntities.add(cert.toCertificateEntity()));
        }
        domainCheckEntityBuilder.certificateEntities(certificateEntities);

        return domainCheckEntityBuilder;
    }

    public PaginatedDto<DomainCheckDto> getDomainHistory(String domain, Integer page, Integer size) {
        var domainCheckList = new ArrayList<DomainCheckDto>();
        var pageRequest = PageRequest.of(
                Optional.ofNullable(page).orElse(0),
                Optional.ofNullable(size).orElse(10),
                Sort.by("createdAt").descending()
        );
        var domainCheckEntities = domainCheckRepository.findAllByDomain(domain, pageRequest);
        for (var domainCheckEntity : domainCheckEntities) {
            domainCheckList.add(convertDomainCheckEntityToDto(domainCheckEntity));
        }
        return new PaginatedDto<>(
                domainCheckList,
                domainCheckEntities.getTotalElements(),
                domainCheckEntities.getTotalPages(),
                domainCheckEntities.getNumber(),
                domainCheckEntities.getNumberOfElements()
        );
    }

    public PaginatedDto<HttpCheckDto> getHttpDomainHistory(String domain, Integer page, Integer size) {
        var httpCheckDtos = new ArrayList<HttpCheckDto>();
        var pageRequest = PageRequest.of(
                Optional.ofNullable(page).orElse(0),
                Optional.ofNullable(size).orElse(10),
                Sort.by("createdAt").descending()
        );
        var domainCheckEntities = domainCheckRepository.findAllByDomain(domain, pageRequest);
        for (var domainCheckEntity : domainCheckEntities) {
            httpCheckDtos.add(convertDomainCheckEntityToDto(domainCheckEntity).getHttpCheckDto());
        }
        return new PaginatedDto<>(
                httpCheckDtos,
                domainCheckEntities.getTotalElements(),
                domainCheckEntities.getTotalPages(),
                domainCheckEntities.getNumber(),
                domainCheckEntities.getNumberOfElements()
        );
    }

    public PaginatedDto<HttpCheckDto> getHttpsDomainHistory(String domain, Integer page, Integer size) {
        var httpCheckDtos = new ArrayList<HttpCheckDto>();
        var pageRequest = PageRequest.of(
                Optional.ofNullable(page).orElse(0),
                Optional.ofNullable(size).orElse(10),
                Sort.by("createdAt").descending()
        );
        var domainCheckEntities = domainCheckRepository.findAllByDomain(domain, pageRequest);
        for (var domainCheckEntity : domainCheckEntities) {
            httpCheckDtos.add(convertDomainCheckEntityToDto(domainCheckEntity).getHttpsCheckDto().getHttpCheckDto());
        }
        return new PaginatedDto<>(
                httpCheckDtos,
                domainCheckEntities.getTotalElements(),
                domainCheckEntities.getTotalPages(),
                domainCheckEntities.getNumber(),
                domainCheckEntities.getNumberOfElements()
        );
    }

    public PaginatedMetricsDto getDomainMetrics(String domain, Integer page, Integer size) {
        var httpMetrics = new ArrayList<MetricDto>();
        var httpsMetrics = new ArrayList<MetricDto>();
        var pageRequest = PageRequest.of(
                Optional.ofNullable(page).orElse(0),
                Optional.ofNullable(size).orElse(10),
                Sort.by("createdAt").descending()
        );
        var domainCheckEntities = domainCheckRepository.findAllByDomain(domain, pageRequest);
        for (var domainCheckEntity : domainCheckEntities) {
            var httpMetricBuilder = new MetricDto.Builder()
                    .metricPeriod(domainCheckEntity.getTimeCheckedOn())
                    .averageResponseTime(domainCheckEntity.getHttpResponseTimeNs());
            var httpsMetricBuilder = new MetricDto.Builder()
                    .metricPeriod(domainCheckEntity.getTimeCheckedOn())
                    .averageResponseTime(domainCheckEntity.getHttpsResponseTimeNs());
            for (var protocolCheck : domainCheckEntity.getProtocolCheckEntities()) {
                var uptimePercentage = Optional.ofNullable(protocolCheck.getStatusCode()).isPresent() &&
                        (protocolCheck.getStatusCode() < 400) ? 100 : 0;
                if (protocolCheck.getProtocol().equals(ProtocolCheckEntity.Protocol.HTTP)) {
                    httpMetricBuilder.uptimePercentage(uptimePercentage);
                }
                if (protocolCheck.getProtocol().equals(ProtocolCheckEntity.Protocol.HTTPS)) {
                    httpsMetricBuilder.uptimePercentage(uptimePercentage);
                }
            }
            httpMetrics.add(httpMetricBuilder.build());
            httpsMetrics.add(httpsMetricBuilder.build());
        }
        return new PaginatedMetricsDto(
                httpMetrics,
                httpsMetrics,
                domainCheckEntities.getTotalElements(),
                domainCheckEntities.getTotalPages(),
                domainCheckEntities.getNumber(),
                domainCheckEntities.getNumberOfElements()
        );
    }


    public Optional<DomainCheckDto> getDomainCheck(String domain, String id) {
        var queryResult = domainCheckRepository.findByIdAndDomain(id,domain);
        if (queryResult.isEmpty()) {
            return Optional.empty();
        }
        var domainCheckEntity = queryResult.get();
        return Optional.of(convertDomainCheckEntityToDto(domainCheckEntity));
    }

    public PaginatedDto<DomainDto> getPaginatedDomains(Integer page, Integer size) {
        var domains = new ArrayList<DomainDto>();
        var paginatedQueryResult = domainCheckRepository.findAllLastChecksPerDomain(
                PageRequest.of(
                        Optional.ofNullable(page).orElse(0),
                        Optional.ofNullable(size).orElse(10),
                        Sort.by("createdAt").descending()
                )
        );
        paginatedQueryResult.forEach(
                (domain) -> domains.add(new DomainDto.Builder()
                        .domain(domain.getDomain())
                        .createdAt(domain.getDomainEntity().getCreatedAt())
                        .lastDomainCheck(convertDomainCheckEntityToDto(domain))
                        .build()
                )
        );

        return new PaginatedDto<>(
                domains,
                paginatedQueryResult.getTotalElements(),
                paginatedQueryResult.getTotalPages(),
                paginatedQueryResult.getNumber(),
                paginatedQueryResult.getNumberOfElements()
        );
    }

    public void scheduleDomainToCheck(String domain) {
        domainRepository.save(new DomainEntity.Builder()
                .domain(domain)
                .build()
        );
    }

    public DomainCheckDto convertDomainCheckEntityToDto(DomainCheckEntity domainCheckEntity) {
        var domainCheckDtoBuilder = new DomainCheckDto.Builder(domainCheckEntity.getDomain());

        for (var protocolCheck: domainCheckEntity.getProtocolCheckEntities()) {
            if (protocolCheck.getProtocol().equals(ProtocolCheckEntity.Protocol.HTTP)) {
                domainCheckDtoBuilder.httpCheck(new HttpCheckDto(
                        protocolCheck,
                        domainCheckEntity.getHttpResponseTimeNs(),
                        domainCheckEntity.getTimeCheckedOn()
                ));
            }
            if (protocolCheck.getProtocol().equals(ProtocolCheckEntity.Protocol.HTTPS)) {
                var httpsCheckDtoBuilder = new HttpsCheckDto.Builder();
                httpsCheckDtoBuilder.httpCheckDto(new HttpCheckDto(
                        protocolCheck,
                        domainCheckEntity.getHttpsResponseTimeNs(),
                        domainCheckEntity.getTimeCheckedOn()
                ));

                domainCheckEntity.getCertificateEntities()
                        .stream()
                        .map(CertificateDetailsDto::new)
                        .forEach(httpsCheckDtoBuilder::certificate);

                domainCheckDtoBuilder.httpsCheckDto(httpsCheckDtoBuilder.build());
            }
        }
        return domainCheckDtoBuilder
                .id(domainCheckEntity.getId())
                .build();
    }

}
