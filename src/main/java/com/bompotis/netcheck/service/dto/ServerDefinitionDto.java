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

/**
 * Created by Kyriakos Bompotis on 9/9/20.
 */
public class ServerDefinitionDto {
    private final String label;
    private final String fieldName;
    private final String suffix;
    private final String valueType;
    private final String metricKind;
    private final String extendedType;
    private final String minThreshold;
    private final String maxThreshold;
    private final Boolean notify;

    public String getLabel() {
        return label;
    }

    public String getFieldName() {
        return fieldName;
    }

    public String getSuffix() {
        return suffix;
    }

    public String getValueType() {
        return valueType;
    }

    public String getMetricKind() {
        return metricKind;
    }

    public String getExtendedType() {
        return extendedType;
    }

    public String getMinThreshold() {
        return minThreshold;
    }

    public String getMaxThreshold() {
        return maxThreshold;
    }

    public Boolean getNotify() {
        return notify;
    }

    public static class Builder implements DtoBuilder<ServerDefinitionDto> {
        private String label;
        private String fieldName;
        private String suffix;
        private String valueType;
        private String metricKind;
        private String extendedType;
        private String minThreshold;
        private String maxThreshold;
        private Boolean notify;

        public Builder label(String label) {
            this.label = label;
            return this;
        }

        public Builder fieldName(String fieldName) {
            this.fieldName = fieldName;
            return this;
        }

        public Builder suffix(String suffix) {
            this.suffix = suffix;
            return this;
        }

        public Builder valueType(String valueType) {
            this.valueType = valueType;
            return this;
        }

        public Builder metricKind(String metricKind) {
            this.metricKind = metricKind;
            return this;
        }

        public Builder extendedType(String extendedType) {
            this.extendedType = extendedType;
            return this;
        }

        public Builder minThreshold(String minThreshold) {
            this.minThreshold = minThreshold;
            return this;
        }

        public Builder maxThreshold(String maxThreshold) {
            this.maxThreshold = maxThreshold;
            return this;
        }

        public Builder notify(Boolean notify) {
            this.notify = notify;
            return this;
        }

        @Override
        public ServerDefinitionDto build() {
            return new ServerDefinitionDto(this);
        }
    }

    private ServerDefinitionDto(Builder builder) {
        this.label = builder.label;
        this.fieldName = builder.fieldName;
        this.suffix = builder.suffix;
        this.valueType = builder.valueType;
        this.metricKind = builder.metricKind;
        this.extendedType = builder.extendedType;
        this.minThreshold = builder.minThreshold;
        this.maxThreshold = builder.maxThreshold;
        this.notify = builder.notify;
    }
}
