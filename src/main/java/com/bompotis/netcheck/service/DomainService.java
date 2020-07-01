package com.bompotis.netcheck.service;

import com.bompotis.netcheck.data.entity.*;
import com.bompotis.netcheck.data.repository.DomainCheckRepository;
import com.bompotis.netcheck.data.repository.DomainMetricRepository;
import com.bompotis.netcheck.data.repository.DomainRepository;
import com.bompotis.netcheck.service.dto.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by Kyriakos Bompotis on 30/11/18.
 */
@Service
public class DomainService {

    private final DomainRepository domainRepository;

    private final DomainCheckRepository domainCheckRepository;

    private final DomainMetricRepository domainMetricRepository;

    @Autowired
    public DomainService(DomainRepository domainRepository,
                         DomainCheckRepository domainCheckRepository,
                         DomainMetricRepository domainMetricRepository) {
        this.domainRepository = domainRepository;
        this.domainCheckRepository = domainCheckRepository;
        this.domainMetricRepository = domainMetricRepository;
    }

    public DomainCheckDto check(String domain) throws IOException, NoSuchAlgorithmException, KeyManagementException {
        var monitored = domainRepository.findById(domain).isPresent();
        return new DomainCheckDto.Builder(domain)
                .monitored(monitored)
                .withCurrentHttpCheck()
                .withCurrentHttpsCheck()
                .build();
    }

    public DomainCheckEntity convertToDomainCheckEntity(DomainCheckDto domainCheckDto, DomainEntity domainEntity) {
        final var newCertificates = new HashSet<CertificateEntity>();
        final var domainCheckEntityBuilder = new DomainCheckEntity.Builder()
                .domain(domainCheckDto.getDomain())
                .httpResponseTimeNs(domainCheckDto.getHttpCheckDto().getResponseTimeNs())
                .httpIpAddress(domainCheckDto.getHttpCheckDto().getIpAddress())
                .timeCheckedOn(domainCheckDto.getHttpCheckDto().getTimeCheckedOn())
                .httpsResponseTimeNs(domainCheckDto.getHttpsCheckDto().getHttpCheckDto().getResponseTimeNs())
                .httpsIpAddress(domainCheckDto.getHttpsCheckDto().getHttpCheckDto().getIpAddress())
                .domainEntity(domainEntity);

        final var newProtocolChecks = Set.of(
                domainCheckDto.getHttpCheckDto().toProtocolCheckEntity(),
                domainCheckDto.getHttpsCheckDto().getHttpCheckDto().toProtocolCheckEntity()
        );

        if(Optional.ofNullable(domainCheckDto.getHttpsCheckDto().getIssuerCertificate()).isPresent()) {
            newCertificates.add(domainCheckDto.getHttpsCheckDto().getIssuerCertificate().toCertificateEntity());
        }
        if(Optional.ofNullable(domainCheckDto.getHttpsCheckDto().getCaCertificates()).isPresent()) {
            domainCheckDto.getHttpsCheckDto().getCaCertificates().forEach((cert) -> newCertificates.add(cert.toCertificateEntity()));
        }

        final var previousDomainCheck = domainCheckRepository
                .findFirstByDomainOrderByTimeCheckedOnDesc(domainCheckDto.getDomain());

        if (previousDomainCheck.isEmpty()) {
            return domainCheckEntityBuilder
                    .httpCheckChange(true)
                    .httpsCheckChange(true)
                    .certificatesChange(true)
                    .protocolCheckEntities(newProtocolChecks)
                    .certificateEntities(newCertificates)
                    .build();
        }
        return assignOlderProtocolsAndCertificates(
                domainCheckEntityBuilder,
                previousDomainCheck.get().getProtocolCheckEntities(),
                previousDomainCheck.get().getCertificateEntities(),
                newProtocolChecks,
                newCertificates
        );
    }

