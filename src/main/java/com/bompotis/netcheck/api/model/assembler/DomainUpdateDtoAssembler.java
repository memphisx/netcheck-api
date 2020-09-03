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

import com.bompotis.netcheck.api.model.PatchOperation;
import com.bompotis.netcheck.service.dto.DomainUpdateDto;
import com.bompotis.netcheck.service.dto.Operation;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Created by Kyriakos Bompotis on 2/9/20.
 */
public class DomainUpdateDtoAssembler {
    private final String domain;

    private static final Map<String, Operation.Action> OPERATIONS_TO_ACTION_MAP = Map.of(
            "delete", Operation.Action.REMOVE,
            "replace", Operation.Action.UPDATE,
            "add", Operation.Action.ADD
    );

    public DomainUpdateDtoAssembler(String domain) {
        this.domain = domain;
    }

    private final Set<String> validFields = Set.of(
            "frequency",
            "endpoint",
            "header",
            "headers",
            "timeout"
    );

    public DomainUpdateDto toDto(List<PatchOperation> patchOperations) {
        var dto = new DomainUpdateDto.Builder(domain);
        for (var patchOperation : patchOperations) {
            var field = validateObject(patchOperation.getField(), "field");
            var path = validatePath(patchOperation.getPath(), field);
            var op = validateObject(patchOperation.getOp(), "op");
            if (!OPERATIONS_TO_ACTION_MAP.containsKey(op)) {
                throw new IllegalArgumentException("Invalid value for property `op`: " + op);
            }
            var action = OPERATIONS_TO_ACTION_MAP.get(op);
            var value = validateValue(patchOperation.getValue(), action);
            if (!validFields.contains(field)) {
                throw new IllegalArgumentException("Invalid value for property `field`: " + field);
            }
            var operation = new Operation(field, action, value, path);
            dto.addOperation(operation);
        }
        return dto.build();
    }

    private <T> T validateObject(T object, String type) {
        var atomicObject = new AtomicReference<T>();
        Optional.ofNullable(object).ifPresentOrElse(
                atomicObject::set,
                () -> {
                    throw new IllegalArgumentException("Property "  + type + " cannot be empty or null");
                }
        );
        return atomicObject.get();
    }

    private <T> T validateValue(T object, Operation.Action action) {
        var errorMessage = "Property `value` cannot be empty or null when adding or updating a field";
        return action.equals(Operation.Action.REMOVE) ?
                object :
                validateNonEmpty(object, errorMessage);
    }

    private <T> T validatePath(T object, String field) {
        var errorMessage = "Property `path` cannot be empty or null when operating on a header";
        return field.equals("header") ?
                validateNonEmpty(object, errorMessage) :
                object;
    }

    private <T> T validateNonEmpty(T object, String message) {
        var atomicObject = new AtomicReference<T>();
        Optional.ofNullable(object).ifPresentOrElse(
                atomicObject::set,
                () -> {
                    throw new IllegalArgumentException(message);
                }
        );
        return atomicObject.get();
    }
}
