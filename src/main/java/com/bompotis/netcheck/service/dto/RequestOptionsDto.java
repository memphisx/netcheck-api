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
package com.bompotis.netcheck.service.dto;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import java.util.Optional;

/**
 * Created by Kyriakos Bompotis on 7/9/20.
 */
public class RequestOptionsDto {
    private final Integer page;
    private final Integer size;
    private final String filter;
    private final String sortBy;
    private final boolean desc;

    public Boolean getDesc() {
        return desc;
    }

    public String getSortBy() {
        return sortBy;
    }

    public String getFilter() {
        return filter;
    }

    public Integer getSize() {
        return size;
    }

    public Integer getPage() {
        return page;
    }

    public PageRequest getPageRequest() {
        var sort = desc ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        return PageRequest.of(page, size, sort);
    }

    public static class Builder implements DtoBuilder<RequestOptionsDto>{
        private Integer page;
        private Integer size;
        private String filter;
        private String sortBy;
        private boolean desc;

        public Builder page(Integer page) {
            this.page = Optional.ofNullable(page).orElse(0);
            return this;
        }

        public Builder size(Integer size) {
            this.size = Optional.ofNullable(size).orElse(10);
            return this;
        }

        public Builder filter(String filter) {
            this.filter = Optional.ofNullable(filter).orElse("");
            return this;
        }

        public Builder sortBy(String sortBy) {
            this.sortBy = Optional.ofNullable(sortBy).orElse("createdAt");
            return this;
        }

        public Builder desc(Boolean desc) {
            this.desc = Optional.ofNullable(desc).orElse(true);
            return this;
        }

        public RequestOptionsDto build() {
            return new RequestOptionsDto(this);
        }
    }

    private RequestOptionsDto(Builder b) {
        this.page = b.page;
        this.size = b.size;
        this.filter = b.filter;
        this.sortBy = b.sortBy;
        this.desc = b.desc;
    }
}
