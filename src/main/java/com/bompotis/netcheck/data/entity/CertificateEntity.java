package com.bompotis.netcheck.data.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.Transient;
import java.util.Date;
import java.util.Set;

/**
 * Created by Kyriakos Bompotis on 14/6/20.
 */
@Entity
@Table(name = "certificate")
public class CertificateEntity extends AbstractTimestampablePersistable<String>{

    @Column(name = "valid_certificate")
    private Boolean certificateIsValid;

    @Column(name = "basic_constraints")
    private Integer basicConstraints;

    @Column(name = "issued_by")
    private String issuedBy;

    @Column(name = "issued_for")
    private String issuedFor;

    @Column(name = "not_before")
    private Date notBefore;

    @Column(name = "not_after")
    private Date notAfter;

    @Column(name = "is_valid")
    private Boolean isValid;

    @Column(name = "expired")
    private Boolean expired;

    @Column(name = "not_yet_valid")
    private Boolean notYetValid;

    @Transient
    public boolean isNew() {
        return null == this.getId();
    }

    public Boolean getCertificateIsValid() {
        return certificateIsValid;
    }

    public Integer getBasicConstraints() {
        return basicConstraints;
    }

    public String getIssuedBy() {
        return issuedBy;
    }

    public String getIssuedFor() {
        return issuedFor;
    }

    public Date getNotBefore() {
        return notBefore;
    }

    public Date getNotAfter() {
        return notAfter;
    }

    public Boolean getValid() {
        return isValid;
    }

    public Boolean getExpired() {
        return expired;
    }

    public Boolean getNotYetValid() {
        return notYetValid;
    }

    public static class Builder {
        private Boolean certificateIsValid;
        private Integer basicConstraints;
        private String issuedBy;
        private String issuedFor;
        private Date notBefore;
        private Date notAfter;
        private Boolean isValid;
        private Boolean expired;
        private Boolean notYetValid;

        public Builder certificateIsValid(Boolean certificateIsValid) {
            this.certificateIsValid = certificateIsValid;
            return this;
        }

        public Builder basicConstraints(Integer basicConstraints) {
            this.basicConstraints = basicConstraints;
            return this;
        }

        public Builder issuedBy(String issuedBy) {
            this.issuedBy = issuedBy;
            return this;
        }

        public Builder issuedFor(String issuedFor) {
            this.issuedFor = issuedFor;
            return this;
        }

        public Builder notBefore(Date notBefore) {
            this.notBefore = notBefore;
            return this;
        }

        public Builder notAfter(Date notAfter) {
            this.notAfter = notAfter;
            return this;
        }

        public Builder valid(Boolean isValid) {
            this.isValid = isValid;
            return this;
        }

        public Builder expired(Boolean expired) {
            this.expired = expired;
            return this;
        }

        public Builder notYetValid(Boolean notYetValid) {
            this.notYetValid = notYetValid;
            return this;
        }

        public CertificateEntity build() {
            return new CertificateEntity(this);
        }
    }

    protected CertificateEntity() {}

    private CertificateEntity(Builder b) {
        this.certificateIsValid = b.certificateIsValid;
        this.basicConstraints = b.basicConstraints;
        this.issuedBy = b.issuedBy;
        this.issuedFor = b.issuedFor;
        this.notBefore = b.notBefore;
        this.notAfter = b.notAfter;
        this.isValid = b.isValid;
        this.expired = b.expired;
        this.notYetValid = b.notYetValid;
    }
}
