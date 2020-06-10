package com.bompotis.netcheck.models;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.hateoas.RepresentationModel;

import java.util.Date;

/**
 * Created by Kyriakos Bompotis on 10/6/20.
 */
public class DomainHistoricEntry extends RepresentationModel<DomainHistoricEntry> {
    private Integer statusCode;
    private Boolean certificateIsValid;
    private Date certificateExpiresOn;
    private Date timeCheckedOn;
    private Boolean dnsResolves;
    private String domain;

    @JsonCreator
    public DomainHistoricEntry(
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
}
