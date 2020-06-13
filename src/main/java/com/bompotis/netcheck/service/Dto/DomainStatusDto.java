package com.bompotis.netcheck.service.Dto;

import com.bompotis.netcheck.data.entities.DomainEntity;
import com.bompotis.netcheck.data.entities.DomainHistoricEntryEntity;
import com.bompotis.netcheck.data.repositories.DomainHistoricEntryRepository;
import com.bompotis.netcheck.data.repositories.DomainRepository;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

/**
 * Created by Kyriakos Bompotis on 10/6/20.
 */
public class DomainStatusDto {
    private final List<CertificateDetailsDto> caCertificates = new ArrayList<>();
    private final CertificateDetailsDto issuerCertificate;
    private Integer statusCode;
    private Boolean dnsResolved = true;
    private String ipAddress;
    private final Long responseTimeNs;
    private final String hostname;
    private final DomainRepository domainRepository;
    private final DomainHistoricEntryRepository domainHistoricEntryRepository;

    public DomainStatusDto(String hostname, DomainRepository domainRepository, DomainHistoricEntryRepository domainHistoricEntryRepository, Long responseTimeNs) {
        this.dnsResolved = false;
        this.hostname = hostname;
        this.domainRepository = domainRepository;
        this.domainHistoricEntryRepository = domainHistoricEntryRepository;
        this.issuerCertificate = null;
        this.responseTimeNs = responseTimeNs;
    }

    public DomainStatusDto(String hostname, String ipAddress, Integer statusCode, List<CertificateDetailsDto> certificates, DomainRepository domainRepository, DomainHistoricEntryRepository domainHistoricEntryRepository, Long responseTimeNs) {
        CertificateDetailsDto issuerCert = null;
        this.ipAddress = ipAddress;
        this.domainRepository = domainRepository;
        this.domainHistoricEntryRepository = domainHistoricEntryRepository;
        this.hostname = hostname;
        this.responseTimeNs = responseTimeNs;
        for (var cert: certificates) {
            if (cert.getBasicConstraints() < 0) {
                issuerCert = cert;
            } else {
                this.caCertificates.add(cert);
            }
        }
        this.issuerCertificate = issuerCert;
        this.statusCode = statusCode;
    }

    public List<CertificateDetailsDto> getCaCertificates() {
        return caCertificates;
    }

    public Integer getStatusCode() {
        return statusCode;
    }

    public CertificateDetailsDto getIssuerCertificate() {
        return issuerCertificate;
    }

    public Boolean getDnsResolved() {
        return dnsResolved;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public String getHostname() {
        return hostname;
    }

    public void storeResult() {
        var domainEntity = new DomainEntity();
        var domainEntityOptional = domainRepository.findById(hostname);
        if (domainEntityOptional.isPresent()) {
            domainEntity = domainEntityOptional.get();
        } else {
            domainEntity.setDomain(hostname);
        }
        var domainHistoricEntity = getHistoricEntry();
        domainHistoricEntity.setDomainEntity(domainEntity);
        domainHistoricEntryRepository.save(domainHistoricEntity);
    }

    public DomainHistoricEntryEntity getHistoricEntry() {
        var domainHistoryEntity = new DomainHistoricEntryEntity();
        if(Optional.ofNullable(issuerCertificate).isPresent()) {
            domainHistoryEntity.setCertificateExpiresOn(issuerCertificate.getNotAfter());
            domainHistoryEntity.setCertificateIsValid(issuerCertificate.isValid());
        }
        domainHistoryEntity.setDnsResolves(dnsResolved);
        domainHistoryEntity.setStatusCode(statusCode);
        domainHistoryEntity.setResponseTimeNs(responseTimeNs);
        domainHistoryEntity.setTimeCheckedOn(new Date());
        return domainHistoryEntity;
    }

    public Long getResponseTimeNs() {
        return responseTimeNs;
    }
}