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
import javax.persistence.Index;
import javax.persistence.Column;
import javax.persistence.Enumerated;
import javax.persistence.EnumType;
import javax.persistence.ManyToOne;
import javax.persistence.FetchType;
import javax.persistence.CascadeType;
import javax.persistence.JoinColumn;

import java.util.Date;
import java.util.Objects;

/**
 * Created by Kyriakos Bompotis on 29/6/20.
 */
@Entity
@Table(name = "domain_metric",
        indexes = {
                @Index(name = "domain_metric__domain_period_protocol_idx", columnList = "domain,period_type,protocol"),
                @Index(name = "domain_metric__domain_period_created_idx", columnList = "domain,period_type,created_at")
})
public class DomainMetricEntity extends AbstractTimestampablePersistable<String>{

    public enum Protocol {
        HTTP,
        HTTPS
    }

    public enum Period{
        HOUR,
        DAY,
        WEEK,
        MONTH
    }

    @NonNull
    @Column(name = "domain", insertable = false, updatable = false)
    private String domain;

    @NonNull
    @Enumerated(EnumType.STRING)
    @Column(name = "protocol")
    private Protocol protocol;

    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.PERSIST)
    @JoinColumn(name = "domain")
    private DomainEntity domainEntity;

    @Column(name = "min_response_time_ns")
    private Long minResponseTimeNs;

    @Column(name = "max_response_time_ns")
    private Long maxResponseTimeNs;

    @Column(name = "avg_response_time_ns")
    private Long avgResponseTimeNs;

    @Column(name = "total_checks")
    private int totalChecks;

    @Column(name = "successful_checks")
    private int successfulChecks;

    @Column(name = "start_period")
    private Date startPeriod;

    @Column(name = "end_period")
    private Date endPeriod;

    @NonNull
    @Enumerated(EnumType.STRING)
    @Column(name = "period_type")
    private Period periodType;


    public DomainEntity getDomainEntity() {
        return domainEntity;
    }

    public Long getMinResponseTimeNs() {
        return minResponseTimeNs;
    }

    public Long getMaxResponseTimeNs() {
        return maxResponseTimeNs;
    }

    public Long getAvgResponseTimeNs() {
        return avgResponseTimeNs;
    }

    public Date getStartPeriod() {
        return startPeriod;
    }

    public Date getEndPeriod() {
        return endPeriod;
    }

    @NonNull
    public Period getPeriodType() {
        return periodType;
    }

    @NonNull
    public String getDomain() {
        return domain;
    }

    @NonNull
    public Protocol getProtocol() {
        return protocol;
    }

    public Integer getSuccessfulChecks() {
        return successfulChecks;
    }

    public Integer getTotalChecks() {
        return totalChecks;
    }

    public static class Builder {
        private DomainEntity domainEntity;
        private String domain;
        private Long minResponseTimeNs;
        private Long maxResponseTimeNs;
        private Long avgResponseTimeNs;
        private Integer totalChecks;
        private Integer successfulChecks;
        private Date startPeriod;
        private Date endPeriod;
        private Period periodType;
        private Protocol protocol;

        public Builder domain(String domain) {
            this.domain = domain;
            return this;
        }

        public Builder domainEntity(DomainEntity domainEntity) {
            this.domainEntity = domainEntity;
            return this;
        }

        public Builder protocol(String protocol) {
            this.protocol = Protocol.valueOf(protocol);
            return this;
        }

        public Builder minResponseTimeNs(Long minResponseTimeNs) {
            this.minResponseTimeNs = minResponseTimeNs;
            return this;
        }

        public Builder maxResponseTimeNs(Long maxResponseTimeNs) {
            this.maxResponseTimeNs = maxResponseTimeNs;
            return this;
        }

        public Builder avgResponseTimeNs(Long avgResponseTimeNs) {
            this.avgResponseTimeNs = avgResponseTimeNs;
            return this;
        }

        public Builder totalChecks(Integer totalChecks) {
            this.totalChecks = totalChecks;
            return this;
        }

        public Builder successfulChecks(Integer successfulChecks) {
            this.successfulChecks = successfulChecks;
            return this;
        }

        public Builder startPeriod(Date startPeriod) {
            this.startPeriod = startPeriod;
            return this;
        }

        public Builder endPeriod(Date endPeriod) {
            this.endPeriod = endPeriod;
            return this;
        }

        public Builder periodType(Period periodType) {
            this.periodType = periodType;
            return this;
        }

        public DomainMetricEntity build() {
            return new DomainMetricEntity(this);
        }
    }

    protected DomainMetricEntity() {}

    private DomainMetricEntity(Builder b) {
        this.domainEntity = b.domainEntity;
        this.minResponseTimeNs = b.minResponseTimeNs;
        this.maxResponseTimeNs = b.maxResponseTimeNs;
        this.avgResponseTimeNs = b.avgResponseTimeNs;
        this.successfulChecks = b.successfulChecks;
        this.totalChecks = b.totalChecks;
        this.startPeriod = b.startPeriod;
        this.endPeriod = b.endPeriod;
        this.periodType = b.periodType;
        this.domain = b.domain;
        this.protocol = b.protocol;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DomainMetricEntity that = (DomainMetricEntity) o;
        return Objects.equals(domainEntity, that.domainEntity) &&
                Objects.equals(minResponseTimeNs, that.minResponseTimeNs) &&
                Objects.equals(maxResponseTimeNs, that.maxResponseTimeNs) &&
                Objects.equals(avgResponseTimeNs, that.avgResponseTimeNs) &&
                Objects.equals(totalChecks, that.totalChecks) &&
                Objects.equals(successfulChecks, that.successfulChecks) &&
                Objects.equals(protocol, that.protocol) &&
                startPeriod.equals(that.startPeriod) &&
                endPeriod.equals(that.endPeriod) &&
                periodType == that.periodType;
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                domainEntity,
                minResponseTimeNs,
                maxResponseTimeNs,
                avgResponseTimeNs,
                totalChecks,
                successfulChecks,
                protocol,
                startPeriod,
                endPeriod,
                periodType
        );
    }
}
