/*
 * Copyright 2020 the original author or authors.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This code is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package com.bompotis.netcheck.service;

import com.bompotis.netcheck.api.exception.EntityNotFoundException;
import com.bompotis.netcheck.data.entity.CertificateEntity;
import com.bompotis.netcheck.data.entity.DomainCheckEntity;
import com.bompotis.netcheck.data.entity.DomainEntity;
import com.bompotis.netcheck.data.entity.ProtocolCheckEntity;
import com.bompotis.netcheck.data.repository.DomainCheckRepository;
import com.bompotis.netcheck.data.repository.DomainRepository;
import com.bompotis.netcheck.service.dto.CertificateDetailsDto;
import com.bompotis.netcheck.service.dto.DomainCheckConfigDto;
import com.bompotis.netcheck.service.dto.DomainCheckDto;
import com.bompotis.netcheck.service.dto.DomainDto;
import com.bompotis.netcheck.service.dto.DomainUpdateDto;
import com.bompotis.netcheck.service.dto.DomainsOptionsDto;
import com.bompotis.netcheck.service.dto.HttpCheckDto;
import com.bompotis.netcheck.service.dto.HttpsCheckDto;
import com.bompotis.netcheck.service.dto.PaginatedDto;
import com.bompotis.netcheck.service.dto.StateDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Created by Kyriakos Bompotis on 30/11/18.
 */
@Service
public class DomainService extends AbstractService{

    private final DomainRepository domainRepository;

    private final DomainCheckRepository domainCheckRepository;

    @Autowired
    public DomainService(DomainRepository domainRepository,
                         DomainCheckRepository domainCheckRepository) {
        this.domainRepository = domainRepository;
        this.domainCheckRepository = domainCheckRepository;
    }

    public DomainCheckDto check(DomainDto domainDto) throws IOException, NoSuchAlgorithmException, KeyManagementException {
        var monitored = domainRepository.findById(domainDto.getDomain()).isPresent();
        var config = new DomainCheckConfigDto.Builder()
                .domain(domainDto.getDomain())
                .endpoint(domainDto.getEndpoint())
                .timeoutMs(domainDto.getTimeoutMs())
                .withHeaders(domainDto.getHeaders())
                .build();
        return new DomainCheckDto.Builder(domainDto.getDomain())
                .monitored(monitored)
                .withCurrentHttpCheck(config, domainDto.getHttpPort())
                .withCurrentHttpsCheck(config, domainDto.getHttpsPort())
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
            domainCheckEntityBuilder.protocolCheckEntities(newProtocolChecks);
        } else if (oldProtocolChecks.equals(newProtocolChecks)) {
            domainCheckEntityBuilder
                    .protocolCheckEntities(oldProtocolChecks)
                    .previousProtocolCheckEntities(oldProtocolChecks);
        } else {
            final var finalProtocolsSet = new HashSet<ProtocolCheckEntity>();
            final var oldProtocolCheckMap = oldProtocolChecks
                    .stream()
                    .collect(Collectors.toMap(ProtocolCheckEntity::getProtocol, oldProtocolCheck -> oldProtocolCheck, (a, b) -> b, HashMap::new));
            for (var newProtocolCheck : newProtocolChecks) {
                final var protocol = newProtocolCheck.getProtocol();
                if (newProtocolCheck.equals(oldProtocolCheckMap.get(protocol))) {
                    finalProtocolsSet.add(oldProtocolCheckMap.get(protocol));
                } else {
                    finalProtocolsSet.add(newProtocolCheck);
                }
            }
            domainCheckEntityBuilder
                    .previousProtocolCheckEntities(oldProtocolChecks)
                    .protocolCheckEntities(finalProtocolsSet);
        }

