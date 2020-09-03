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

    public DomainUpdateDtoAssembler(String domain) {
        this.domain = domain;
    }

    private final Map<String, Operation.Action> operationToActionMap = Map.of(
            "delete", Operation.Action.REMOVE,
            "replace", Operation.Action.UPDATE,
            "add", Operation.Action.ADD
    );

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
            if (!operationToActionMap.containsKey(op)) {
                throw new IllegalArgumentException("Invalid value for property `op`: " + op);
            }
            var action = operationToActionMap.get(op);
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
