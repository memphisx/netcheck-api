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

import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.Id;
import javax.persistence.Column;
import javax.persistence.OneToMany;
import javax.persistence.FetchType;
import javax.persistence.CascadeType;

import java.util.Objects;
import java.util.Set;

/**
 * Created by Kyriakos Bompotis on 8/6/20.
 */
@Entity
@Table(name = "domain")
public class DomainEntity extends AbstractTimestampable<String>{

    @Id
    @Column(name = "domain")
    private String domain;

    @NonNull
    @Column(name = "check_frequency_minutes")
    private int checkFrequency;

    @OneToMany(fetch = FetchType.LAZY, mappedBy="domainEntity", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<DomainCheckEntity> domainHistoryEntries;

    protected DomainEntity() {}

    public Set<DomainCheckEntity> getDomainHistoryEntries() {
        return domainHistoryEntries;
    }

    public String getDomain() {
        return domain;
    }

    public int getCheckFrequency() {
        return checkFrequency;
    }

    @Override
    public String getId()  {
        return domain;
    }

    @Override
    public boolean isNew() {
        return null == domain;
    }

    public static class Builder {
        private String domain;
        private Set<DomainCheckEntity> domainHistoryEntries;
        private int frequency = 10;

        public Builder domainHistoryEntries(Set<DomainCheckEntity> domainHistoryEntries) {
            this.domainHistoryEntries = domainHistoryEntries;
            return this;
        }

        public Builder frequency(int frequencyMinutes) {
            this.frequency = frequencyMinutes;
            return this;
        }

        public Builder domain(String domain) {
            this.domain = domain;
            return this;
        }

        public DomainEntity build() {
            return new DomainEntity(this);
        }
    }

    private DomainEntity(Builder b) {
        this.domain = b.domain;
        this.domainHistoryEntries = b.domainHistoryEntries;
        this.checkFrequency = b.frequency;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DomainEntity that = (DomainEntity) o;
        return domain.equals(that.domain);
    }

    @Override
    public int hashCode() {
        return Objects.hash(domain);
    }
}
