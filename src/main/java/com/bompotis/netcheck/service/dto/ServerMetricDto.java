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

import com.bompotis.netcheck.api.exception.InvalidAuthHeaders;
import com.bompotis.netcheck.api.exception.UnauthorizedStatusException;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Date;
import java.util.Map;
import java.util.Optional;

/**
 * Created by Kyriakos Bompotis on 6/9/20.
 */
public class ServerMetricDto {
    private final String id;
    private final String serverId;
    private final String password;
    private final Map<String,String> metrics;
    private final Date collectedAt;

    public String getServerId() {
        return serverId;
    }

    public String getPassword() {
        return password;
    }

    public Date getCollectedAt() {
        return collectedAt;
    }

    public Map<String, String> getMetrics() {
        return metrics;
    }

    public String getId() {
        return id;
    }

    public ServerMetricEvent toServerMetricEvent(String id) {
        return new ServerMetricEvent(this, id);
    }

    public static class Builder implements DtoBuilder<ServerMetricDto> {
        private final String serverId;
        private String password;
        private Map<String,String> metrics;
        private Date collectedAt;
        private String id;

        public Builder(String serverId) {
            this.serverId = serverId;
        }

        public Builder metrics(Map<String,String> metrics) {
            this.metrics = metrics;
            return this;
        }

        public Builder authHeader(String authHeader) throws InvalidAuthHeaders, UnauthorizedStatusException {
            var credentials = new Credentials(authHeader);
            if (!credentials.getUsername().equals(this.serverId)) {
                throw new UnauthorizedStatusException();
            }
            this.password = credentials.getPassword();
            return this;
        }

        public Builder collectedAt(Date collectedAt) {
            this.collectedAt = collectedAt;
            return this;
        }

        public Builder id(String id) {
            this.id = id;
            return this;
        }

        private static class Credentials {
            private final String username;
            private final String password;

            private Credentials(String base64AuthHeader) throws InvalidAuthHeaders {
                if (Optional.ofNullable(base64AuthHeader).isPresent() && base64AuthHeader.toLowerCase().startsWith("basic")) {
                    String base64Credentials = base64AuthHeader.substring("Basic".length()).trim();
                    byte[] credDecoded = Base64.getDecoder().decode(base64Credentials);
                    String credentials = new String(credDecoded, StandardCharsets.UTF_8);
                    final String[] values = credentials.split(":", 2);
                    this.username = values[0];
                    this.password = values[1];
                }
                else throw new InvalidAuthHeaders();
            }

            public String getUsername() {
                return username;
            }

            public String getPassword() {
                return password;
            }
        }

        public ServerMetricDto build() {
            return new ServerMetricDto(this);
        }
    }

    public static class ServerMetricEvent {
        private final String id;
        private final String serverId;
        private final Map<String,String> metrics;
        private final Date collectedAt;

        private ServerMetricEvent(ServerMetricDto serverMetricDto, String id) {
            this.id = id;
            this.serverId = serverMetricDto.serverId;
            this.metrics = Map.copyOf(serverMetricDto.metrics);
            this.collectedAt = serverMetricDto.collectedAt;
        }

        public String getId() {
            return id;
        }

        public String getServerId() {
            return serverId;
        }

        public Map<String, String> getMetrics() {
            return metrics;
        }

        public Date getCollectedAt() {
            return collectedAt;
        }
    }

    private ServerMetricDto(Builder b) {
        this.serverId = b.serverId;
        this.password = b.password;
        this.metrics = b.metrics;
        this.collectedAt = b.collectedAt;
        this.id = b.id;
    }
}
