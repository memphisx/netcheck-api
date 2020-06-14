package com.bompotis.netcheck.service.dto;

import java.security.cert.CertificateExpiredException;
import java.security.cert.CertificateNotYetValidException;
import java.security.cert.X509Certificate;
import java.util.Date;

/**
 * Created by Kyriakos Bompotis on 4/6/20.
 */
public class CertificateDetailsDto {
    private final Integer basicConstraints;
    private final String issuedBy;
    private final String issuedFor;
    private final Date notBefore;
    private final Date notAfter;
    private Boolean isValid;
    private Boolean expired;

    public CertificateDetailsDto(X509Certificate certificate) {
        this.basicConstraints = certificate.getBasicConstraints();
        this.issuedFor = certificate.getSubjectX500Principal().getName();
        this.issuedBy = certificate.getIssuerX500Principal().getName();
        this.notBefore = certificate.getNotBefore();
        this.notAfter = certificate.getNotAfter();
        try {
            certificate.checkValidity();
            this.isValid = true;
            this.expired = false;
        } catch (CertificateExpiredException e) {
            this.isValid = false;
            this.expired = true;
        } catch (CertificateNotYetValidException e) {
            this.isValid = false;
            this.expired = false;
        }
    }

    public Date getNotBefore() {
        return notBefore;
    }

    public Date getNotAfter() {
        return notAfter;
    }

    public boolean isValid() {
        return isValid;
    }

    public Boolean getExpired() {
        return expired;
    }

    public String getIssuedBy() {
        return issuedBy;
    }

    public Integer getBasicConstraints() {
        return basicConstraints;
    }

    public String getIssuedFor() {
        return issuedFor;
    }
}
