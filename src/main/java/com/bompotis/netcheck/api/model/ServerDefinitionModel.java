package com.bompotis.netcheck.api.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.hateoas.RepresentationModel;

import jakarta.validation.constraints.NotNull;

/**
 * Created by Kyriakos Bompotis on 9/9/20.
 */
public class ServerDefinitionModel extends RepresentationModel<ServerDefinitionModel>  {

    @NotNull(message = "label is mandatory")
    private String label;

    @NotNull(message = "fieldName is mandatory")
    private String fieldName;

    private String suffix;

    @NotNull(message = "valueType is mandatory")
    private String valueType;

    @NotNull(message = "metricKind is mandatory")
    private String metricKind;

    private String  extendedType;

    private String minThreshold;

    private String maxThreshold;

    private Boolean notify;

    public ServerDefinitionModel() {}

    public ServerDefinitionModel(@JsonProperty("label") String label,
                                 @JsonProperty("fieldName") String fieldName,
                                 @JsonProperty("suffix") String suffix,
                                 @JsonProperty("valueType") String valueType,
                                 @JsonProperty("metricKind") String metricKind,
                                 @JsonProperty("extendedType") String extendedType,
                                 @JsonProperty("minThreshold") String minThreshold,
                                 @JsonProperty("maxThreshold") String maxThreshold,
                                 @JsonProperty("notify") Boolean notify) {
        this.label = label;
        this.fieldName = fieldName;
        this.suffix = suffix;
        this.valueType = valueType;
        this.metricKind = metricKind;
        this.extendedType = extendedType;
        this.minThreshold = minThreshold;
        this.maxThreshold = maxThreshold;
        this.notify = notify;
    }

    public String getLabel() {
        return label;
    }

    public String getFieldName() {
        return fieldName;
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public String getSuffix() {
        return suffix;
    }

    public String getValueType() {
        return valueType;
    }

    public String getMetricKind() {
        return metricKind;
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public String getExtendedType() {
        return extendedType;
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public String getMinThreshold() {
        return minThreshold;
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public String getMaxThreshold() {
        return maxThreshold;
    }

    public Boolean getNotify() {
        return notify;
    }
}
