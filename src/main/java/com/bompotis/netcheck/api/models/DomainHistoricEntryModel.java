package com.bompotis.netcheck.api.models;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.hateoas.RepresentationModel;
import org.springframework.hateoas.server.core.Relation;

import java.util.Date;

/**
 * Created by Kyriakos Bompotis on 10/6/20.
 */
@Relation(collectionRelation = "historicEntries", itemRelation = "historicEntry")
public class DomainHistoricEntryModel extends RepresentationModel<DomainHistoricEntryModel> {
    private final Integer statusCode;
    private final Boolean certificateIsValid;
    private final Date certificateExpiresOn;
    private final Date timeCheckedOn;
    private final Boolean dnsResolves;
    private final String domain;

    @JsonCreator
    public DomainHistoricEntryModel(
            @JsonProperty("domain") String domain,
            @JsonProperty("statusCode") Integer statusCode,
            @JsonProperty("certificateIsValid") Boolean certificateIsValid,
            @JsonProperty("certificateExpiresOn") Date certificateExpiresOn,
            @JsonProperty("checkedOn") Date timeCheckedOn,
            @JsonProperty("dnsResolves") Boolean dnsResolves
    ) {
        this.domain = domain;
        this.statusCode = statusCode;
        this.certificateExpiresOn = certificateExpiresOn;
        this.certificateIsValid = certificateIsValid;
        this.timeCheckedOn = timeCheckedOn;
        this.dnsResolves = dnsResolves;
    }

    public Date getCertificateExpiresOn() {
        return certificateExpiresOn;
    }

    public Boolean getCertificateIsValid() {
        return certificateIsValid;
    }

    public Integer getStatusCode() {
        return statusCode;
    }

    public Boolean getDnsResolves() {
        return dnsResolves;
    }

    public Date getTimeCheckedOn() {
        return timeCheckedOn;
    }

    public String getDomain() {
        return domain;
    }
}
