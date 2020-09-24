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

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import java.util.Optional;

/**
 * Created by Kyriakos Bompotis on 8/9/20.
 */
@Entity
@Table(name = "server_metric_definition", indexes = {
        @Index(name = "server_metric_definition__server_created_idx", columnList = "server_id, created_at")
})
public class ServerMetricDefinitionEntity extends AbstractTimestampable<String>{

    @NonNull
    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.PERSIST)
    @JoinColumn(name = "server_id")
    private ServerEntity serverEntity;

    @NonNull
    @Column(name = "label")
    private String label;

    @Id
    @Column(name = "field_name")
    private String fieldName;

    @Column(name = "suffix")
    private String suffix;

    @NonNull
    @Column(name = "value_type")
    @Enumerated(EnumType.STRING)
    private ValueType valueType;

    @NonNull
    @Column(name = "metric_kind")
    @Enumerated(EnumType.STRING)
    private MetricKind metricKind;

    @Column(name = "extended_type")
    @Enumerated(EnumType.STRING)
    private ExtendedType extendedType;

    @Column(name = "min_threshold")
    private String minThreshold;

    @Column(name = "max_threshold")
    private String maxThreshold;

    @NonNull
    @Column(name = "notify")
    private boolean notify;

    @NonNull
    public MetricKind getMetricKind() {
        return metricKind;
    }

    @NonNull
    public String getFieldName() {
        return fieldName;
    }

    @NonNull
    public String getLabel() {
        return label;
    }

    @Override
    public String getId()  {
        return fieldName;
    }

    @Override
    public boolean isNew() {
        return false;
    }

    public ExtendedType getExtendedType() {
        return extendedType;
    }

    public String getMinThreshold() {
        return minThreshold;
    }

    public String getMaxThreshold() {
        return maxThreshold;
    }

    @NonNull
    public boolean isNotify() {
        return notify;
    }

    public enum ValueType {
        BOOL,
        INT,
        DOUBLE,
        STRING
    }

    public enum ExtendedType {
        PERCENTAGE,
        DATETIME,
        TIMESTAMP,
        TEMPERATURE,
        DATE,
        BYTES,
        KILOBYTES,
        MEGABYTES,
        GIGABYTES
    }

    public enum MetricKind {
        GAUGE,
        DELTA,
        CUMULATIVE
    }

    @NonNull
    public ServerEntity getServerEntity() {
        return serverEntity;
    }

    public void setFieldName(String fieldName) {
        this.fieldName = fieldName;
    }

    public String getSuffix() {
        return suffix;
    }

    public ValueType getValueType() {
        return valueType;
    }

    protected ServerMetricDefinitionEntity() {}

    public static class Builder implements EntityBuilder<ServerMetricDefinitionEntity>{
        private ServerEntity serverEntity;
        private String label;
        private String fieldName;
        private String suffix;
        private ValueType valueType;
        private MetricKind metricKind;
        private ExtendedType extendedType;
        private String minThreshold;
        private String maxThreshold;
        private boolean notify;

        public Builder label(String label) {
            this.label = label;
            return this;
        }

        public Builder serverEntity(ServerEntity serverEntity) {
            this.serverEntity = serverEntity;
            return this;
        }

        public Builder suffix(String suffix) {
            this.suffix = suffix;
            return this;
        }

        public Builder fieldName(String fieldName) {
            this.fieldName = fieldName;
            return this;
        }

        public Builder extendedType(String extendedType) {
            this.extendedType = Optional.ofNullable(extendedType)
                    .map(type -> ExtendedType.valueOf(type.toUpperCase()))
                    .orElse(null);
            return this;
        }

        public Builder minThreshold(String minThreshold) {
            this.minThreshold = minThreshold;
            return this;
        }

        public Builder notify(Boolean notify) {
            this.notify = Optional.ofNullable(notify).orElse(false);
            return this;
        }

        public Builder maxThreshold(String maxThreshold) {
            this.maxThreshold = maxThreshold;
            return this;
        }

        public Builder valueType(String valueType) {
            this.valueType = ValueType.valueOf(valueType.toUpperCase());
            return this;
        }

        public Builder metricKind(String metricKind) {
            this.metricKind = MetricKind.valueOf(metricKind.toUpperCase());
            return this;
        }

        public ServerMetricDefinitionEntity build() {
            return new ServerMetricDefinitionEntity(this);
        }
    }

    private ServerMetricDefinitionEntity(Builder b) {
        this.serverEntity = b.serverEntity;
        this.label = b.label;
        this.fieldName = b.fieldName;
        this.valueType = b.valueType;
        this.suffix = b.suffix;
        this.metricKind = b.metricKind;
        this.extendedType = b.extendedType;
        this.notify = b.notify;
        this.maxThreshold = b.maxThreshold;
        this.minThreshold = b.minThreshold;
    }
}
