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
package com.bompotis.netcheck.scheduler.batch.reader;

import com.bompotis.netcheck.data.entity.DomainCheckEntity;
import com.bompotis.netcheck.data.repository.DomainCheckRepository;
import org.springframework.batch.item.data.RepositoryItemReader;
import org.springframework.data.domain.Sort;
import org.springframework.lang.NonNull;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Created by Kyriakos Bompotis on 13/7/20.
 */
public class OldDomainCheckItemReader extends RepositoryItemReader<DomainCheckEntity> {

    private final int threshold;

    public OldDomainCheckItemReader(DomainCheckRepository domainCheckRepository, Integer threshold) {
        this.setRepository(domainCheckRepository);
        this.setMethodName("findAllNonFirstCheckedBeforeDate");
        this.setPageSize(10);
        this.setSort(Map.of("createdAt", Sort.Direction.ASC));
        if (threshold > 0) {
            this.threshold = threshold;
        }
        else {
            this.threshold = 3;
        }
    }

    @NonNull
    @Override
    protected List<DomainCheckEntity> doPageRead() throws Exception {
        this.setArguments(List.of(Date.from(LocalDateTime.now().minusDays(1).minusMonths(threshold).toInstant(ZoneOffset.UTC))));
        return super.doPageRead();
    }
}
