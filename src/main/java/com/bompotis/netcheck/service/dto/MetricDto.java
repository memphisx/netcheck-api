package com.bompotis.netcheck.service.dto;

import java.util.Date;

/**
 * Created by Kyriakos Bompotis on 22/6/20.
 */
public class MetricDto {
    private final Date metricPeriod;
    private final Integer uptimePercentage;
    private final Long averageResponseTime;

    public Long getAverageResponseTime() {
        return averageResponseTime;
    }

    public Integer getUptimePercentage() {
        return uptimePercentage;
    }

    public Date getMetricPeriod() {
        return metricPeriod;
    }

    public static class Builder {
        private Date metricPeriod;
        private Integer uptimePercentage;
        private Long averageResponseTime;

        public Builder metricPeriod(Date metricPeriod) {
            this.metricPeriod = metricPeriod;
            return this;
        }

        public Builder uptimePercentage(Integer uptimePercentage) {
            this.uptimePercentage = uptimePercentage;
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
        this.metricPeriod = b.metricPeriod;
        this.uptimePercentage = b.uptimePercentage;
        this.averageResponseTime = b.averageResponseTime;
    }
}