    private DomainCheckEntity assignOlderProtocolsAndCertificates(DomainCheckEntity.Builder domainCheckEntityBuilder,
                                                                  Set<ProtocolCheckEntity> oldProtocolChecks,
                                                                  Set<CertificateEntity> oldCertificates,
                                                                  Set<ProtocolCheckEntity> newProtocolChecks,
                                                                  HashSet<CertificateEntity> newCertificates) {
        if (oldProtocolChecks.isEmpty()) {
            domainCheckEntityBuilder.protocolCheckEntities(newProtocolChecks).httpsCheckChange(true).httpCheckChange(true);
        } else if (oldProtocolChecks.equals(newProtocolChecks)) {
            domainCheckEntityBuilder.protocolCheckEntities(oldProtocolChecks).httpsCheckChange(false).httpCheckChange(false);
        } else {
            final var finalProtocolsSet = new HashSet<ProtocolCheckEntity>();
            final var oldProtocolCheckMap = oldProtocolChecks
                    .stream()
                    .collect(Collectors.toMap(ProtocolCheckEntity::getProtocol, oldProtocolCheck -> oldProtocolCheck, (a, b) -> b, HashMap::new));
            for (var newProtocolCheck : newProtocolChecks) {
                final var protocol = newProtocolCheck.getProtocol();
                if (newProtocolCheck.equals(oldProtocolCheckMap.get(protocol))) {
                    if (protocol.equals(ProtocolCheckEntity.Protocol.HTTP)) {
                        domainCheckEntityBuilder.httpCheckChange(false);
                    }
                    if (protocol.equals(ProtocolCheckEntity.Protocol.HTTPS)) {
                        domainCheckEntityBuilder.httpsCheckChange(false);
                    }
                    finalProtocolsSet.add(oldProtocolCheckMap.get(protocol));
                } else {
                    if (protocol.equals(ProtocolCheckEntity.Protocol.HTTP)) {
                        domainCheckEntityBuilder.httpCheckChange(true);
                    }
                    if (protocol.equals(ProtocolCheckEntity.Protocol.HTTPS)) {
                        domainCheckEntityBuilder.httpsCheckChange(true);
                    }
                    finalProtocolsSet.add(newProtocolCheck);
                }
            }
            domainCheckEntityBuilder.protocolCheckEntities(finalProtocolsSet);
        }

        if (oldCertificates.isEmpty()) {
            domainCheckEntityBuilder.certificateEntities(newCertificates).certificatesChange(true);
        } else if (oldCertificates.equals(newCertificates)) {
            domainCheckEntityBuilder.certificateEntities(oldCertificates).certificatesChange(false);
        } else {
            domainCheckEntityBuilder.certificatesChange(true);
            final var finalCertificateSet = new HashSet<CertificateEntity>();
            final var oldCertificateMap = oldCertificates
                    .stream()
                    .collect(Collectors.toMap(CertificateEntity::getBasicConstraints, oldCertificate -> oldCertificate, (a, b) -> b, HashMap::new));
            for (var newCertificate : newCertificates) {
                final var basicConstraint = newCertificate.getBasicConstraints();
                if (newCertificate.equals(oldCertificateMap.get(basicConstraint))) {
                    finalCertificateSet.add(oldCertificateMap.get(basicConstraint));
                } else {
                    finalCertificateSet.add(newCertificate);
                }
            }
            domainCheckEntityBuilder.certificateEntities(finalCertificateSet);
        }

        return domainCheckEntityBuilder.build();
    }

