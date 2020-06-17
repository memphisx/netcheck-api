package com.bompotis.netcheck.data.entities;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.Transient;
import java.util.Date;

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

    public void setCertificateIsValid(Boolean certificateIsValid) {
        this.certificateIsValid = certificateIsValid;
    }

    public Integer getBasicConstraints() {
        return basicConstraints;
    }

    public void setBasicConstraints(Integer basicConstraints) {
        this.basicConstraints = basicConstraints;
    }

    public String getIssuedBy() {
        return issuedBy;
    }

    public void setIssuedBy(String issuedBy) {
        this.issuedBy = issuedBy;
    }

    public String getIssuedFor() {
        return issuedFor;
    }

    public void setIssuedFor(String issuedFor) {
        this.issuedFor = issuedFor;
    }

    public Date getNotBefore() {
        return notBefore;
    }

    public void setNotBefore(Date notBefore) {
        this.notBefore = notBefore;
    }

    public Date getNotAfter() {
        return notAfter;
    }

    public void setNotAfter(Date notAfter) {
        this.notAfter = notAfter;
    }

    public Boolean getValid() {
        return isValid;
    }

    public void setValid(Boolean valid) {
        isValid = valid;
    }

    public Boolean getExpired() {
        return expired;
    }

    public void setExpired(Boolean expired) {
        this.expired = expired;
    }

    public Boolean getNotYetValid() {
        return notYetValid;
    }

    public void setNotYetValid(Boolean notYetValid) {
        this.notYetValid = notYetValid;
    }
}
