package com.bompotis.netcheck.data.entities;


import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.util.Date;

/**
 * Created by Kyriakos Bompotis on 8/6/20.
 */
@Entity
@Table(name = "domain_historic_entry")
public class DomainHistoricEntryEntity {

    @Id
    @GeneratedValue(generator="uuid")
    @GenericGenerator(name="uuid", strategy = "uuid2")
    @Column(name = "id")
    private String id;

    @Column(name = "status_code")
    private Integer statusCode;

    @Column(name = "valid_certificate")
    private Boolean certificateIsValid;

    @Column(name = "cert_expiration_date")
    private Date certificateExpiresOn;

    @Column(name = "check_date")
    private Date timeCheckedOn;

    @Column(name = "dns_resolves")
    private Boolean dnsResolves;

    @Column(name = "response_time_ns")
    private Long responseTimeNs;

    @Column(name = "domain", insertable = false, updatable = false)
    private String domain;

    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.PERSIST)
    @JoinColumn(name = "domain")
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

    public String getDomain() {
        return domain;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }

    public Long getResponseTimeNs() {
        return responseTimeNs;
    }

    public void setResponseTimeNs(Long responseTimeNs) {
        this.responseTimeNs = responseTimeNs;
    }
}
