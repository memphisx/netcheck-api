package com.bompotis.netcheck.data.entities;

import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.util.Date;
import java.util.List;

/**
 * Created by Kyriakos Bompotis on 14/6/20.
 */
@Entity
@Table(name = "domain_certificate")
public class DomainCertificateEntity {

    @Id
    @GeneratedValue(generator="uuid")
    @GenericGenerator(name="uuid", strategy = "uuid2")
    @Column(name = "id")
    private String id;

    @OneToMany(fetch = FetchType.LAZY, mappedBy="issuerCertificate", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<HttpsCheckEntity> httpsCheckEntities;

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

    public List<HttpsCheckEntity> getHttpsCheckEntities() {
        return httpsCheckEntities;
    }

    public void setHttpsCheckEntities(List<HttpsCheckEntity> httpsCheckEntities) {
        this.httpsCheckEntities = httpsCheckEntities;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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
