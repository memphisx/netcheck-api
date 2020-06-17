package com.bompotis.netcheck.api.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.hateoas.RepresentationModel;
import org.springframework.hateoas.server.core.Relation;

/**
 * Created by Kyriakos Bompotis on 10/6/20.
 */
@Relation(collectionRelation = "domainChecks", itemRelation = "domainCheck")
public class DomainCheckModel extends RepresentationModel<DomainCheckModel> {
    private final HttpCheckModel httpCheck;
    private final HttpCheckModel httpsCheck;
    private final CertificateModel issuerCertificate;

    public DomainCheckModel(@JsonProperty("httpCheck") HttpCheckModel httpCheck,
                            @JsonProperty("httpsCheck") HttpCheckModel httpsCheck,
                            @JsonProperty("issuerCertificate") CertificateModel issuerCertificate) {
        this.httpCheck = httpCheck;
        this.httpsCheck = httpsCheck;
        this.issuerCertificate = issuerCertificate;
    }

    public HttpCheckModel getHttpCheck() {
        return httpCheck;
    }

    public HttpCheckModel getHttpsCheck() {
        return httpsCheck;
    }

    public CertificateModel getIssuerCertificate() {
        return issuerCertificate;
    }
}
