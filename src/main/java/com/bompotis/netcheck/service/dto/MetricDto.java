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

import java.util.Date;

/**
 * Created by Kyriakos Bompotis on 22/6/20.
 */
public class MetricDto {
    private final Date metricPeriodStart;
    private final Date metricPeriodEnd;
    private final Integer totalChecks;
    private final Integer successfulChecks;
    private final Long averageResponseTime;
    private final Long maxResponseTime;
    private final Long minResponseTime;
    private final String protocol;

    public Long getAverageResponseTime() {
        return averageResponseTime;
    }

    public Date getMetricPeriodStart() {
        return metricPeriodStart;
    }

    public Date getMetricPeriodEnd() {
        return metricPeriodEnd;
    }

    public Long getMaxResponseTime() {
        return maxResponseTime;
    }

    public Long getMinResponseTime() {
        return minResponseTime;
    }

    public String getProtocol() {
        return protocol;
    }

    public Integer getTotalChecks() {
        return totalChecks;
    }

    public Integer getSuccessfulChecks() {
        return successfulChecks;
    }

    public static class Builder implements DtoBuilder<MetricDto>{
        private Date metricPeriodStart;
        private Date metricPeriodEnd;
        private Integer totalChecks;
        private Integer successfulChecks;
        private Long averageResponseTime;
        private Long maxResponseTime;
        private Long minResponseTime;
        private String protocol;

        public Builder metricPeriodStart(Date metricPeriodStart) {
            this.metricPeriodStart = metricPeriodStart;
            return this;
        }

        public Builder metricPeriodEnd(Date metricPeriodEnd) {
            this.metricPeriodEnd = metricPeriodEnd;
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

        public Builder maxResponseTime(Long maxResponseTime) {
            this.maxResponseTime = maxResponseTime;
            return this;
        }

        public Builder minResponseTime(Long minResponseTime) {
            this.minResponseTime = minResponseTime;
            return this;
        }

        public Builder protocol(String protocol) {
            this.protocol = protocol;
            return this;
        }

        public Builder averageResponseTime(Long averageResponseTime) {
            this.averageResponseTime = averageResponseTime;
            return this;
        }

        public MetricDto build() {
            return new MetricDto(this);
        }
    }

    private MetricDto(Builder b) {
        this.metricPeriodStart = b.metricPeriodStart;
        this.metricPeriodEnd = b.metricPeriodEnd;
        this.totalChecks = b.totalChecks;
        this.successfulChecks = b.successfulChecks;
        this.averageResponseTime = b.averageResponseTime;
        this.maxResponseTime = b.maxResponseTime;
        this.minResponseTime = b.minResponseTime;
        this.protocol = b.protocol;
    }
}
