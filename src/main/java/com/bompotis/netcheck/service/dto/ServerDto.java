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
import java.util.List;

/**
 * Created by Kyriakos Bompotis on 4/9/20.
 */
public class ServerDto {

    private final String serverId;
    private final String serverName;
    private final String password;
    private final Date dateAdded;
    private final String description;
    private final List<ServerDefinitionDto> serverDefinitionDtos;

    public String getServerId() {
        return serverId;
    }

    public String getServerName() {
        return serverName;
    }

    public String getPassword() {
        return password;
    }

    public Date getDateAdded() {
        return dateAdded;
    }

    public String getDescription() {
        return description;
    }

    public List<ServerDefinitionDto> getServerDefinitionDtos() {
        return serverDefinitionDtos;
    }

    public static class Builder implements DtoBuilder<ServerDto> {
        private String serverId;
        private String serverName;
        private String description;
        private String password;
        private Date dateAdded;
        private List<ServerDefinitionDto> serverDefinitionDtos = List.of();

        public Builder serverId(String serverId) {
            this.serverId = serverId;
            return this;
        }

        public Builder description(String description) {
            this.description = description;
            return this;
        }

        public Builder serverName(String serverName) {
            this.serverName = serverName;
            return this;
        }

        public Builder password(String password) {
            this.password = password;
            return this;
        }

        public Builder dateAdded(Date dateAdded) {
            this.dateAdded = dateAdded;
            return this;
        }

        public Builder serverDefinitionDtos(List<ServerDefinitionDto> serverDefinitionDtos) {
            this.serverDefinitionDtos = List.copyOf(serverDefinitionDtos);
            return this;
        }

        public ServerDto build() {
            return new ServerDto(this);
        }
    }

    private ServerDto(Builder b) {
        this.serverId = b.serverId;
        this.serverName = b.serverName;
        this.password = b.password;
        this.dateAdded = b.dateAdded;
        this.description = b.description;
        this.serverDefinitionDtos = b.serverDefinitionDtos;
    }
}
