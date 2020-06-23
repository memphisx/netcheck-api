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
    private final Date metricPeriod;
    private final Integer uptimePercentage;
    private final Long averageResponseTime;

    @JsonCreator
    public MetricModel(
            @JsonProperty("metricPeriod") Date metricPeriod,
            @JsonProperty("uptimePercentage") Integer uptimePercentage,
            @JsonProperty("averageResponseTime") Long averageResponseTime) {
        this.metricPeriod = metricPeriod;
        this.uptimePercentage = uptimePercentage;
        this.averageResponseTime = averageResponseTime;
    }

    public Long getAverageResponseTime() {
        return averageResponseTime;
    }

    public Integer getUptimePercentage() {
        return uptimePercentage;
    }

    public Date getMetricPeriod() {
        return metricPeriod;
    }
}
