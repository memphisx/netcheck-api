package com.bompotis.netcheck.service;

import com.bompotis.netcheck.data.entity.*;
import com.bompotis.netcheck.data.repository.DomainCheckRepository;
import com.bompotis.netcheck.data.repository.DomainRepository;
import com.bompotis.netcheck.service.dto.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import javax.net.ssl.HttpsURLConnection;
import java.io.IOException;
import java.net.*;
import java.security.cert.X509Certificate;
import java.util.*;

/**
 * Created by Kyriakos Bompotis on 30/11/18.
 */
@Service
public class DomainService {

    private final Set<Integer> STATUS_CODES_WITH_EMPTY_RESPONSES = Set.of(
            HttpURLConnection.HTTP_NOT_FOUND,
            HttpURLConnection.HTTP_BAD_REQUEST,
            HttpURLConnection.HTTP_NO_CONTENT,
            422
    );

    private final Set<Integer> REDIRECT_STATUS_CODES = Set.of(
            HttpURLConnection.HTTP_MOVED_TEMP,
            HttpURLConnection.HTTP_MOVED_PERM,
            HttpURLConnection.HTTP_SEE_OTHER
    );

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
            checkAssembler(httpCheckDtoBuilder, conn, beginTime);
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
            checkAssembler(httpCheckDtoBuilder, conn, beginTime);
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

    private void checkAssembler(HttpCheckDto.Builder httpCheckDtoBuilder, HttpURLConnection conn, long beginTime) throws IOException {
        var hostname = conn.getURL().getHost();
        httpCheckDtoBuilder
                .hostname(hostname)
                .timeCheckedOn(new Date())
                .ipAddress(InetAddress.getByName(hostname).getHostAddress())
                .dnsResolved(true);
        var responseCode = conn.getResponseCode();
        if (!STATUS_CODES_WITH_EMPTY_RESPONSES.contains(responseCode)) {
            conn.getInputStream();
        }
        if (REDIRECT_STATUS_CODES.contains(responseCode)) {
            httpCheckDtoBuilder.redirectUri(conn.getHeaderField("Location"));
        }
        httpCheckDtoBuilder.statusCode(conn.getResponseCode())
                .responseTimeNs(System.nanoTime() - beginTime);
        httpCheckDtoBuilder.statusCode(responseCode);
    }


    private URL getHttpsDomainUri(String domain) throws MalformedURLException {
        return new URL("https://" + domain);
    }


    private URL getHttpDomainUri(String domain) throws MalformedURLException {
        return new URL("http://" + domain);
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
                new ProtocolCheckEntity.Builder()
                        .dnsResolves(domainCheckDto.getHttpCheckDto().getDnsResolved())
                        .statusCode(domainCheckDto.getHttpCheckDto().getStatusCode())
                        .protocol(domainCheckDto.getHttpCheckDto().getProtocol())
                        .ipAddress(domainCheckDto.getHttpCheckDto().getIpAddress())
                        .hostname(domainCheckDto.getHttpCheckDto().getHostname())
                        .redirectUri(domainCheckDto.getHttpCheckDto().getRedirectUri())
                .build(),
                new ProtocolCheckEntity.Builder()
                        .dnsResolves(domainCheckDto.getHttpsCheckDto().getHttpCheckDto().getDnsResolved())
                        .statusCode(domainCheckDto.getHttpsCheckDto().getHttpCheckDto().getStatusCode())
                        .protocol(domainCheckDto.getHttpsCheckDto().getHttpCheckDto().getProtocol())
                        .ipAddress(domainCheckDto.getHttpsCheckDto().getHttpCheckDto().getIpAddress())
                        .hostname(domainCheckDto.getHttpsCheckDto().getHttpCheckDto().getHostname())
                        .redirectUri(domainCheckDto.getHttpsCheckDto().getHttpCheckDto().getRedirectUri())
                .build()
        ));


        var certificateEntities = new HashSet<CertificateEntity>();
        if(Optional.ofNullable(domainCheckDto.getHttpsCheckDto().getIssuerCertificate()).isPresent()) {
            certificateEntities.add(convertToDomainCertificateEntity(domainCheckDto.getHttpsCheckDto().getIssuerCertificate()));
        }
        domainCheckEntityBuilder.certificateEntities(certificateEntities);

        return domainCheckEntityBuilder;
    }

    private CertificateEntity convertToDomainCertificateEntity(CertificateDetailsDto certificateDetailsDto) {
        return new CertificateEntity.Builder()
                .basicConstraints(certificateDetailsDto.getBasicConstraints())
                .valid(certificateDetailsDto.isValid())
                .expired(certificateDetailsDto.getExpired())
                .notYetValid(certificateDetailsDto.getNotYetValid())
                .certificateIsValid(certificateDetailsDto.isValid())
                .issuedBy(certificateDetailsDto.getIssuedBy())
                .issuedFor(certificateDetailsDto.getIssuedFor())
                .notAfter(certificateDetailsDto.getNotAfter())
                .notBefore(certificateDetailsDto.getNotBefore())
                .build();
    }

    public PaginatedDomainCheckDto getDomainHistory(String domain, Integer page, Integer size) {
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

        return new PaginatedDomainsDto(
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
        var domainCheckDtoBuilder = new DomainCheckDto.Builder();

        for (var protocolCheck: domainCheckEntity.getProtocolCheckEntities()) {
            if (protocolCheck.getProtocol().equals(ProtocolCheckEntity.Protocol.HTTP)) {
                var httpCheckDto = new HttpCheckDto.Builder().statusCode(protocolCheck.getStatusCode())
                        .id(protocolCheck.getId())
                        .hostname(protocolCheck.getHostname())
                        .protocol(protocolCheck.getProtocol().toString())
                        .ipAddress(protocolCheck.getIpAddress())
                        .redirectUri(protocolCheck.getRedirectUri())
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
                        .redirectUri(protocolCheck.getRedirectUri())
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
