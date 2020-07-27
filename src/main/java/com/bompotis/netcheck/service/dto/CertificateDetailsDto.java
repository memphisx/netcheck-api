/*
 * Copyright 2020 the original author or authors.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This code is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package com.bompotis.netcheck.service.dto;

import com.bompotis.netcheck.data.entity.CertificateEntity;

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
    private final Boolean isValid;
    private final Boolean expired;
    private final Boolean notYetValid;

    public CertificateDetailsDto(X509Certificate certificate) {
        this.basicConstraints = certificate.getBasicConstraints();
        this.issuedFor = certificate.getSubjectX500Principal().getName();
        this.issuedBy = certificate.getIssuerX500Principal().getName();
        this.notBefore = certificate.getNotBefore();
        this.notAfter = certificate.getNotAfter();
        var expectedIsValidValue = false;
        var expectedExpiredValue = false;
        var expectedNotYetValidValue = false;
        try {
            certificate.checkValidity();
            expectedIsValidValue = true;
        } catch (CertificateExpiredException e) {
            expectedExpiredValue = true;
        } catch (CertificateNotYetValidException e) {
            expectedNotYetValidValue = true;
        }
        this.isValid = expectedIsValidValue;
        this.expired = expectedExpiredValue;
        this.notYetValid = expectedNotYetValidValue;
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

    public Boolean getNotYetValid() {
        return notYetValid;
    }

    public static class Builder {
        private Integer basicConstraints;
        private String issuedBy;
        private String issuedFor;
        private Date notBefore;
        private Date notAfter;
        private Boolean isValid;
        private Boolean expired;
        private Boolean notYetValid;

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

        public Builder valid(Boolean valid) {
            isValid = valid;
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

        public CertificateDetailsDto build() {
            return new CertificateDetailsDto(this);
        }
    }

    private CertificateDetailsDto(CertificateDetailsDto.Builder b) {
        this.basicConstraints = b.basicConstraints;
        this.issuedFor = b.issuedFor;
        this.issuedBy = b.issuedBy;
        this.notBefore = b.notBefore;
        this.notAfter = b.notAfter;
        this.isValid = b.isValid;
        this.expired = b.expired;
        this.notYetValid = b.notYetValid;
    }

    public CertificateDetailsDto(CertificateEntity entity) {
        this.isValid = entity.isValid();
        this.notAfter = entity.getNotAfter();
        this.notBefore = entity.getNotBefore();
        this.expired = entity.getExpired();
        this.basicConstraints = entity.getBasicConstraints();
        this.issuedBy = entity.getIssuedBy();
        this.issuedFor = entity.getIssuedFor();
        this.notYetValid = entity.getNotYetValid();
    }

    public CertificateEntity toCertificateEntity() {
        return new CertificateEntity.Builder()
                .basicConstraints(this.getBasicConstraints())
                .valid(this.isValid())
                .expired(this.getExpired())
                .notYetValid(this.getNotYetValid())
                .valid(this.isValid())
                .issuedBy(this.getIssuedBy())
                .issuedFor(this.getIssuedFor())
                .notAfter(this.getNotAfter())
                .notBefore(this.getNotBefore())
                .build();
    }
}
