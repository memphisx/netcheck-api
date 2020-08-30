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

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.Date;

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
        DomainCheckEntity domainCheckEntity = null;
        try {
            log.info("Processing {}", domainEntity.getDomain());
            if (isEligibleForCheck(domainEntity.getDomain())) {
                log.info("Domain {} is eligible for check", domainEntity.getDomain());
                domainCheckEntity = check(domainEntity);
                log.info("Successfully checked {}. Passing to writer", domainEntity.getDomain());
            } else {
                log.info("Skipping check for domain {}.", domainEntity.getDomain());
            }
        } catch (Exception e) {
            log.error("Failed to check {}", domainEntity.getDomain(), e);
        }
        return domainCheckEntity;
    }

    private DomainCheckEntity check(DomainEntity domainEntity) throws NoSuchAlgorithmException, IOException, KeyManagementException {
        log.info("Checking {}", domainEntity.getDomain());
        var status = domainService.check(domainEntity.getDomain());
        return domainService.convertToDomainCheckEntity(status, domainEntity);
    }

    private boolean isEligibleForCheck(String domain) {
        var optionalDomainWithLastChecks = domainService.getDomain(domain);
        if (optionalDomainWithLastChecks.isEmpty()) {
            log.info("No checks found for {}", domain);
            return true;
        }
        var domainWithLastChecks = optionalDomainWithLastChecks.get();
        var lastCheckDate = domainWithLastChecks
                .getLastDomainCheck()
                .getHttpCheckDto()
                .getTimeCheckedOn()
                .toInstant()
                .atZone(ZoneOffset.UTC)
                .toLocalDateTime()
                .withSecond(0);
        log.info("Last check for domain {} was triggered on {}", domain, lastCheckDate.toString());
        var now = new Date().toInstant().atZone(ZoneOffset.UTC).toLocalDateTime();
        var nextExpectedCheck = lastCheckDate.plusMinutes(domainWithLastChecks.getCheckFrequencyMinutes());
        return nextExpectedCheck.isBefore(now);
    }
}
