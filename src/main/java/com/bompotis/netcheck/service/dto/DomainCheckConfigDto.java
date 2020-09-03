package com.bompotis.netcheck.service.dto;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Created by Kyriakos Bompotis on 2/9/20.
 */
public class DomainCheckConfigDto {
    private final String domain;
    private final String endpoint;
    private final Map<String,String> headers;
    private final int timeoutMs;

    public String getDomain() {
        return domain;
    }

    public String getEndpoint() {
        return endpoint;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public int getTimeoutMs() {
        return timeoutMs;
    }

    public static class Builder {
        private String domain;
        private String endpoint = "";
        private int timeoutMs = 30000;
        private final Map<String,String> headers = new HashMap<>();

        public Builder domain(String domain) {
            this.domain = domain;
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
            this.headers.put(key,value);
            return this;
        }

        public Builder withHeaders(Map<String,String> headers) {
            if (Optional.ofNullable(headers).isPresent()) {
                this.headers.putAll(headers);
            }
            return this;
        }

        public DomainCheckConfigDto build() {
            return new DomainCheckConfigDto(this);
        }
    }

    private DomainCheckConfigDto(Builder b) {
        this.domain = b.domain;
        this.endpoint = b.endpoint;
        this.timeoutMs = b.timeoutMs;
        this.headers = Map.copyOf(b.headers);
    }
}
