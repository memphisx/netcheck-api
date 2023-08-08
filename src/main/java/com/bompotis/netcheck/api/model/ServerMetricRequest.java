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
package com.bompotis.netcheck.api.model;

import jakarta.validation.constraints.NotNull;
import java.util.Date;
import java.util.Map;

/**
 * Created by Kyriakos Bompotis on 6/9/20.
 */
public class ServerMetricRequest {
    @NotNull(message = "metrics are mandatory")
    private Map<String,String> metrics;

    @NotNull(message = "collectedAt Date is mandatory")
    private Date collectedAt;

    public ServerMetricRequest() {}

    public ServerMetricRequest(Map<String, String> metrics, Date collectedAt) {
        this.metrics = metrics;
        this.collectedAt = collectedAt;
    }

    public Map<String, String> getMetrics() {
        return metrics;
    }

    public Date getCollectedAt() {
        return collectedAt;
    }
}
