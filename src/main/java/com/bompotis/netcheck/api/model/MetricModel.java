package com.bompotis.netcheck.api.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.hateoas.RepresentationModel;
import org.springframework.hateoas.server.core.Relation;

import java.util.Date;

/**
 * Created by Kyriakos Bompotis on 22/6/20.
 */
@Relation(collectionRelation = "metrics", itemRelation = "metric")
public class MetricModel extends RepresentationModel<HttpCheckModel> {
    private final Date metricPeriodStart;
    private final Date metricPeriodEnd;
    private final Integer totalChecks;
    private final Integer successfulChecks;
    private final Long averageResponseTime;
    private final Long maxResponseTime;
    private final Long minResponseTime;
    private final String protocol;

    @JsonCreator
    public MetricModel(
            @JsonProperty("metricPeriodStart") Date metricPeriodStart,
            @JsonProperty("metricPeriodEnd") Date metricPeriodEnd,
            @JsonProperty("totalChecks") Integer totalChecks,
            @JsonProperty("successfulChecks") Integer successfulChecks,
            @JsonProperty("averageResponseTime") Long averageResponseTime,
            @JsonProperty("maxResponseTime") Long maxResponseTime,
            @JsonProperty("minResponseTime") Long minResponseTime,
            @JsonProperty("protocol") String protocol) {
        this.metricPeriodStart = metricPeriodStart;
        this.metricPeriodEnd = metricPeriodEnd;
        this.totalChecks = totalChecks;
        this.successfulChecks = successfulChecks;
        this.averageResponseTime = averageResponseTime;
        this.maxResponseTime = maxResponseTime;
        this.minResponseTime = minResponseTime;
        this.protocol = protocol;
    }

    public Date getMetricPeriodStart() {
        return metricPeriodStart;
    }

    public Date getMetricPeriodEnd() {
        return metricPeriodEnd;
    }

    public Integer getTotalChecks() {
        return totalChecks;
    }

    public Integer getSuccessfulChecks() {
        return successfulChecks;
    }

    public Long getAverageResponseTime() {
        return averageResponseTime;
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
}
