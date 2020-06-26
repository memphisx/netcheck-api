package com.bompotis.netcheck.api.model.assembler;

import org.springframework.hateoas.RepresentationModel;
import org.springframework.hateoas.server.mvc.RepresentationModelAssemblerSupport;

/**
 * Created by Kyriakos Bompotis on 17/6/20.
 */
public abstract class PaginatedRepresentationModelAssemblerSupport<T, D extends RepresentationModel<?>> extends RepresentationModelAssemblerSupport<T, D> {
    public PaginatedRepresentationModelAssemblerSupport(Class controllerClass, Class<D> resourceType) {
        super(controllerClass, resourceType);
    }

    protected boolean isValidPage(int pageNumber, int totalPages) {
        return pageNumber+1 <= totalPages;
    }

    protected boolean isNotFirstPage(int pageNumber) {
        return pageNumber != 0;
    }

    protected boolean isNotLastPage(int pageNumber, int totalPages) {
        return (pageNumber + 1 != totalPages);
    }
}