        if (oldCertificates.isEmpty()) {
            domainCheckEntityBuilder.certificateEntities(newCertificates);
        } else if (oldCertificates.equals(newCertificates)) {
            domainCheckEntityBuilder
                    .certificateEntities(oldCertificates)
                    .previousCertificateEntities(oldCertificates);
        } else {
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
            domainCheckEntityBuilder
                    .previousCertificateEntities(oldCertificates)
                    .certificateEntities(finalCertificateSet);
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

    public PaginatedDto<DomainDto> getPaginatedDomains(DomainsOptionsDto options) {
        var domains = new ArrayList<DomainDto>();
        if (options.getShowLastChecks()) {
            var paginatedQueryResult = options.getFilter().isBlank() ?
                    domainCheckRepository.findAllLastChecksPerDomain(options.getPageRequest()) :
                    domainCheckRepository.findAllLastChecksPerDomainFiltered(options.getFilter(), options.getPageRequest());
            paginatedQueryResult.forEach(
                    (domain) -> domains.add(new DomainDto.Builder()
                            .domain(domain.getDomain())
                            .checkFrequencyMinutes(domain.getDomainEntity().getCheckFrequency())
                            .createdAt(domain.getDomainEntity().getCreatedAt())
                            .withHeaders(domain.getDomainEntity().getHeaders())
                            .timeoutMs(domain.getDomainEntity().getTimeoutMs())
                            .endpoint(domain.getDomainEntity().getEndpoint())
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
        } else {
            var paginatedQueryResult = options.getFilter().isBlank() ?
                    domainRepository.findAll(options.getPageRequest()) :
                    domainRepository.findAllFiltered(options.getFilter(), options.getPageRequest());

            paginatedQueryResult.forEach(
                    (domain) -> domains.add(new DomainDto.Builder()
                            .domain(domain.getDomain())
                            .endpoint(domain.getEndpoint())
                            .withHeaders(domain.getHeaders())
                            .timeoutMs(domain.getTimeoutMs())
                            .checkFrequencyMinutes(domain.getCheckFrequency())
                            .createdAt(domain.getCreatedAt())
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
    }

    public void scheduleDomainToCheck(DomainDto domainDto) {
        domainRepository.save(new DomainEntity.Builder()
                .frequency(domainDto.getCheckFrequencyMinutes())
                .domain(domainDto.getDomain())
                .timeoutMs(domainDto.getTimeoutMs())
                .endpoint(domainDto.getEndpoint())
                .headers(domainDto.getHeaders())
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

    public PaginatedDto<StateDto> getDomainStates(String domain, String protocol, Boolean includeCertificates, Integer page, Integer size) {
        final ArrayList<StateDto> stateList = new ArrayList<>();
        Page<DomainCheckEntity> domainCheckEntities;
        Optional<DomainCheckEntity> veryNextDomainCheckEntity = Optional.empty();
        if (protocol.equals("HTTP")) {
            domainCheckEntities = domainCheckRepository.findAllHttpStateChanges(domain, getDefaultPageRequest(page, size));
            if (!domainCheckEntities.isEmpty() && Optional.ofNullable(page).isPresent() && page > 0) {
                var previousPage = domainCheckRepository.findAllHttpStateChanges(domain, getDefaultPageRequest(page - 1, size));
                veryNextDomainCheckEntity = Optional.of(previousPage.getContent().get(previousPage.getSize() - 1));
            }
        } else {
            domainCheckEntities = domainCheckRepository.findAllHttpsStateChanges(domain, getDefaultPageRequest(page, size));
            if (includeCertificates) {
                domainCheckEntities = domainCheckRepository.findAllHttpsAndCertificateStateChanges(domain, getDefaultPageRequest(page, size));
                if (!domainCheckEntities.isEmpty() && Optional.ofNullable(page).isPresent() && page > 0) {
                    var previousPage = domainCheckRepository.findAllHttpsAndCertificateStateChanges(domain, getDefaultPageRequest(page - 1, size));
                    veryNextDomainCheckEntity = Optional.of(previousPage.getContent().get(previousPage.getSize() - 1));
                }
            } else {
                if (!domainCheckEntities.isEmpty() && Optional.ofNullable(page).isPresent() && page > 0) {
                    var previousPage = domainCheckRepository.findAllHttpsStateChanges(domain, getDefaultPageRequest(page - 1, size));
                    veryNextDomainCheckEntity = Optional.of(previousPage.getContent().get(previousPage.getSize() - 1));
                }
            }
        }

        for (DomainCheckEntity domainCheckEntity : domainCheckEntities) {
            var check = domainCheckEntity
                    .getProtocolCheckEntities()
                    .stream()
                    .collect(Collectors.toMap(protocolCheck -> protocolCheck.getProtocol().toString(), protocolCheck -> protocolCheck, (a, b) -> b))
                    .get(protocol.toUpperCase());

            var previousCheck = domainCheckEntity
                    .getPreviousProtocolCheckEntities()
                    .stream()
                    .collect(Collectors.toMap(protocolCheck -> protocolCheck.getProtocol().toString(), protocolCheck -> protocolCheck, (a, b) -> b))
                    .get(protocol.toUpperCase());

            Duration duration;
            if (veryNextDomainCheckEntity.isEmpty()) {
                duration = Duration.between(
                        domainCheckEntity.getTimeCheckedOn().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime(),
                        LocalDateTime.now()
                );
            } else {
                duration = Duration.between(
                        domainCheckEntity.getTimeCheckedOn().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime(),
                        veryNextDomainCheckEntity.get().getTimeCheckedOn().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime()
                );
            }

            var builder = new StateDto.Builder()
                    .hostname(check.getHostname())
                    .dnsResolves(check.getDnsResolves())
                    .redirectUri(check.getRedirectUri())
                    .connectionAccepted(check.isConnectionAccepted())
                    .changeType(domainCheckEntity.getChangeType())
                    .id(check.getId())
                    .statusCode(check.getStatusCode())
                    .protocol(check.getProtocol().toString())
                    .timeCheckedOn(domainCheckEntity.getTimeCheckedOn())
                    .duration(duration);

            if (Optional.ofNullable(previousCheck).isPresent()) {
                builder.previousState(
                        new StateDto.Builder()
                                .hostname(previousCheck.getHostname())
                                .dnsResolves(previousCheck.getDnsResolves())
                                .redirectUri(previousCheck.getRedirectUri())
                                .connectionAccepted(previousCheck.isConnectionAccepted())
                                .id(previousCheck.getId())
                                .statusCode(previousCheck.getStatusCode())
                                .protocol(previousCheck.getProtocol().toString())
                                .build()
                );
            }

            stateList.add(builder.build());

            veryNextDomainCheckEntity = Optional.of(domainCheckEntity);
        }
        return new PaginatedDto<>(
                stateList,
                domainCheckEntities.getTotalElements(),
                domainCheckEntities.getTotalPages(),
                domainCheckEntities.getNumber(),
                domainCheckEntities.getNumberOfElements()
        );
    }

    /**
     * Get the configuration of a scheduled domain
     * @param domain the domain
     * @return Optional data transferable object containing the scheduled domain's configuration
     */
    public Optional<DomainDto> getDomain(String domain) {
        var optionalDomain = domainCheckRepository.findDomainWithItsLastChecks(domain);
        return optionalDomain.map(domainCheckEntity -> new DomainDto.Builder()
                .domain(domainCheckEntity.getDomain())
                .checkFrequencyMinutes(domainCheckEntity.getDomainEntity().getCheckFrequency())
                .withHeaders(domainCheckEntity.getDomainEntity().getHeaders())
                .endpoint(domainCheckEntity.getDomainEntity().getEndpoint())
                .timeoutMs(domainCheckEntity.getDomainEntity().getTimeoutMs())
                .createdAt(domainCheckEntity.getDomainEntity().getCreatedAt())
                .lastDomainCheck(convertDomainCheckEntityToDto(domainCheckEntity))
                .build());
    }

    /**
     * Indicates if a domain is scheduled for checks
     * @param domain the domain to check
     * @return flag indicating if the domain is scheduled for checks
     */
    public boolean domainIsScheduled(String domain) {
        return domainRepository.existsById(domain);
    }

    /**
     * Remove a domain from the scheduler and all gathered metrics and previous checks.
     * @param domain the domain to be deleted and all its checks and metrics
     * @throws EntityNotFoundException if no entity found with the provided domain
     */
    public void deleteScheduledDomain(String domain) throws EntityNotFoundException {
        domainRepository.delete(domainRepository.findById(domain).orElseThrow(EntityNotFoundException::new));
    }

    /**
     * Update the configuration of a domain
     * @param domainUpdateDto data transferable object containing the updated values for the domain config
     * @throws EntityNotFoundException if no entity found with the provided domain in the dto
     */
    public void updateDomainConfig(DomainUpdateDto domainUpdateDto) throws EntityNotFoundException {
        domainRepository.save(
                new DomainEntity.Updater(
                        domainRepository.findById(domainUpdateDto.getDomain()).orElseThrow(EntityNotFoundException::new)
                ).withUpdatedValues(domainUpdateDto.getOperations()).build()
        );
    }
}
