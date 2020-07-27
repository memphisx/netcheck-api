/*
 * Copyright 2020 the original author or authors.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This code is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package com.bompotis.netcheck.api.model;

import com.fasterxml.jackson.annotation.JsonInclude;
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
    private final Boolean monitored;
    private final String domain;

    public DomainCheckModel(@JsonProperty("domain") String domain,
                            @JsonProperty("monitored") Boolean monitored,
                            @JsonProperty("httpChecks") Collection<HttpCheckModel> httpChecks,
                            @JsonProperty("issuerCertificate") CertificateModel issuerCertificate,
                            @JsonProperty("caCertificates") List<CertificateModel> caCertificates) {
        this.domain = domain;
        this.monitored = monitored;
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

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public Boolean getMonitored() {
        return monitored;
    }
}
