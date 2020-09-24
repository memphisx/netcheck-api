package com.bompotis.netcheck.api.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.hateoas.RepresentationModel;
import org.springframework.hateoas.server.core.Relation;

import javax.validation.constraints.NotNull;
import java.util.Date;
import java.util.Map;

/**
 * Created by Kyriakos Bompotis on 6/9/20.
 */
@Relation(collectionRelation = "metrics", itemRelation = "metric")
public class ServerMetricModel extends RepresentationModel<HttpCheckModel> {

    private final String id;

    @NotNull(message = "metrics are mandatory")
    private final Map<String,String> metrics;

    @NotNull(message = "collectedAt Date is mandatory")
    private final Date collectedAt;

    public ServerMetricModel(
            @JsonProperty("id") String id,
            @JsonProperty("metrics") Map<String, String> metrics,
            @JsonProperty("collectedAt") Date collectedAt) {
        this.metrics = metrics;
        this.collectedAt = collectedAt;
        this.id = id;
    }

    public Map<String, String> getMetrics() {
        return metrics;
    }

    public Date getCollectedAt() {
        return collectedAt;
    }

    public String getId() {
        return id;
    }
}
