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

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Created by Kyriakos Bompotis on 19/6/20.
 */
public class DomainDto {
    private final String domain;
    private final DomainCheckDto lastDomainCheck;
    private final Date createdAt;
    private final Integer checkFrequencyMinutes;
    private final String endpoint;
    private final Map<String,String> headers;
    private final Integer timeoutMs;

    public DomainCheckDto getLastDomainCheck() {
        return lastDomainCheck;
    }

    public String getDomain() {
        return domain;
    }

    public Date getCreatedAt() {
        return createdAt;
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

    public static class Builder implements DtoBuilder<DomainDto> {
        private DomainCheckDto lastDomainCheck;
        private String domain;
        private Date createdAt;
        private Integer checkFrequencyMinutes = 10;
        private String endpoint = "";
        private Map<String,String> headers = new HashMap<>();
        private Integer timeoutMs = 30000;

        public Builder lastDomainCheck(DomainCheckDto lastDomainCheck) {
            this.lastDomainCheck = lastDomainCheck;
            return this;
        }

        public Builder domain(String domain) {
            this.domain = domain;
            return this;
        }

        public Builder checkFrequencyMinutes(Integer checkFrequencyMinutes) {
            this.checkFrequencyMinutes = Optional.ofNullable(checkFrequencyMinutes).orElse(10);
            return this;
        }

        public Builder createdAt(Date createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public Builder endpoint(String endpoint) {
            this.endpoint = Optional.ofNullable(endpoint).orElse("");
            return this;
        }

        public Builder timeoutMs(Integer timeoutMs) {
            this.timeoutMs = Optional.ofNullable(timeoutMs).orElse(30000);
            return this;
        }

        public Builder withHeader(String key, String value) {
            if (Optional.ofNullable(this.headers).isEmpty()) {
                this.headers = new HashMap<>();
            }
            this.headers.put(key,value);
            return this;
        }

        public Builder withHeaders(Map<String,String> headers) {
            if (Optional.ofNullable(headers).isPresent()) {
                if (Optional.ofNullable(this.headers).isEmpty()) {
                    this.headers = new HashMap<>();
                }
                this.headers.putAll(headers);
            }
            return this;
        }

        public DomainDto build() {
            return new DomainDto(this);
        }
    }

    private DomainDto(Builder b) {
        this.lastDomainCheck = b.lastDomainCheck;
        this.domain = b.domain;
        this.createdAt = b.createdAt;
        this.checkFrequencyMinutes = b.checkFrequencyMinutes;
        this.endpoint = b.endpoint;
        this.headers = Optional.ofNullable(b.headers).isPresent() ? Map.copyOf(b.headers) : null;
        this.timeoutMs = b.timeoutMs;
    }
}
