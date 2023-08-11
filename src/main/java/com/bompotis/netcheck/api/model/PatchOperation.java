package com.bompotis.netcheck.api.model;

/**
 * Created by Kyriakos Bompotis on 2/9/20.
 */
public record PatchOperation(String op, String field, String value, String path) {
}
