package com.bompotis.netcheck.api.model.assembler;

import com.bompotis.netcheck.api.model.PatchOperation;
import com.bompotis.netcheck.service.dto.Operation;
import com.bompotis.netcheck.service.dto.PatchDtoBuilder;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Created by Kyriakos Bompotis on 7/9/20.
 */
public interface AbstractUpdateDtoAssembler<D,B extends PatchDtoBuilder<D> > {

    Map<String, Operation.Action> getOperationsToActionMap();

    Set<String> getValidFields();

    Set<String> getRequiredFieldsForPath();

    B getDtoBuilder();

    default  <T> T validateObject(T object, String type) {
        var atomicObject = new AtomicReference<T>();
        Optional.ofNullable(object).ifPresentOrElse(
                atomicObject::set,
                () -> {
                    throw new IllegalArgumentException("Property "  + type + " cannot be empty or null");
                }
        );
        return atomicObject.get();
    }

    default  <T> T validateValue(T object, Operation.Action action) {
        var errorMessage = "Property `value` cannot be empty or null when adding or updating a field";
        return action.equals(Operation.Action.REMOVE) ?
                object :
                validateNonEmpty(object, errorMessage);
    }

    default  <T> T validatePath(T object, String field) {
        var errorMessage = "Property `path` cannot be empty or null when operating on a " + field;
        return getRequiredFieldsForPath().contains(field) ?
                validateNonEmpty(object, errorMessage) :
                object;
    }

    default  <T> T validateNonEmpty(T object, String message) {
        var atomicObject = new AtomicReference<T>();
        Optional.ofNullable(object).ifPresentOrElse(
                atomicObject::set,
                () -> {
                    throw new IllegalArgumentException(message);
                }
        );
        return atomicObject.get();
    }

    default D toDto(List<PatchOperation> patchOperations) {
        var dto = getDtoBuilder();
        for (var patchOperation : patchOperations) {
            var field = validateObject(patchOperation.getField(), "field");
            var path = validatePath(patchOperation.getPath(), field);
            var op = validateObject(patchOperation.getOp(), "op");
            if (!getOperationsToActionMap().containsKey(op)) {
                throw new IllegalArgumentException("Invalid value for property `op`: " + op);
            }
            var action = getOperationsToActionMap().get(op);
            var value = validateValue(patchOperation.getValue(), action);
            if (!getValidFields().contains(field)) {
                throw new IllegalArgumentException("Invalid value for property `field`: " + field);
            }
            dto.addOperation(new Operation(field, action, value, path));
        }
        return dto.build();
    }
}
