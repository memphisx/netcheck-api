package com.bompotis.netcheck.service;

import com.bompotis.netcheck.data.entities.*;
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
import java.util.HashSet;
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

    public DomainCheckDto check(String domain) throws IOException {
        return new DomainCheckDto.Builder()
                .httpCheckDto(checkHttp(domain))
                .httpsCheckDto(checkHttps(domain))
                .domain(domain)
                .build();
    }

    private HttpCheckDto checkHttp(String domain) throws IOException {
        var httpCheckDtoBuilder = new HttpCheckDto.Builder().protocol("HTTP");
        HttpURLConnection conn = null;
        try {
            var beginTime = System.nanoTime();
            conn = (HttpURLConnection) getHttpDomainUri(domain).openConnection();
            conn.getInputStream();
            var hostname = conn.getURL().getHost();
            httpCheckDtoBuilder
                    .timeCheckedOn(new Date())
                    .hostname(hostname)
                    .ipAddress(InetAddress.getByName(hostname).getHostAddress())
                    .dnsResolved(true)
                    .statusCode(conn.getResponseCode())
                    .responseTimeNs(System.nanoTime() - beginTime);
        } catch (UnknownHostException e) {
            httpCheckDtoBuilder.dnsResolved(false);
        } finally {
            Optional.ofNullable(conn)
                    .ifPresent(HttpURLConnection::disconnect);
        }
        return httpCheckDtoBuilder.build();
    }

    private HttpsCheckDto checkHttps(String domain) throws IOException {
        var httpsCheckDtoBuilder = new HttpsCheckDto.Builder();
        var httpCheckDtoBuilder = new HttpCheckDto.Builder().protocol("HTTPS");
        HttpsURLConnection conn = null;
        try {
            var beginTime = System.nanoTime();
            conn = (HttpsURLConnection) getHttpsDomainUri(domain).openConnection();
            conn.getInputStream();
            var hostname = conn.getURL().getHost();
            httpCheckDtoBuilder
                    .hostname(hostname)
                    .timeCheckedOn(new Date())
                    .ipAddress(InetAddress.getByName(hostname).getHostAddress())
                    .dnsResolved(true)
                    .statusCode(conn.getResponseCode())
                    .responseTimeNs(System.nanoTime() - beginTime);
            var serverCerts = conn.getServerCertificates();
            for (var cert : serverCerts) {
                if(cert instanceof X509Certificate) {
                    httpsCheckDtoBuilder
                            .certificate(new CertificateDetailsDto((X509Certificate ) cert));
                }
            }
        } catch (UnknownHostException e) {
            httpCheckDtoBuilder.dnsResolved(false);
        } finally {
            Optional.ofNullable(conn)
                    .ifPresent(HttpURLConnection::disconnect);
        }
        return httpsCheckDtoBuilder.httpCheckDto(httpCheckDtoBuilder.build()).build();
    }


    private URL getHttpsDomainUri(String domain) throws MalformedURLException {
        return new URL("https://" + domain);
    }


    private URL getHttpDomainUri(String domain) throws MalformedURLException {
        return new URL("http://" + domain);
    }


    public void storeResult(DomainCheckDto domainCheckDto) {
        var domainEntity = new DomainEntity();
        var domainEntityOptional = domainRepository.findById(domainCheckDto.getDomain());
        if (domainEntityOptional.isPresent()) {
            domainEntity = domainEntityOptional.get();
        } else {
            domainEntity.setDomain(domainCheckDto.getDomain());
        }
        var domainCheckEntity = convertToDomainCheckEntity(domainCheckDto);
        domainCheckEntity.setDomainEntity(domainEntity);
        domainCheckRepository.save(domainCheckEntity);
    }

    public DomainCheckEntity convertToDomainCheckEntity(DomainCheckDto domainCheckDto) {
        var domainCheckEntity = new DomainCheckEntity();
        var protocolCheckEntities = new HashSet<ProtocolCheckEntity>();
        var certificateEntities = new HashSet<CertificateEntity>();
        domainCheckEntity.setDomain(domainCheckDto.getDomain());

        var httpCheckEntity = new ProtocolCheckEntity();
        httpCheckEntity.setDnsResolves(domainCheckDto.getHttpCheckDto().getDnsResolved());
        httpCheckEntity.setStatusCode(domainCheckDto.getHttpCheckDto().getStatusCode());
        httpCheckEntity.setProtocol(domainCheckDto.getHttpCheckDto().getProtocol());
        httpCheckEntity.setIpAddress(domainCheckDto.getHttpCheckDto().getIpAddress());
        httpCheckEntity.setHostname(domainCheckDto.getHttpCheckDto().getHostname());
        protocolCheckEntities.add(httpCheckEntity);

        domainCheckEntity.setHttpResponseTimeNs(domainCheckDto.getHttpCheckDto().getResponseTimeNs());
        domainCheckEntity.setTimeCheckedOn(domainCheckDto.getHttpCheckDto().getTimeCheckedOn());

        var httpsCheckEntity = new ProtocolCheckEntity();
        httpsCheckEntity.setDnsResolves(domainCheckDto.getHttpsCheckDto().getHttpCheckDto().getDnsResolved());
        httpsCheckEntity.setStatusCode(domainCheckDto.getHttpsCheckDto().getHttpCheckDto().getStatusCode());
        httpsCheckEntity.setProtocol(domainCheckDto.getHttpsCheckDto().getHttpCheckDto().getProtocol());
        httpsCheckEntity.setIpAddress(domainCheckDto.getHttpsCheckDto().getHttpCheckDto().getIpAddress());
        httpsCheckEntity.setHostname(domainCheckDto.getHttpsCheckDto().getHttpCheckDto().getHostname());
        protocolCheckEntities.add(httpsCheckEntity);

        domainCheckEntity.setHttpsResponseTimeNs(domainCheckDto.getHttpsCheckDto().getHttpCheckDto().getResponseTimeNs());
        domainCheckEntity.setProtocolCheckEntities(protocolCheckEntities);

        if(Optional.ofNullable(domainCheckDto.getHttpsCheckDto().getIssuerCertificate()).isPresent()) {
            certificateEntities.add(convertToDomainCertificateEntity(domainCheckDto.getHttpsCheckDto().getIssuerCertificate()));
        }
        domainCheckEntity.setCertificateEntities(certificateEntities);

        return domainCheckEntity;
    }

    private CertificateEntity convertToDomainCertificateEntity(CertificateDetailsDto certificateDetailsDto) {
        var issuerCertificateEntity = new CertificateEntity();
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
        var domainCheckList = new ArrayList<DomainCheckDto>();
        var pageRequest = PageRequest.of(
                Optional.ofNullable(page).orElse(0),
                Optional.ofNullable(size).orElse(10)
        );
        var domainCheckEntities = domainCheckRepository.findAllByDomain(domain, pageRequest);
        for (var domainCheckEntity : domainCheckEntities) {
            domainCheckList.add(convertDomainCheckEntityToDto(domainCheckEntity));
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
        return Optional.of(convertDomainCheckEntityToDto(domainCheckEntity));
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

    public DomainCheckDto convertDomainCheckEntityToDto(DomainCheckEntity domainCheckEntity) {
        var domainCheckDtoBuilder = new DomainCheckDto.Builder();

        for (var protocolCheck: domainCheckEntity.getProtocolCheckEntities()) {
            if (protocolCheck.getProtocol().equals(ProtocolCheckEntity.Protocol.HTTP)) {
                var httpCheckDto = new HttpCheckDto.Builder().statusCode(protocolCheck.getStatusCode())
                        .id(protocolCheck.getId())
                        .hostname(protocolCheck.getHostname())
                        .protocol(protocolCheck.getProtocol().toString())
                        .ipAddress(protocolCheck.getIpAddress())
                        .responseTimeNs(domainCheckEntity.getHttpResponseTimeNs())
                        .timeCheckedOn(domainCheckEntity.getTimeCheckedOn())
                        .dnsResolved(protocolCheck.getDnsResolves())
                        .build();
                domainCheckDtoBuilder.httpCheckDto(httpCheckDto);
            }
            if (protocolCheck.getProtocol().equals(ProtocolCheckEntity.Protocol.HTTPS)) {
                var httpsCheckDtoBuilder = new HttpsCheckDto.Builder();
                var httpCheckDto = new HttpCheckDto.Builder().statusCode(protocolCheck.getStatusCode())
                        .id(protocolCheck.getId())
                        .hostname(protocolCheck.getHostname())
                        .protocol(protocolCheck.getProtocol().toString())
                        .ipAddress(protocolCheck.getIpAddress())
                        .responseTimeNs(domainCheckEntity.getHttpsResponseTimeNs())
                        .timeCheckedOn(domainCheckEntity.getTimeCheckedOn())
                        .dnsResolved(protocolCheck.getDnsResolves())
                        .build();
                httpsCheckDtoBuilder.httpCheckDto(httpCheckDto);

                for (var certificate : domainCheckEntity.getCertificateEntities()) {
                    var certificateDto = new CertificateDetailsDto.Builder()
                            .valid(certificate.getCertificateIsValid())
                            .notAfter(certificate.getNotAfter())
                            .notBefore(certificate.getNotBefore())
                            .expired(certificate.getExpired())
                            .basicConstraints(certificate.getBasicConstraints())
                            .issuedBy(certificate.getIssuedBy())
                            .issuedFor(certificate.getIssuedFor())
                            .notYetValid(certificate.getNotYetValid())
                            .build();
                    httpsCheckDtoBuilder.certificate(certificateDto);
                }
                domainCheckDtoBuilder.httpsCheckDto(httpsCheckDtoBuilder.build());
            }
        }
        return domainCheckDtoBuilder
                .domain(domainCheckEntity.getDomain())
                .id(domainCheckEntity.getId())
                .build();
    }

}
