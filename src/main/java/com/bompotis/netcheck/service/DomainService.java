package com.bompotis.netcheck.service;

import com.bompotis.netcheck.data.entities.DomainEntity;
import com.bompotis.netcheck.data.repositories.DomainCheckRepository;
import com.bompotis.netcheck.data.repositories.DomainRepository;
import com.bompotis.netcheck.service.dto.*;
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

    private final DomainCheckRepository domainCheckRepository;

    @Autowired
    public DomainService(DomainRepository domainRepository, DomainCheckRepository domainCheckRepository) {
        this.domainRepository = domainRepository;
        this.domainCheckRepository = domainCheckRepository;
    }

    private URL getHttpsDomainUri(String domain) throws MalformedURLException {
        return new URL("https://" + domain);
    }

    public DomainStatusDto buildAndCheck(String domain) throws IOException {
        var certificates = new ArrayList<CertificateDetailsDto>();
        HttpsURLConnection conn = null;
        try {
            var beginTime = System.nanoTime();
            conn = (HttpsURLConnection) getHttpsDomainUri(domain).openConnection();
            conn.getInputStream();
            var hostname = conn.getURL().getHost();
            var ipAddress = InetAddress.getByName(hostname).getHostAddress();
            var statusCode = conn.getResponseCode();
            var responseTime = System.nanoTime() - beginTime;
            var serverCerts = conn.getServerCertificates();
            for (var cert : serverCerts) {
                if(cert instanceof X509Certificate) {
                    X509Certificate x = (X509Certificate ) cert;
                    certificates.add(new CertificateDetailsDto(x));
                }
            }
            return new DomainStatusDto(hostname, ipAddress, statusCode, certificates, domainRepository, domainCheckRepository, responseTime);
        } catch (UnknownHostException e) {
            return new DomainStatusDto(domain, domainRepository, domainCheckRepository, null);
        } finally {
            if (Optional.ofNullable(conn).isPresent()) {
                conn.disconnect();
            }
        }
    }

    public PaginatedDomainCheckDto getDomainHistory(String domain, Integer page, Integer size) {
        var domainCheckList = new ArrayList<DomainCheckDto>();
        var pageRequest = PageRequest.of(
                Optional.ofNullable(page).orElse(0),
                Optional.ofNullable(size).orElse(10)
        );
        var domainCheckEntities = domainCheckRepository.findAllByDomain(domain, pageRequest);
        for (var domainCheckEntity : domainCheckEntities) {
            domainCheckList.add(
                    new DomainCheckDto(
                            domainCheckEntity.getId(),
                            domain,
                            domainCheckEntity.getHttpsCheckEntity().getStatusCode(),
                            domainCheckEntity.getHttpsCheckEntity().getIssuerCertificate().getCertificateIsValid(),
                            domainCheckEntity.getHttpsCheckEntity().getIssuerCertificate().getCertificateExpiresOn(),
                            domainCheckEntity.getTimeCheckedOn(),
                            domainCheckEntity.getHttpsResponseTimeNs(),
                            domainCheckEntity.getHttpsCheckEntity().getDnsResolves()
                    )
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

    public Optional<DomainCheckDto> getDomainCheck(String domain, String id) {
        var queryResult = domainCheckRepository.findById(id);
        if (queryResult.isEmpty()) {
            return Optional.empty();
        }
        var domainCheckEntity = queryResult.get();
        var domainCheckDto = new DomainCheckDto(
                domainCheckEntity.getId(),
                domain,
                domainCheckEntity.getHttpsCheckEntity().getStatusCode(),
                domainCheckEntity.getHttpsCheckEntity().getIssuerCertificate().getCertificateIsValid(),
                domainCheckEntity.getHttpsCheckEntity().getIssuerCertificate().getCertificateExpiresOn(),
                domainCheckEntity.getTimeCheckedOn(),
                domainCheckEntity.getHttpsResponseTimeNs(),
                domainCheckEntity.getHttpsCheckEntity().getDnsResolves()
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
