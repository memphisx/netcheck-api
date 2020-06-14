package com.bompotis.netcheck.service.dto;

import java.util.Date;

/**
 * Created by Kyriakos Bompotis on 12/6/20.
 */
public class DomainCheckDto {
    private final String id;
    private final String domain;
    private final Integer statusCode;
    private final Boolean certificateIsValid;
    private final Date certificateExpiresOn;
    private final Date timeCheckedOn;
    private final Long responseTimeNs;
    private final Boolean dnsResolves;

    public DomainCheckDto(String id,
                          String domain,
                          Integer statusCode,
                          Boolean certificateIsValid,
                          Date certificateExpiresOn,
                          Date timeCheckedOn,
                          Long responseTimeNs,
                          Boolean dnsResolves) {
        this.id = id;
        this.domain = domain;
        this.statusCode = statusCode;
        this.responseTimeNs = responseTimeNs;
        this.certificateIsValid = certificateIsValid;
        this.certificateExpiresOn = certificateExpiresOn;
        this.timeCheckedOn = timeCheckedOn;
        this.dnsResolves = dnsResolves;
    }

    public String getId() {
        return id;
    }

    public Integer getStatusCode() {
        return statusCode;
    }

    public Boolean getCertificateIsValid() {
        return certificateIsValid;
    }

    public Date getCertificateExpiresOn() {
        return certificateExpiresOn;
    }

    public Date getTimeCheckedOn() {
        return timeCheckedOn;
    }

    public Boolean getDnsResolves() {
        return dnsResolves;
    }

    public String getDomain() {
        return domain;
    }

    public Long getResponseTimeNs() {
        return responseTimeNs;
    }
}
