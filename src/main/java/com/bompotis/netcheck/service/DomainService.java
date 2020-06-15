package com.bompotis.netcheck.service;

import com.bompotis.netcheck.data.entities.DomainCertificateEntity;
import com.bompotis.netcheck.data.entities.DomainCheckEntity;
import com.bompotis.netcheck.data.entities.DomainEntity;
import com.bompotis.netcheck.data.entities.HttpsCheckEntity;
import com.bompotis.netcheck.data.repositories.DomainCheckRepository;
import com.bompotis.netcheck.data.repositories.DomainRepository;
import com.bompotis.netcheck.service.dto.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import javax.net.ssl.HttpsURLConnection;
import java.io.IOException;
import java.net.*;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Date;
import java.util.Optional;

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

    private URL getHttpsDomainUri(String domain) throws MalformedURLException {
        return new URL("https://" + domain);
    }

    public DomainStatusDto check(String domain) throws IOException {
        var domainStatusDtoBuilder = new DomainStatusDto.Builder()
                .domain(domain);
        HttpsURLConnection conn = null;
        try {
            var beginTime = System.nanoTime();
            conn = (HttpsURLConnection) getHttpsDomainUri(domain).openConnection();
            conn.getInputStream();
            var hostname = conn.getURL().getHost();
            domainStatusDtoBuilder = domainStatusDtoBuilder
                    .hostname(hostname)
                    .ipAddress(InetAddress.getByName(hostname).getHostAddress())
                    .dnsResolved(true)
                    .statusCode(conn.getResponseCode())
                    .responseTimeNs(System.nanoTime() - beginTime);
            var serverCerts = conn.getServerCertificates();
            for (var cert : serverCerts) {
                if(cert instanceof X509Certificate) {
                    domainStatusDtoBuilder = domainStatusDtoBuilder
                            .certificate(new CertificateDetailsDto((X509Certificate ) cert));
                }
            }
        } catch (UnknownHostException e) {
            domainStatusDtoBuilder = domainStatusDtoBuilder.dnsResolved(false);
        } finally {
            Optional.ofNullable(conn)
                    .ifPresent(HttpURLConnection::disconnect);
        }
        return domainStatusDtoBuilder.build();
    }

    public void storeResult(DomainStatusDto domainStatusDto) {
        var domainEntity = new DomainEntity();
        var domainEntityOptional = domainRepository.findById(domainStatusDto.getHostname());
        if (domainEntityOptional.isPresent()) {
            domainEntity = domainEntityOptional.get();
        } else {
            domainEntity.setDomain(domainStatusDto.getHostname());
        }
        var domainCheckEntity = convertToDomainCheckEntity(domainStatusDto);
        domainCheckEntity.setDomainEntity(domainEntity);
        domainCheckRepository.save(domainCheckEntity);
    }

    public DomainCheckEntity convertToDomainCheckEntity(DomainStatusDto domainStatusDto) {
        var domainCheckEntity = new DomainCheckEntity();
        var httpsCheckEntity = new HttpsCheckEntity();
        httpsCheckEntity.setDnsResolves(domainStatusDto.getDnsResolved());
        httpsCheckEntity.setStatusCode(domainStatusDto.getStatusCode());

        if(Optional.ofNullable(domainStatusDto.getIssuerCertificate()).isPresent()) {
            httpsCheckEntity.setIssuerCertificate(convertToDomainCertificateEntity(domainStatusDto.getIssuerCertificate()));
        }

        domainCheckEntity.setHttpsCheckEntity(httpsCheckEntity);

        domainCheckEntity.setHttpsResponseTimeNs(domainStatusDto.getResponseTimeNs());
        domainCheckEntity.setTimeCheckedOn(new Date());
        return domainCheckEntity;
    }

    private DomainCertificateEntity convertToDomainCertificateEntity(CertificateDetailsDto certificateDetailsDto) {
        var issuerCertificateEntity = new DomainCertificateEntity();
        issuerCertificateEntity.setBasicConstraints(certificateDetailsDto.getBasicConstraints());
        issuerCertificateEntity.setValid(certificateDetailsDto.isValid());
        issuerCertificateEntity.setExpired(certificateDetailsDto.getExpired());
        issuerCertificateEntity.setNotYetValid(certificateDetailsDto.getNotYetValid());
        issuerCertificateEntity.setCertificateIsValid(certificateDetailsDto.isValid());
        issuerCertificateEntity.setIssuedBy(certificateDetailsDto.getIssuedBy());
        issuerCertificateEntity.setIssuedFor(certificateDetailsDto.getIssuedFor());
        issuerCertificateEntity.setNotAfter(certificateDetailsDto.getNotAfter());
        issuerCertificateEntity.setNotBefore(certificateDetailsDto.getNotBefore());
        return issuerCertificateEntity;
    }

    public PaginatedDomainCheckDto getDomainHistory(String domain, Integer page, Integer size) {
        var domainCheckList = new ArrayList<DomainStatusDto>();
        var pageRequest = PageRequest.of(
                Optional.ofNullable(page).orElse(0),
                Optional.ofNullable(size).orElse(10)
        );
        var domainCheckEntities = domainCheckRepository.findAllByDomain(domain, pageRequest);
        for (var domainCheckEntity : domainCheckEntities) {
            var httpsCheckEntity = domainCheckEntity.getHttpsCheckEntity();
            var issuerCertificate = httpsCheckEntity.getIssuerCertificate();
            domainCheckList.add(
                    new DomainStatusDto.Builder()
                            .domain(domain)
                            .statusCode(httpsCheckEntity.getStatusCode())
                            .id(domainCheckEntity.getId())
                            .responseTimeNs(domainCheckEntity.getHttpsResponseTimeNs())
                            .timeCheckedOn(domainCheckEntity.getTimeCheckedOn())
                            .dnsResolved(httpsCheckEntity.getDnsResolves())
                            .certificate(new CertificateDetailsDto.Builder()
                                    .valid(issuerCertificate.getCertificateIsValid())
                                    .notAfter(issuerCertificate.getNotAfter())
                                    .notBefore(issuerCertificate.getNotBefore())
                                    .expired(issuerCertificate.getExpired())
                                    .basicConstraints(issuerCertificate.getBasicConstraints())
                                    .issuedBy(issuerCertificate.getIssuedBy())
                                    .issuedFor(issuerCertificate.getIssuedFor())
                                    .notYetValid(issuerCertificate.getNotYetValid())
                                    .build())
                            .build()
            );
        }
        return new PaginatedDomainCheckDto(
                domainCheckList,
                domainCheckEntities.getTotalElements(),
                domainCheckEntities.getTotalPages(),
                domainCheckEntities.getNumber(),
                domainCheckEntities.getNumberOfElements()
        );
    }

    public Optional<DomainStatusDto> getDomainCheck(String domain, String id) {
        var queryResult = domainCheckRepository.findById(id);
        if (queryResult.isEmpty()) {
            return Optional.empty();
        }
        var domainCheckEntity = queryResult.get();
        var httpsEntity = domainCheckEntity.getHttpsCheckEntity();
        var issuerCertificate = httpsEntity.getIssuerCertificate();
        var domainCheckDto = new DomainStatusDto.Builder()
                .domain(domain)
                .id(domainCheckEntity.getId())
                .timeCheckedOn(domainCheckEntity.getTimeCheckedOn())
                .responseTimeNs(domainCheckEntity.getHttpsResponseTimeNs())
                .dnsResolved(httpsEntity.getDnsResolves())
                .statusCode(httpsEntity.getStatusCode())
                .certificate(new CertificateDetailsDto.Builder()
                        .valid(issuerCertificate.getCertificateIsValid())
                        .notAfter(issuerCertificate.getNotAfter())
                        .notBefore(issuerCertificate.getNotBefore())
                        .expired(issuerCertificate.getExpired())
                        .basicConstraints(issuerCertificate.getBasicConstraints())
                        .issuedBy(issuerCertificate.getIssuedBy())
                        .issuedFor(issuerCertificate.getIssuedFor())
                        .notYetValid(issuerCertificate.getNotYetValid())
                        .build())
                .build();

        return Optional.of(domainCheckDto);
    }

    public PaginatedDomainsDto getPaginatedDomains(Integer page, Integer size) {
        var domains = new ArrayList<String>();
        var paginatedQueryResult = domainRepository.findAll(
                PageRequest.of(
                        Optional.ofNullable(page).orElse(0),
                        Optional.ofNullable(size).orElse(10)
                )
        );
        for (var domain : paginatedQueryResult) {
            domains.add(domain.getDomain());
        }

        return new PaginatedDomainsDto(
                domains,
                paginatedQueryResult.getTotalElements(),
                paginatedQueryResult.getTotalPages(),
                paginatedQueryResult.getNumber(),
                paginatedQueryResult.getNumberOfElements()
        );
    }

    public void scheduleDomainToCheck(String domain) {
        var domainEntity = new DomainEntity();
        domainEntity.setDomain(domain);
        domainRepository.save(domainEntity);
    }

}
