package com.bompotis.netcheck.data.entities;


import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.util.Date;

/**
 * Created by Kyriakos Bompotis on 8/6/20.
 */
@Entity
public class DomainHistoryEntry {

    @Id
    @GeneratedValue(generator="uuid")
    @GenericGenerator(name="uuid", strategy = "uuid2")
    private String id;
    private Integer statusCode;
    private Boolean certificateIsValid;
    private Date certificateExpiresOn;
    private Date timeCheckedOn;
    private Boolean dnsResolves;

    @ManyToOne(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    private DomainEntity domainEntity;


    public void setId(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public Integer getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(Integer statusCode) {
        this.statusCode = statusCode;
    }

    public Boolean getCertificateIsValid() {
        return certificateIsValid;
    }

    public void setCertificateIsValid(Boolean certificateIsValid) {
        this.certificateIsValid = certificateIsValid;
    }

    public Date getCertificateExpiresOn() {
        return certificateExpiresOn;
    }

    public void setCertificateExpiresOn(Date certificateExpiresOn) {
        this.certificateExpiresOn = certificateExpiresOn;
    }

    public Boolean getDnsResolves() {
        return dnsResolves;
    }

    public void setDnsResolves(Boolean dnsResolves) {
        this.dnsResolves = dnsResolves;
    }

    public DomainEntity getDomainEntity() {
        return domainEntity;
    }

    public void setDomainEntity(DomainEntity domainEntity) {
        this.domainEntity = domainEntity;
    }

    public Date getTimeCheckedOn() {
        return timeCheckedOn;
    }

    public void setTimeCheckedOn(Date timeCheckedOn) {
        this.timeCheckedOn = timeCheckedOn;
    }
}