    public PaginatedDto<DomainCheckDto> getDomainHistory(String domain, Integer page, Integer size) {
        ArrayList<DomainCheckDto> domainCheckList = new ArrayList<>();
        final var domainCheckEntities = domainCheckRepository.findAllByDomain(domain, getDefaultPageRequest(page, size));
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
        var domainCheckEntities = domainCheckRepository.findAllByDomain(domain, getDefaultPageRequest(page, size));
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
        var domainCheckEntities = domainCheckRepository.findAllByDomain(domain, getDefaultPageRequest(page, size));
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
        var paginatedQueryResult = domainCheckRepository.findAllLastChecksPerDomain(getDefaultPageRequest(page, size));
        paginatedQueryResult.forEach(
                (domain) -> domains.add(new DomainDto.Builder()
                        .domain(domain.getDomain())
                        .checkFrequencyMinutes(domain.getDomainEntity().getCheckFrequency())
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

    public void scheduleDomainToCheck(String domain, Integer frequency) {
        domainRepository.save(new DomainEntity.Builder()
                .frequency(Optional.ofNullable(frequency).orElse(10))
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
                        domainCheckEntity.getTimeCheckedOn(),
                        domainCheckEntity.getHttpIpAddress()
                ));
            }
            if (protocolCheck.getProtocol().equals(ProtocolCheckEntity.Protocol.HTTPS)) {
                var httpsCheckDtoBuilder = new HttpsCheckDto.Builder();
                httpsCheckDtoBuilder.httpCheckDto(new HttpCheckDto(
                        protocolCheck,
                        domainCheckEntity.getHttpsResponseTimeNs(),
                        domainCheckEntity.getTimeCheckedOn(),
                        domainCheckEntity.getHttpsIpAddress()
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

    private PageRequest getDefaultPageRequest(Integer page, Integer size) {
        return PageRequest.of(
                Optional.ofNullable(page).orElse(0),
                Optional.ofNullable(size).orElse(10),
                Sort.by("createdAt").descending()
        );
    }

    public PaginatedDto<StateDto> getDomainStates(String domain, String protocol, Integer page, Integer size) {
        final ArrayList<StateDto> stateList = new ArrayList<>();
        final var domainCheckEntities = domainCheckRepository.findAllStateChanges(domain, getDefaultPageRequest(page, size));
        for (var domainCheckEntity : domainCheckEntities) {
            var protocolToCollect = Optional.ofNullable(protocol).orElse("HTTPS");
            var check = domainCheckEntity
                    .getProtocolCheckEntities()
                    .stream()
                    .collect(Collectors.toMap(protocolCheck -> protocolCheck.getProtocol().toString(), protocolCheck -> protocolCheck, (a, b) -> b))
                    .get(protocolToCollect.toUpperCase());
            stateList.add(new StateDto.Builder()
                    .hostname(check.getHostname())
                    .dnsResolves(check.getDnsResolves())
                    .redirectUri(check.getRedirectUri())
                    .id(check.getId())
                    .statusCode(check.getStatusCode())
                    .protocol(check.getProtocol().toString())
                    .timeCheckedOn(domainCheckEntity.getTimeCheckedOn())
                    .certificatesChange(domainCheckEntity.isCertificatesChange())
                    .httpCheckChange(domainCheckEntity.isHttpCheckChange())
                    .httpsCheckChange(domainCheckEntity.isHttpsCheckChange())
                    .build()
            );
        }
        return new PaginatedDto<>(
                stateList,
                domainCheckEntities.getTotalElements(),
                domainCheckEntities.getTotalPages(),
                domainCheckEntities.getNumber(),
                domainCheckEntities.getNumberOfElements()
        );
    }

    public PaginatedDto<MetricDto> getDomainMetrics(String domain, String period, String protocol, Integer page, Integer size) {
        var httpMetrics = new ArrayList<MetricDto>();
        var domainMetricEntities =
                domainMetricRepository.findAllByDomainAndProtocolAndPeriodType(
                        domain,
                        DomainMetricEntity.Protocol.valueOf(protocol),
                        DomainMetricEntity.Period.valueOf(period),
                        getDefaultPageRequest(page, size)
                );

        for (var domainMetricEntity : domainMetricEntities) {
            var httpMetric = new MetricDto.Builder()
                    .maxResponseTime(domainMetricEntity.getMaxResponseTimeNs())
                    .totalChecks(domainMetricEntity.getTotalChecks())
                    .successfulChecks(domainMetricEntity.getSuccessfulChecks())
                    .averageResponseTime(domainMetricEntity.getAvgResponseTimeNs())
                    .minResponseTime(domainMetricEntity.getMinResponseTimeNs())
                    .metricPeriodStart(domainMetricEntity.getStartPeriod())
                    .metricPeriodEnd(domainMetricEntity.getEndPeriod())
                    .protocol(protocol)
                    .build();
            httpMetrics.add(httpMetric);
        }
        return new PaginatedDto<>(
                httpMetrics,
                domainMetricEntities.getTotalElements(),
                domainMetricEntities.getTotalPages(),
                domainMetricEntities.getNumber(),
                domainMetricEntities.getNumberOfElements()
        );
    }
}
