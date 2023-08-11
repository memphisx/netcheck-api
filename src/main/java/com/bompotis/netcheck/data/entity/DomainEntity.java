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

import com.bompotis.netcheck.service.dto.Operation;
import io.hypersistence.utils.hibernate.type.json.JsonBinaryType;
import jakarta.persistence.*;
import org.hibernate.annotations.Type;
import org.springframework.lang.NonNull;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;


/**
 * Created by Kyriakos Bompotis on 8/6/20.
 */
@Entity
@Table(name = "domain")
public class DomainEntity extends AbstractTimestampable<String>{

    @Id
    @Column(name = "domain")
    private String domain;

    @Column(name = "check_frequency_minutes")
    private int checkFrequency;

    @NonNull
    @Column(name = "endpoint")
    private String endpoint;

    @Column(name = "timeout_ms")
    private int timeoutMs;

    @Column(name = "http_port")
    private int httpPort;

    @Column(name = "https_port")
    private int httpsPort;

    @NonNull
    @Type(JsonBinaryType.class)
    @Column(columnDefinition = "jsonb", name = "headers")
    private Map<String,String> headers;

    @OneToMany(fetch = FetchType.LAZY, mappedBy="domainEntity", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<DomainCheckEntity> domainHistoryEntries;

    @OneToMany(fetch = FetchType.LAZY, mappedBy="domainEntity", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<DomainMetricEntity> domainMetricEntries;

    protected DomainEntity() {}

    public Set<DomainCheckEntity> getDomainHistoryEntries() {
        return domainHistoryEntries;
    }

    public String getDomain() {
        return domain;
    }

    public int getCheckFrequency() {
        return checkFrequency;
    }

    @Override
    public String getId()  {
        return domain;
    }

    @Override
    public boolean isNew() {
        return null == domain;
    }

    @NonNull
    public String getEndpoint() {
        return endpoint;
    }

    @NonNull
    public Map<String, String> getHeaders() {
        return headers;
    }

    public Set<DomainMetricEntity> getDomainMetricEntries() {
        return domainMetricEntries;
    }

    public int getTimeoutMs() {
        return timeoutMs;
    }

    public int getHttpPort() {
        return httpPort;
    }

    public int getHttpsPort() {
        return httpsPort;
    }

    public static class Builder implements EntityBuilder<DomainEntity> {
        private String domain;
        private Set<DomainCheckEntity> domainHistoryEntries;
        private int frequency = 10;
        private String endpoint = "";
        private Map<String,String> headers = new HashMap<>();
        private Set<DomainMetricEntity> domainMetricEntries;
        private int timeoutMs = 30000;

        private int httpPort = 80;

        private int httpsPort = 443;

        public Builder domainHistoryEntries(Set<DomainCheckEntity> domainHistoryEntries) {
            this.domainHistoryEntries = domainHistoryEntries;
            return this;
        }

        public Builder domainMetricEntries(Set<DomainMetricEntity> domainMetricEntries) {
            this.domainMetricEntries = domainMetricEntries;
            return this;
        }

        public Builder frequency(int frequencyMinutes) {
            this.frequency = frequencyMinutes;
            return this;
        }

        public Builder domain(String domain) {
            this.domain = domain;
            return this;
        }

        public Builder endpoint(String endpoint) {
            this.endpoint = endpoint;
            return this;
        }

        public Builder httpPort(Integer httpPort) {
            if (Optional.ofNullable(httpPort).isPresent()) {
                this.httpPort = httpPort;
            }
            return this;
        }

        public Builder httpsPort(Integer httpsPort) {
            if (Optional.ofNullable(httpsPort).isPresent()) {
                this.httpsPort = httpsPort;
            }
            return this;
        }

        public Builder timeoutMs(int timeoutMs) {
            this.timeoutMs = timeoutMs;
            return this;
        }

        public Builder headers(Map<String,String> headers) {
            if (Optional.ofNullable(headers).isPresent()) {
                this.headers = headers;
            }
            return this;
        }

        public DomainEntity build() {
            return new DomainEntity(this);
        }
    }

    public static class Updater implements OperationUpdater<DomainEntity> {
        private final String domain;
        private final Set<DomainCheckEntity> domainHistoryEntries;
        private int frequency;
        private String endpoint;
        private Map<String,String> headers;
        private final Set<DomainMetricEntity> domainMetricEntries;
        private int timeoutMs;
        private int httpPort;
        private int httpsPort;
        private final Date createdAt;

        public Updater(DomainEntity entity) {
            this.domain = entity.domain;
            this.domainHistoryEntries = entity.domainHistoryEntries;
            this.frequency = entity.checkFrequency;
            this.endpoint = entity.endpoint;
            this.headers = entity.headers;
            this.domainMetricEntries = entity.domainMetricEntries;
            this.timeoutMs = entity.timeoutMs;
            this.createdAt = entity.getCreatedAt();
            this.httpPort = entity.httpPort;
            this.httpsPort = entity.httpsPort;
        }

        public Updater withUpdatedValues(List<Operation> operations) {
            operations.forEach(this::processOperation);
            return this;
        }

        public void removeField(String field, String path) {
            switch (field) {
                case "frequency" -> this.frequency = 10;
                case "endpoint" -> this.endpoint = "";
                case "timeout" -> this.timeoutMs = 30000;
                case "headers" -> this.headers = new HashMap<>();
                case "header" -> this.headers.remove(path);
                case "httpPort" -> this.httpPort = 80;
                case "httpsPort" -> this.httpsPort = 443;
                default -> throw new IllegalArgumentException("Invalid property for removal: " + field);
            }
        }

        public void updateField(String field, String path, String value) {
            switch (field) {
                case "frequency" -> this.frequency = Integer.parseInt(value);
                case "endpoint" -> this.endpoint = value;
                case "timeout" -> this.timeoutMs = Integer.parseInt(value);
                case "header" -> this.headers.put(path, value);
                case "httpPort" -> this.httpPort = Integer.parseInt(value);
                case "httpsPort" -> this.httpsPort = Integer.parseInt(value);
                default -> throw new IllegalArgumentException("Invalid property to update/add: " + field);
            }
        }

        public DomainEntity build() {
            return new DomainEntity(this);
        }
    }

    private DomainEntity(Builder b) {
        this.domain = b.domain;
        this.domainHistoryEntries = b.domainHistoryEntries != null ? Set.copyOf(b.domainHistoryEntries) : null;
        this.domainMetricEntries = b.domainMetricEntries != null ? Set.copyOf(b.domainMetricEntries) : null;
        this.checkFrequency = b.frequency;
        this.endpoint = b.endpoint;
        this.headers = Map.copyOf(b.headers);
        this.timeoutMs = b.timeoutMs;
        this.httpPort = b.httpPort;
        this.httpsPort = b.httpsPort;
    }

    private DomainEntity(Updater b) {
        this.domain = b.domain;
        this.domainHistoryEntries = b.domainHistoryEntries != null ? Set.copyOf(b.domainHistoryEntries) : null;
        this.domainMetricEntries = b.domainMetricEntries != null ? Set.copyOf(b.domainMetricEntries) : null;
        this.checkFrequency = b.frequency;
        this.endpoint = b.endpoint;
        this.headers = b.headers != null ? Map.copyOf(b.headers) : Map.of();
        this.timeoutMs = b.timeoutMs;
        this.createdAt = b.createdAt;
        this.httpsPort = b.httpsPort;
        this.httpPort = b.httpPort;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DomainEntity that = (DomainEntity) o;
        return domain.equals(that.domain);
    }

    @Override
    public int hashCode() {
        return Objects.hash(domain);
    }
}
