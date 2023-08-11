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
package com.bompotis.netcheck.api.model.assembler;

import com.bompotis.netcheck.service.dto.DomainUpdateDto;
import com.bompotis.netcheck.service.dto.Operation;

import java.util.Map;
import java.util.Set;

/**
 * Created by Kyriakos Bompotis on 2/9/20.
 */
public class DomainUpdateDtoAssembler implements AbstractUpdateDtoAssembler<DomainUpdateDto,DomainUpdateDto.Builder>{
    private final String domain;

    private static final Map<String, Operation.Action> OPERATIONS_TO_ACTION_MAP = Map.of(
            "delete", Operation.Action.REMOVE,
            "replace", Operation.Action.UPDATE,
            "add", Operation.Action.ADD
    );

    private static final Set<String> VALID_FIELDS = Set.of(
            "frequency",
            "endpoint",
            "header",
            "headers",
            "timeout",
            "httpPort",
            "httpsPort"
    );

    private static final Set<String> REQUIRED_FIELDS_FOR_PATH = Set.of(
            "header"
    );

    public DomainUpdateDtoAssembler(String domain) {
        this.domain = domain;
    }

    @Override
    public Map<String, Operation.Action> getOperationsToActionMap() {
        return OPERATIONS_TO_ACTION_MAP;
    }

    @Override
    public Set<String> getValidFields() {
        return VALID_FIELDS;
    }

    @Override
    public Set<String> getRequiredFieldsForPath() {
        return REQUIRED_FIELDS_FOR_PATH;
    }

    @Override
    public DomainUpdateDto.Builder getDtoBuilder() {
        return new DomainUpdateDto.Builder(domain);
    }
}
