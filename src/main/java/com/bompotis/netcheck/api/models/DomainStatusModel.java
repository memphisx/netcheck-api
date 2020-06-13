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
    private final List<CertificateModel> caCertificates;
    private final CertificateModel issuerCertificate;
    private final Integer statusCode;
    private final Boolean dnsResolved;
    private final String ipAddress;
    private final String hostname;

    @JsonCreator
    public DomainStatusModel(
            @JsonProperty("caCertificates") List<CertificateModel> caCertificates,
            @JsonProperty("hostname") String hostname,
            @JsonProperty("ipAddress") String ipAddress,
            @JsonProperty("statusCode") Integer statusCode,
            @JsonProperty("dnsResolved") Boolean dnsResolved,
            @JsonProperty("issuerCertificate") CertificateModel issuerCertificate) {
        this.caCertificates = caCertificates;
        this.hostname = hostname;
        this.statusCode = statusCode;
        this.ipAddress = ipAddress;
        this.dnsResolved = dnsResolved;
        this.issuerCertificate = issuerCertificate;
    }

    public Integer getStatusCode() {
        return statusCode;
    }

    public String getHostname() {
        return hostname;
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
}
