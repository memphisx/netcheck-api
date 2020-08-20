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
package com.bompotis.netcheck.scheduler.batch.processor;

import com.bompotis.netcheck.data.entity.DomainCheckEntity;
import com.bompotis.netcheck.data.entity.DomainEntity;
import com.bompotis.netcheck.service.DomainService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Created by Kyriakos Bompotis on 10/6/20.
 */
@Component
@Transactional
public class DomainCheckProcessor implements ItemProcessor<DomainEntity, DomainCheckEntity> {

    private final DomainService domainService;

    private static final Logger log = LoggerFactory.getLogger(DomainCheckProcessor.class);

    @Autowired
    public DomainCheckProcessor(DomainService domainService) {
        this.domainService = domainService;
    }

    @Override
    public DomainCheckEntity process(DomainEntity domainEntity) {
        DomainCheckEntity domainCheckEntityBuilder = null;
        try {
            log.info("Checking {}", domainEntity.getDomain());
            var status = domainService.check(domainEntity.getDomain());
            domainCheckEntityBuilder = domainService.convertToDomainCheckEntity(status, domainEntity);
            log.info("Successfully checked {}. Passing to writer", domainEntity.getDomain());
        } catch (Exception e) {
            log.error("Failed to check {}", domainEntity.getDomain(), e);
        }
        return domainCheckEntityBuilder;
    }
}
