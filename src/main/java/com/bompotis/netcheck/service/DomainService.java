package com.bompotis.netcheck.service;

import com.bompotis.netcheck.data.entities.DomainEntity;
import com.bompotis.netcheck.data.repositories.DomainHistoricEntryRepository;
import com.bompotis.netcheck.data.repositories.DomainRepository;
import com.bompotis.netcheck.service.Dto.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import javax.net.ssl.HttpsURLConnection;
import java.io.IOException;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.UnknownHostException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Optional;

/**
 * Created by Kyriakos Bompotis on 30/11/18.
 */
@Service
public class DomainService {

    private final DomainRepository domainRepository;

    private final DomainHistoricEntryRepository domainHistoricEntryRepository;

    @Autowired
    public DomainService(DomainRepository domainRepository, DomainHistoricEntryRepository domainHistoricEntryRepository) {
        this.domainRepository = domainRepository;
        this.domainHistoricEntryRepository = domainHistoricEntryRepository;
    }

    private URL getHttpsDomainUri(String domain) throws MalformedURLException {
        return new URL("https://" + domain);
    }

    public DomainStatusDto buildAndCheck(String domain) throws IOException {
        var certificates = new ArrayList<CertificateDetailsDto>();
        try {
            var beginTime = System.nanoTime();
            var conn = (HttpsURLConnection) getHttpsDomainUri(domain).openConnection();
            var endtime = System.nanoTime();
            var hostname = conn.getURL().getHost();
            var ipAddress = InetAddress.getByName(hostname).getHostAddress();
            var statusCode = conn.getResponseCode();
            var responseTime = endtime - beginTime;
            var serverCerts = conn.getServerCertificates();
            for (var cert : serverCerts) {
                if(cert instanceof X509Certificate) {
                    X509Certificate x = (X509Certificate ) cert;
                    certificates.add(new CertificateDetailsDto(x));
                }
            }
            return new DomainStatusDto(hostname, ipAddress, statusCode, certificates, domainRepository, domainHistoricEntryRepository, responseTime);
        } catch (UnknownHostException e) {
            return new DomainStatusDto(domain, domainRepository, domainHistoricEntryRepository, null);
        }
    }

    public PaginatedDomainCheckDto getDomainHistory(String domain, Integer page, Integer size) {
        var domainCheckList = new ArrayList<DomainCheckDto>();
        var pageRequest = PageRequest.of(
                Optional.ofNullable(page).orElse(0),
                Optional.ofNullable(size).orElse(10)
        );
        var domainsHistoricEntries = domainHistoricEntryRepository.findAllByDomain(domain, pageRequest);
        for (var domainStatusEntry : domainsHistoricEntries) {
            domainCheckList.add(
                    new DomainCheckDto(
                            domainStatusEntry.getId(),
                            domain,
                            domainStatusEntry.getStatusCode(),
                            domainStatusEntry.getCertificateIsValid(),
                            domainStatusEntry.getCertificateExpiresOn(),
                            domainStatusEntry.getTimeCheckedOn(),
                            domainStatusEntry.getResponseTimeNs(),
                            domainStatusEntry.getDnsResolves()
                    )
            );
        }
        return new PaginatedDomainCheckDto(
                domainCheckList,
                domainsHistoricEntries.getTotalElements(),
                domainsHistoricEntries.getTotalPages(),
                domainsHistoricEntries.getNumber(),
                domainsHistoricEntries.getNumberOfElements()
        );
    }

    public Optional<DomainCheckDto> getDomainHistoricEntry(String domain, String id) {
        var queryResult = domainHistoricEntryRepository.findById(id);
        if (queryResult.isEmpty()) {
            return Optional.empty();
        }
        var domainHistoricEntryEntity = queryResult.get();
        var domainCheckDto = new DomainCheckDto(
                domainHistoricEntryEntity.getId(),
                domain,
                domainHistoricEntryEntity.getStatusCode(),
                domainHistoricEntryEntity.getCertificateIsValid(),
                domainHistoricEntryEntity.getCertificateExpiresOn(),
                domainHistoricEntryEntity.getTimeCheckedOn(),
                domainHistoricEntryEntity.getResponseTimeNs(),
                domainHistoricEntryEntity.getDnsResolves()
        );
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
