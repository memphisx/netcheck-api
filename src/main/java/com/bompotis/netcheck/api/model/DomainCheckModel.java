package com.bompotis.netcheck.api.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.hateoas.RepresentationModel;
import org.springframework.hateoas.server.core.Relation;

import java.util.Collection;
import java.util.List;

/**
 * Created by Kyriakos Bompotis on 10/6/20.
 */
@Relation(collectionRelation = "domainChecks", itemRelation = "domainCheck")
public class DomainCheckModel extends RepresentationModel<DomainCheckModel> {
    private final Collection<HttpCheckModel> httpChecks;
    private final CertificateModel issuerCertificate;
    private final List<CertificateModel> caCertificates;
    private final String domain;

    public DomainCheckModel(@JsonProperty("domain") String domain,
                            @JsonProperty("httpChecks") Collection<HttpCheckModel> httpChecks,
                            @JsonProperty("issuerCertificate") CertificateModel issuerCertificate,
                            @JsonProperty("caCertificates") List<CertificateModel> caCertificates) {
        this.domain = domain;
        this.httpChecks = httpChecks;
        this.issuerCertificate = issuerCertificate;
        this.caCertificates = caCertificates;
    }

    public CertificateModel getIssuerCertificate() {
        return issuerCertificate;
    }

    public List<CertificateModel> getCaCertificates() {
        return caCertificates;
    }

    public Collection<HttpCheckModel> getHttpChecks() {
        return httpChecks;
    }

    public String getDomain() {
        return domain;
    }
}
