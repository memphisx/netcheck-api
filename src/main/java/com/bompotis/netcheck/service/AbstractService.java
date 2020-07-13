package com.bompotis.netcheck.service;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import java.util.Optional;

/**
 * Created by Kyriakos Bompotis on 13/7/20.
 */
public abstract class AbstractService {

    protected PageRequest getDefaultPageRequest(Integer page, Integer size) {
        return PageRequest.of(
                Optional.ofNullable(page).orElse(0),
                Optional.ofNullable(size).orElse(10),
                Sort.by("createdAt").descending()
        );
    }
}
