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

    @OneToMany(targetEntity = HttpsCheckEntity.class, fetch = FetchType.LAZY, mappedBy="issuerCertificate", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<HttpsCheckEntity> httpsCheckEntities;

    @Column(name = "valid_certificate")
    private Boolean certificateIsValid;

    @Column(name = "cert_expiration_date")
    private Date certificateExpiresOn;

    public void setId(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
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

    public List<HttpsCheckEntity> getHttpsCheckEntities() {
        return httpsCheckEntities;
    }

    public void setHttpsCheckEntities(List<HttpsCheckEntity> httpsCheckEntities) {
        this.httpsCheckEntities = httpsCheckEntities;
    }
}
