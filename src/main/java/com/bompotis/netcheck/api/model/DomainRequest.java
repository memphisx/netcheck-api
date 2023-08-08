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

import jakarta.validation.constraints.Min;
import java.util.Map;

/**
 * Created by Kyriakos Bompotis on 2/9/20.
 */
public class DomainRequest {
    @Min(value = 1, message = "checkFrequencyMinutes should not be less than 1")
    private final Integer checkFrequencyMinutes;

    private final String endpoint;

    private final Map<String,String> headers;

    @Min(value = 1, message = "timeoutMs should not be less than 1")
    private final Integer timeoutMs;

    public DomainRequest(Integer checkFrequencyMinutes, String endpoint, Map<String, String> headers, Integer timeoutMs) {
        this.checkFrequencyMinutes = checkFrequencyMinutes;
        this.endpoint = endpoint;
        this.headers = headers;
        this.timeoutMs = timeoutMs;
    }

    public Integer getCheckFrequencyMinutes() {
        return checkFrequencyMinutes;
    }

    public String getEndpoint() {
        return endpoint;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public Integer getTimeoutMs() {
        return timeoutMs;
    }
}
