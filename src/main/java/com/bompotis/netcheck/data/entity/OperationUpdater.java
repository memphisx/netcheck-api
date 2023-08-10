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

/**
 * Created by Kyriakos Bompotis on 8/9/20.
 */
public interface OperationUpdater<T> extends EntityBuilder<T> {
    void removeField(String field, String path);

    void updateField(String field, String path, String value);

    default void processOperation(Operation operation) {
        switch (operation.action()) {
            case REMOVE -> removeField(operation.field(), operation.path());
            case ADD, UPDATE -> updateField(operation.field(), operation.path(), operation.value());
            default -> throw new IllegalArgumentException("Invalid operation");
        }
    }
}
