package com.bompotis.netcheck.data.entity;

import org.springframework.lang.NonNull;

import javax.persistence.*;
import java.util.Date;
import java.util.Objects;
import java.util.Set;

/**
 * Created by Kyriakos Bompotis on 8/6/20.
 */
@Entity
@Table(name = "domain_check",
        indexes = {
                @Index(name = "domain_check__domain_http_https_cert_idx", columnList = "domain,http_check_change,https_check_change,certificates_change"),
                @Index(name = "domain_check__domain_created_idx", columnList = "domain,created_at"),
                @Index(name = "domain_check__domain_check_date_idx", columnList = "domain,check_date")
})
public class DomainCheckEntity extends AbstractTimestampablePersistable<String>{

    @ManyToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinTable(
            name = "check_protocol",
            joinColumns = @JoinColumn(name = "domain_check_id"),
            inverseJoinColumns = @JoinColumn(name = "protocol_check_id"))
    private Set<ProtocolCheckEntity> protocolCheckEntities;

    @ManyToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinTable(
            name = "check_certificate",
            joinColumns = @JoinColumn(name = "domain_check_id"),
            inverseJoinColumns = @JoinColumn(name = "certificate_id"))
    private Set<CertificateEntity> certificateEntities;

    @NonNull
    @Column(name = "certificates_change")
    private boolean certificatesChange;

    @NonNull
    @Column(name = "http_check_change")
    private boolean httpCheckChange;

    @NonNull
    @Column(name = "https_check_change")
    private boolean httpsCheckChange;

    @NonNull
    @Column(name = "domain", insertable = false, updatable = false)
    private String domain;

    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.PERSIST)
    @JoinColumn(name = "domain")
    private DomainEntity domainEntity;

    @Column(name = "http_ip_address")
    private String httpIpAddress;

    @Column(name = "https_ip_address")
    private String httpsIpAddress;

    @NonNull
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

    public String getHttpIpAddress() {
        return httpIpAddress;
    }

    public String getHttpsIpAddress() {
        return httpsIpAddress;
    }

    public boolean isCertificatesChange() {
        return certificatesChange;
    }

    public boolean isHttpCheckChange() {
        return httpCheckChange;
    }

    public boolean isHttpsCheckChange() {
        return httpsCheckChange;
    }

    public static class Builder {
        private Set<ProtocolCheckEntity> protocolCheckEntities;
        private Set<CertificateEntity> certificateEntities;
        private String domain;
        private DomainEntity domainEntity;
        private Date timeCheckedOn;
        private Long httpResponseTimeNs;
        private Long httpsResponseTimeNs;
        private boolean certificatesChange;
        private boolean httpCheckChange;
        private boolean httpsCheckChange;
        private String httpIpAddress;
        private String httpsIpAddress;

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

        public Builder certificatesChange(boolean certificatesChange) {
            this.certificatesChange = certificatesChange;
            return this;
        }

        public Builder httpCheckChange(boolean httpCheckChange) {
            this.httpCheckChange = httpCheckChange;
            return this;
        }

        public Builder httpsCheckChange(boolean httpsCheckChange) {
            this.httpsCheckChange = httpsCheckChange;
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

        public Builder httpIpAddress(String httpIpAddress) {
            this.httpIpAddress = httpIpAddress;
            return this;
        }

        public Builder httpsIpAddress(String httpsIpAddress) {
            this.httpsIpAddress = httpsIpAddress;
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
        this.certificatesChange = b.certificatesChange;
        this.httpCheckChange = b.httpCheckChange;
        this.httpsCheckChange = b.httpsCheckChange;
        this.httpIpAddress = b.httpIpAddress;
        this.httpsIpAddress = b.httpsIpAddress;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DomainCheckEntity that = (DomainCheckEntity) o;
        return domain.equals(that.domain) &&
                timeCheckedOn.equals(that.timeCheckedOn) &&
                Objects.equals(httpResponseTimeNs, that.httpResponseTimeNs) &&
                Objects.equals(httpIpAddress, that.httpIpAddress) &&
                Objects.equals(httpsIpAddress, that.httpsIpAddress) &&
                Objects.equals(httpsResponseTimeNs, that.httpsResponseTimeNs);
    }

    @Override
    public int hashCode() {
        return Objects.hash(domain, httpIpAddress, httpsIpAddress, timeCheckedOn, httpResponseTimeNs, httpsResponseTimeNs);
    }
}
