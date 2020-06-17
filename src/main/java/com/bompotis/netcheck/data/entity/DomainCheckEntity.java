package com.bompotis.netcheck.data.entity;

import javax.persistence.*;
import java.util.Date;
import java.util.Set;

/**
 * Created by Kyriakos Bompotis on 8/6/20.
 */
@Entity
@Table(name = "domain_check")
public class DomainCheckEntity extends AbstractTimestampablePersistable<String>{

    @OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<ProtocolCheckEntity> protocolCheckEntities;

    @OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<CertificateEntity> certificateEntities;

    @Column(name = "domain", insertable = false, updatable = false)
    private String domain;

    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.PERSIST)
    @JoinColumn(name = "domain")
    private DomainEntity domainEntity;

    @Column(name = "check_date")
    private Date timeCheckedOn;

    @Column(name = "http_response_time_ns")
    private Long httpResponseTimeNs;

    @Column(name = "https_response_time_ns")
    private Long httpsResponseTimeNs;

    public Long getHttpsResponseTimeNs() {
        return httpsResponseTimeNs;
    }

    public void setHttpsResponseTimeNs(Long responseTimeNs) {
        this.httpsResponseTimeNs = responseTimeNs;
    }

    public String getDomain() {
        return domain;
    }

    public void setDomain(String domain) {
        this.domain = domain;
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

    public Long getHttpResponseTimeNs() {
        return httpResponseTimeNs;
    }

    public void setHttpResponseTimeNs(Long httpResponseTimeNs) {
        this.httpResponseTimeNs = httpResponseTimeNs;
    }

    public Set<ProtocolCheckEntity> getProtocolCheckEntities() {
        return protocolCheckEntities;
    }

    public void setProtocolCheckEntities(Set<ProtocolCheckEntity> protocolCheckEntities) {
        this.protocolCheckEntities = protocolCheckEntities;
    }

    public Set<CertificateEntity> getCertificateEntities() {
        return certificateEntities;
    }

    public void setCertificateEntities(Set<CertificateEntity> certificateEntities) {
        this.certificateEntities = certificateEntities;
    }
}
