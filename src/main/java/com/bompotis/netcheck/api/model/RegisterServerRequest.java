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

import jakarta.validation.constraints.NotBlank;

/**
 * Created by Kyriakos Bompotis on 4/9/20.
 */
public class RegisterServerRequest {
    @NotBlank(message = "serverName is mandatory")
    private String serverName;

    private String description;

    public RegisterServerRequest() {}

    public RegisterServerRequest(String serverName, String description) {
        this.serverName = serverName;
        this.description = description;
    }

    public String getServerName() {
        return serverName;
    }

    public String getDescription() {
        return description;
    }
}
