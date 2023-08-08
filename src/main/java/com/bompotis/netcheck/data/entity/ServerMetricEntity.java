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

import io.hypersistence.utils.hibernate.type.json.JsonBinaryType;
import jakarta.persistence.*;
import org.hibernate.annotations.Type;
import org.springframework.lang.NonNull;

import java.util.Date;
import java.util.Map;
import java.util.Objects;

/**
 * Created by Kyriakos Bompotis on 5/9/20.
 */
@Entity
@Table(name = "server_metric", indexes = {
        @Index(name = "server_metrics__server_created_idx", columnList = "server_id, created_at")
})
public class ServerMetricEntity extends AbstractTimestampablePersistable<String>{

    @NonNull
    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.PERSIST)
    @JoinColumn(name = "server_id")
    private ServerEntity serverEntity;

    @NonNull
    @Type(JsonBinaryType.class)
    @Column(columnDefinition = "jsonb", name = "metrics")
    private Map<String,String> metrics;

    @NonNull
    @Column(name = "collected_at")
    private Date collectedAt;

    @NonNull
    public ServerEntity getServerEntity() {
        return serverEntity;
    }

    @NonNull
    public Map<String, String> getMetrics() {
        return metrics;
    }

    @NonNull
    public Date getCollectedAt() {
        return collectedAt;
    }

    protected ServerMetricEntity() {}

    public static class Builder {
        private ServerEntity serverEntity;
        private Map<String,String> metrics;
        private Date collectedAt;

        public Builder metrics(Map<String,String> metrics) {
            this.metrics = metrics;
            return this;
        }

        public Builder serverEntity(ServerEntity serverEntity) {
            this.serverEntity = serverEntity;
            return this;
        }

        public Builder collectedAt(Date collectedAt) {
            this.collectedAt = collectedAt;
            return this;
        }

        public ServerMetricEntity build() {
            return new ServerMetricEntity(this);
        }
    }

    private ServerMetricEntity(Builder b) {
        this.metrics = b.metrics;
        this.serverEntity = b.serverEntity;
        this.collectedAt = b.collectedAt;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ServerMetricEntity that = (ServerMetricEntity) o;
        return serverEntity.equals(that.serverEntity) &&
                metrics.equals(that.metrics);
    }

    @Override
    public int hashCode() {
        return Objects.hash(serverEntity, metrics);
    }
}
