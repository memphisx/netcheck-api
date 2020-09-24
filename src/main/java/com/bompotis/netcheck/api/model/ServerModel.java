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

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.hateoas.RepresentationModel;
import org.springframework.hateoas.server.core.Relation;

import java.util.Date;
import java.util.List;

/**
 * Created by Kyriakos Bompotis on 4/9/20.
 */
@Relation(collectionRelation = "servers", itemRelation = "server")
public class ServerModel extends RepresentationModel<ServerModel> {
    private final String serverId;
    private final String serverName;
    private final String description;
    private final Date dateAdded;
    private final String password;
    private final List<ServerDefinitionModel> metricDefinitions;

    public ServerModel(@JsonProperty("serverId") String serverId,
                       @JsonProperty("serverName") String serverName,
                       @JsonProperty("description") String description,
                       @JsonProperty("password") String password,
                       @JsonProperty("dateAdded") Date dateAdded,
                       @JsonProperty("metricDefinitions") List<ServerDefinitionModel> metricDefinitions) {
        this.serverId = serverId;
        this.serverName = serverName;
        this.description = description;
        this.password = password;
        this.dateAdded = dateAdded;
        this.metricDefinitions = metricDefinitions;
    }

    public String getServerId() {
        return serverId;
    }

    public String getServerName() {
        return serverName;
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public String getPassword() {
        return password;
    }

    public Date getDateAdded() {
        return dateAdded;
    }

    public String getDescription() {
        return description;
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public List<ServerDefinitionModel> getMetricDefinitions() {
        return metricDefinitions;
    }
}
