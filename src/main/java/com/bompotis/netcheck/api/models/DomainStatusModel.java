package com.bompotis.netcheck.api.models;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.hateoas.RepresentationModel;
import org.springframework.hateoas.server.core.Relation;

import java.util.List;

/**
 * Created by Kyriakos Bompotis on 11/6/20.
 */
@Relation(collectionRelation = "domainStatuses", itemRelation = "domainStatus")
public class DomainStatusModel extends RepresentationModel<DomainStatusModel> {
    private final Integer statusCode;
    private final Boolean dnsResolved;
    private final List<CertificateModel> caCertificates;
    private final CertificateModel issuerCertificate;
    private final String ipAddress;
    private final String domain;
    private final Long responseTime;

    @JsonCreator
    public DomainStatusModel(
            @JsonProperty("caCertificates") List<CertificateModel> caCertificates,
            @JsonProperty("domain") String domain,
            @JsonProperty("ipAddress") String ipAddress,
            @JsonProperty("statusCode") Integer statusCode,
            @JsonProperty("dnsResolved") Boolean dnsResolved,
            @JsonProperty("responseTimeNs") Long responseTime,
            @JsonProperty("issuerCertificate") CertificateModel issuerCertificate) {
        this.caCertificates = caCertificates;
        this.domain = domain;
        this.statusCode = statusCode;
        this.ipAddress = ipAddress;
        this.dnsResolved = dnsResolved;
        this.issuerCertificate = issuerCertificate;
        this.responseTime = responseTime;
    }

    public Integer getStatusCode() {
        return statusCode;
    }

    public String getDomain() {
        return domain;
    }

    public Boolean getDnsResolved() {
        return dnsResolved;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public CertificateModel getIssuerCertificate() {
        return issuerCertificate;
    }

    public List<CertificateModel> getCaCertificates() {
        return caCertificates;
    }

    public Long getResponseTime() {
        return responseTime;
    }
}
