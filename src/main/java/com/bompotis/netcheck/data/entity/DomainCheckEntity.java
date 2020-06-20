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

    protected DomainCheckEntity() {}

    public Long getHttpsResponseTimeNs() {
        return httpsResponseTimeNs;
    }

    public String getDomain() {
        return domain;
    }

    public DomainEntity getDomainEntity() {
        return domainEntity;
    }

    public Date getTimeCheckedOn() {
        return timeCheckedOn;
    }

    public Long getHttpResponseTimeNs() {
        return httpResponseTimeNs;
    }

    public Set<ProtocolCheckEntity> getProtocolCheckEntities() {
        return protocolCheckEntities;
    }

    public Set<CertificateEntity> getCertificateEntities() {
        return certificateEntities;
    }

    public static class Builder {
        private Set<ProtocolCheckEntity> protocolCheckEntities;
        private Set<CertificateEntity> certificateEntities;
        private String domain;
        private DomainEntity domainEntity;
        private Date timeCheckedOn;
        private Long httpResponseTimeNs;
        private Long httpsResponseTimeNs;

        public Builder protocolCheckEntities(Set<ProtocolCheckEntity> protocolCheckEntities) {
            this.protocolCheckEntities = protocolCheckEntities;
            return this;
        }

        public Builder certificateEntities(Set<CertificateEntity> certificateEntities) {
            this.certificateEntities = certificateEntities;
            return this;
        }

        public Builder domain(String domain) {
            this.domain = domain;
            return this;
        }

        public Builder domainEntity(DomainEntity domainEntity) {
            this.domainEntity = domainEntity;
            return this;
        }

        public Builder timeCheckedOn(Date timeCheckedOn) {
            this.timeCheckedOn = timeCheckedOn;
            return this;
        }

        public Builder httpResponseTimeNs(Long httpResponseTimeNs) {
            this.httpResponseTimeNs = httpResponseTimeNs;
            return this;
        }

        public Builder httpsResponseTimeNs(Long httpsResponseTimeNs) {
            this.httpsResponseTimeNs = httpsResponseTimeNs;
            return this;
        }

        public DomainCheckEntity build() {
            return new DomainCheckEntity(this);
        }
    }

    private DomainCheckEntity(Builder b) {
        this.protocolCheckEntities = b.protocolCheckEntities;
        this.certificateEntities = b.certificateEntities;
        this.domain = b.domain;
        this.domainEntity = b.domainEntity;
        this.timeCheckedOn = b.timeCheckedOn;
        this.httpResponseTimeNs = b.httpResponseTimeNs;
        this.httpsResponseTimeNs = b.httpsResponseTimeNs;
    }
}
