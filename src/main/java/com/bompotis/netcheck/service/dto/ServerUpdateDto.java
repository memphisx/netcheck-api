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

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Kyriakos Bompotis on 2/9/20.
 */
public class ServerUpdateDto {
    private final String serverId;
    private final List<Operation> operations;

    public String getServerId() {
        return serverId;
    }

    public List<Operation> getOperations() {
        return operations;
    }

    public static class Builder implements PatchDtoBuilder<ServerUpdateDto>{
        private final String serverId;
        private final List<Operation> operations = new ArrayList<>();

        public Builder(String serverId) {
            this.serverId = serverId;
        }

        public Builder addOperation(Operation operation) {
            this.operations.add(operation);
            return this;
        }

        public ServerUpdateDto build() {
            return new ServerUpdateDto(this);
        }
    }

    private ServerUpdateDto(Builder b) {
        this.serverId = b.serverId;
        this.operations = b.operations;
    }
}