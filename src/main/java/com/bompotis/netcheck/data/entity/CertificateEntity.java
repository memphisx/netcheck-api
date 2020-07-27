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
package com.bompotis.netcheck.data.entity;

import org.springframework.lang.NonNull;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.Transient;
import java.util.Date;
import java.util.Objects;

/**
 * Created by Kyriakos Bompotis on 14/6/20.
 */
@Entity
@Table(name = "certificate")
public class CertificateEntity extends AbstractTimestampablePersistable<String>{

    @NonNull
    @Column(name = "basic_constraints")
    private Integer basicConstraints;

    @NonNull
    @Column(name = "issued_by")
    private String issuedBy;

    @NonNull
    @Column(name = "issued_for")
    private String issuedFor;

    @NonNull
    @Column(name = "not_before")
    private Date notBefore;

    @NonNull
    @Column(name = "not_after")
    private Date notAfter;

    @NonNull
    @Column(name = "is_valid")
    private boolean isValid;

    @NonNull
    @Column(name = "expired")
    private boolean expired;

    @NonNull
    @Column(name = "not_yet_valid")
    private boolean notYetValid;

    @Transient
    public boolean isNew() {
        return null == this.getId();
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

    public Boolean isValid() {
        return isValid;
    }

    public Boolean getExpired() {
        return expired;
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
        private boolean isValid;
        private boolean expired;
        private boolean notYetValid;

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

        public Builder valid(boolean isValid) {
            this.isValid = isValid;
            return this;
        }

        public Builder expired(boolean expired) {
            this.expired = expired;
            return this;
        }

        public Builder notYetValid(boolean notYetValid) {
            this.notYetValid = notYetValid;
            return this;
        }

        public CertificateEntity build() {
            return new CertificateEntity(this);
        }
    }

    protected CertificateEntity() {}

    private CertificateEntity(Builder b) {
        this.basicConstraints = b.basicConstraints;
        this.issuedBy = b.issuedBy;
        this.issuedFor = b.issuedFor;
        this.notBefore = b.notBefore;
        this.notAfter = b.notAfter;
        this.isValid = b.isValid;
        this.expired = b.expired;
        this.notYetValid = b.notYetValid;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CertificateEntity that = (CertificateEntity) o;
        return isValid == that.isValid &&
                expired == that.expired &&
                notYetValid == that.notYetValid &&
                basicConstraints.equals(that.basicConstraints) &&
                issuedBy.equals(that.issuedBy) &&
                issuedFor.equals(that.issuedFor) &&
                notBefore.equals(that.notBefore) &&
                notAfter.equals(that.notAfter);
    }

    @Override
    public int hashCode() {
        return Objects.hash(basicConstraints, issuedBy, issuedFor, notBefore, notAfter, isValid, expired, notYetValid);
    }
}
