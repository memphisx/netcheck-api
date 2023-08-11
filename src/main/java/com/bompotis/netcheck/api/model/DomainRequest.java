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

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import java.util.Map;

/**
 * Created by Kyriakos Bompotis on 2/9/20.
 */
public record DomainRequest(
        @Min(value = 1, message = "checkFrequencyMinutes should not be less than 1") Integer checkFrequencyMinutes,
        String endpoint,
        @Min(value = 1, message = "port should not be less than 1") @Max(value = 65535, message = "port should be no more than 65535") Integer httpPort,
        @Min(value = 1, message = "port should not be less than 1") @Max(value = 65535, message = "port should be no more than 65535") Integer httpsPort,
        Map<String, String> headers,
        @Min(value = 1, message = "timeoutMs should not be less than 1") Integer timeoutMs) {
    public DomainRequest(Integer checkFrequencyMinutes, String endpoint, Integer httpPort, Integer httpsPort, Map<String, String> headers, Integer timeoutMs) {
        this.checkFrequencyMinutes = checkFrequencyMinutes;
        this.endpoint = endpoint;
        this.httpPort = httpPort;
        this.httpsPort = httpsPort;
        this.headers = headers;
        this.timeoutMs = timeoutMs;
    }

    @Override
    public Integer checkFrequencyMinutes() {
        return checkFrequencyMinutes;
    }

    @Override
    public Integer timeoutMs() {
        return timeoutMs;
    }
}
