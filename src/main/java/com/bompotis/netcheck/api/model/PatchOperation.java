package com.bompotis.netcheck.api.model;

/**
 * Created by Kyriakos Bompotis on 2/9/20.
 */
public class PatchOperation {
    private final String op;
    private final String field;
    private final String path;
    private final String value;

    public PatchOperation(String op, String field, String value, String path) {
        this.op = op;
        this.path = path;
        this.field = field;
        this.value = value;
    }

    public String getOp() {
        return op;
    }

    public String getPath() {
        return path;
    }

    public String getValue() {
        return value;
    }

    public String getField() {
        return field;
    }
}
