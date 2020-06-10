package com.bompotis.netcheck.service;

import com.bompotis.netcheck.data.entities.DomainEntity;
import com.bompotis.netcheck.data.entities.DomainHistoricEntryEntity;
import com.bompotis.netcheck.data.repositories.DomainHistoricEntryRepository;
import com.bompotis.netcheck.data.repositories.DomainRepository;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by Kyriakos Bompotis on 10/6/20.
 */
public class DomainStatus {
    private final List<CertDetails> caCertificates = new ArrayList<>();
    private CertDetails issuerCertificate = null;
    private Integer statusCode;
    private Boolean dnsResolved = true;
    private String ipAddress;
    private String hostname;
    private final DomainRepository domainRepository;
    private final DomainHistoricEntryRepository domainHistoricEntryRepository;

    DomainStatus(DomainRepository domainRepository, DomainHistoricEntryRepository domainHistoricEntryRepository) {
        this.dnsResolved = false;
        this.domainRepository = domainRepository;
        this.domainHistoricEntryRepository = domainHistoricEntryRepository;
    }

    DomainStatus(String hostname, String ipAddress, Integer statusCode, List<CertDetails> certificates, DomainRepository domainRepository, DomainHistoricEntryRepository domainHistoricEntryRepository) {
        this.ipAddress = ipAddress;
        this.domainRepository = domainRepository;
        this.domainHistoricEntryRepository = domainHistoricEntryRepository;
        this.hostname = hostname;
        for (var cert: certificates) {
            if (cert.getBasicConstraints() < 0) {
                this.issuerCertificate = cert;
            } else {
                this.caCertificates.add(cert);
            }
        }
        this.statusCode = statusCode;
    }

    public List<CertDetails> getCaCertificates() {
        return caCertificates;
    }

    public Integer getStatusCode() {
        return statusCode;
    }

    public CertDetails getIssuerCertificate() {
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
        domainHistoryEntity.setCertificateExpiresOn(issuerCertificate.getNotAfter());
        domainHistoryEntity.setCertificateIsValid(issuerCertificate.isValid());
        domainHistoryEntity.setDnsResolves(dnsResolved);
        domainHistoryEntity.setStatusCode(statusCode);
        domainHistoryEntity.setTimeCheckedOn(new Date());
        return domainHistoryEntity;
    }
}