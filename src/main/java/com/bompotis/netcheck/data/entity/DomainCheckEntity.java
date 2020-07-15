package com.bompotis.netcheck.data.entity;

import org.springframework.lang.NonNull;

import javax.persistence.*;
import java.util.Date;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Created by Kyriakos Bompotis on 8/6/20.
 */
@Entity
@Table(name = "domain_check",
        indexes = {
                @Index(name = "domain_check__domain_change_type_idx", columnList = "domain,change_type"),
                @Index(name = "domain_check__domain_created_idx", columnList = "domain,created_at"),
                @Index(name = "domain_check__domain_check_date_idx", columnList = "domain,check_date")
})
public class DomainCheckEntity extends AbstractTimestampablePersistable<String>{

    public enum ChangeType {
        NO_CHANGE,
        FIRST_CHECK,
        HTTP_CHANGE,
        HTTPS_CHANGE,
        CERTS_CHANGE,
        HTTP_HTTPS_CHANGE,
        HTTP_CERTS_CHANGE,
        HTTP_HTTPS_CERTS_CHANGE,
        HTTPS_CERTS_CHANGE
    }

    @ManyToMany(fetch = FetchType.LAZY, cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinTable(
            name = "check_protocol",
            joinColumns = @JoinColumn(name = "domain_check_id"),
            inverseJoinColumns = @JoinColumn(name = "protocol_check_id"))
    private Set<ProtocolCheckEntity> protocolCheckEntities;

    @ManyToMany(fetch = FetchType.LAZY, cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinTable(
            name = "check_certificate",
            joinColumns = @JoinColumn(name = "domain_check_id"),
            inverseJoinColumns = @JoinColumn(name = "certificate_id"))
    private Set<CertificateEntity> certificateEntities;

    @ManyToMany(fetch = FetchType.LAZY, cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinTable(
            name = "previous_check_certificate",
            joinColumns = @JoinColumn(name = "domain_check_id"),
            inverseJoinColumns = @JoinColumn(name = "certificate_id"))
    private Set<CertificateEntity> previousCertificateEntities;

    @ManyToMany(fetch = FetchType.LAZY, cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinTable(
            name = "previous_check_protocol",
            joinColumns = @JoinColumn(name = "domain_check_id"),
            inverseJoinColumns = @JoinColumn(name = "protocol_check_id"))
    private Set<ProtocolCheckEntity> previousProtocolCheckEntities;

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

    @Enumerated(EnumType.STRING)
    @Column(name = "change_type")
    private ChangeType changeType;

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


    public Set<ProtocolCheckEntity> getPreviousProtocolCheckEntities() {
        return previousProtocolCheckEntities;
    }

    public Set<CertificateEntity> getPreviousCertificateEntities() {
        return previousCertificateEntities;
    }

    public String getChangeType() {
        return changeType.name();
    }

    public String getHttpIpAddress() {
        return httpIpAddress;
    }

    public String getHttpsIpAddress() {
        return httpsIpAddress;
    }

    public boolean isCertificatesChange() {
        return this.changeType.equals(ChangeType.CERTS_CHANGE) ||
                this.changeType.equals(ChangeType.HTTP_CERTS_CHANGE) ||
                this.changeType.equals(ChangeType.HTTPS_CERTS_CHANGE) ||
                this.changeType.equals(ChangeType.HTTP_HTTPS_CERTS_CHANGE);
    }

    public boolean isHttpCheckChange() {
        return this.changeType.equals(ChangeType.HTTP_CHANGE) ||
                this.changeType.equals(ChangeType.HTTP_CERTS_CHANGE) ||
                this.changeType.equals(ChangeType.HTTP_HTTPS_CHANGE) ||
                this.changeType.equals(ChangeType.HTTP_HTTPS_CERTS_CHANGE);
    }

    public boolean isHttpsCheckChange() {
        return this.changeType.equals(ChangeType.HTTPS_CHANGE) ||
                this.changeType.equals(ChangeType.HTTPS_CERTS_CHANGE) ||
                this.changeType.equals(ChangeType.HTTP_HTTPS_CHANGE) ||
                this.changeType.equals(ChangeType.HTTP_HTTPS_CERTS_CHANGE);
    }

    public static class Builder {
        private Set<ProtocolCheckEntity> protocolCheckEntities;
        private Set<CertificateEntity> certificateEntities;
        private Set<ProtocolCheckEntity> previousProtocolCheckEntities;
        private Set<CertificateEntity> previousCertificateEntities;
        private String domain;
        private ChangeType changeType;
        private DomainEntity domainEntity;
        private Date timeCheckedOn;
        private Long httpResponseTimeNs;
        private Long httpsResponseTimeNs;
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

        public Builder previousProtocolCheckEntities(Set<ProtocolCheckEntity> previousProtocolCheckEntities) {
            this.previousProtocolCheckEntities = previousProtocolCheckEntities;
            return this;
        }

        public Builder previousCertificateEntities(Set<CertificateEntity> previousCertificateEntities) {
            this.previousCertificateEntities = previousCertificateEntities;
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

        public Builder httpIpAddress(String httpIpAddress) {
            this.httpIpAddress = httpIpAddress;
            return this;
        }

        public Builder httpsIpAddress(String httpsIpAddress) {
            this.httpsIpAddress = httpsIpAddress;
            return this;
        }

        public DomainCheckEntity build() {
            setChangeType();
            return new DomainCheckEntity(this);
        }

        private void setChangeType() {
            if (Optional.ofNullable(previousProtocolCheckEntities).orElse(Set.of()).isEmpty() &&
                    Optional.ofNullable(previousCertificateEntities).orElse(Set.of()).isEmpty()) {
                this.changeType = ChangeType.FIRST_CHECK;
            } else {
                boolean protocolChecksAreTheSame = protocolCheckEntities.equals(previousProtocolCheckEntities);
                boolean certificatesAreTheSame = certificateEntities.equals(previousCertificateEntities);
                if (protocolChecksAreTheSame && certificatesAreTheSame) {
                    this.changeType = ChangeType.NO_CHANGE;
                } else if (protocolChecksAreTheSame) {
                    this.changeType = ChangeType.CERTS_CHANGE;
                } else {
                    boolean httpProtocolChecksAreTheSame = false;
                    boolean httpsProtocolChecksAreTheSame = false;
                    var protocolCheckMap = protocolCheckEntities
                            .stream()
                            .collect(Collectors.toMap(ProtocolCheckEntity::getProtocol, protocolCheck -> protocolCheck, (a, b) -> b));
                    assert protocolCheckEntities.size() == 2;
                    for (var protocolCheck : previousProtocolCheckEntities) {
                        if (protocolCheck.getProtocol().equals(ProtocolCheckEntity.Protocol.HTTP)) {
                            httpProtocolChecksAreTheSame = protocolCheck.equals(protocolCheckMap.get(protocolCheck.getProtocol()));
                        } else if (protocolCheck.getProtocol().equals(ProtocolCheckEntity.Protocol.HTTPS)) {
                            httpsProtocolChecksAreTheSame = protocolCheck.equals(protocolCheckMap.get(protocolCheck.getProtocol()));
                        }
                        else throw new IllegalArgumentException("Invalid Protocol: " + protocolCheck.getProtocol());
                    }

                    if (!httpProtocolChecksAreTheSame && !httpsProtocolChecksAreTheSame && !certificatesAreTheSame) {
                        this.changeType = ChangeType.HTTP_HTTPS_CERTS_CHANGE;
                    } else if (!httpProtocolChecksAreTheSame && httpsProtocolChecksAreTheSame && !certificatesAreTheSame) {
                        this.changeType = ChangeType.HTTP_CERTS_CHANGE;
                    } else if (httpProtocolChecksAreTheSame && httpsProtocolChecksAreTheSame && !certificatesAreTheSame) {
                        this.changeType = ChangeType.HTTPS_CERTS_CHANGE;
                    } else if (!httpProtocolChecksAreTheSame && !httpsProtocolChecksAreTheSame) {
                        this.changeType = ChangeType.HTTP_HTTPS_CHANGE;
                    } else if (!httpProtocolChecksAreTheSame) {
                        this.changeType = ChangeType.HTTP_CHANGE;
                    } else {
                        this.changeType = ChangeType.HTTPS_CHANGE;
                    }
                }
            }
        }
    }

    private DomainCheckEntity(Builder b) {
        this.protocolCheckEntities = b.protocolCheckEntities;
        this.certificateEntities = b.certificateEntities;
        this.previousProtocolCheckEntities = b.previousProtocolCheckEntities;
        this.previousCertificateEntities = b.previousCertificateEntities;
        this.domain = b.domain;
        this.domainEntity = b.domainEntity;
        this.timeCheckedOn = b.timeCheckedOn;
        this.httpResponseTimeNs = b.httpResponseTimeNs;
        this.httpsResponseTimeNs = b.httpsResponseTimeNs;
        this.changeType = b.changeType;
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
                Objects.equals(changeType, that.changeType) &&
                Objects.equals(httpIpAddress, that.httpIpAddress) &&
                Objects.equals(httpsIpAddress, that.httpsIpAddress) &&
                Objects.equals(httpsResponseTimeNs, that.httpsResponseTimeNs);
    }

    @Override
    public int hashCode() {
        return Objects.hash(domain, httpIpAddress, httpsIpAddress, changeType, timeCheckedOn, httpResponseTimeNs, httpsResponseTimeNs);
    }
}
