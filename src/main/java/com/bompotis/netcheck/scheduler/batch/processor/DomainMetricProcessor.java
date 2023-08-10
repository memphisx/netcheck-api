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

import com.bompotis.netcheck.data.entity.DomainEntity;
import com.bompotis.netcheck.data.entity.DomainMetricEntity;
import com.bompotis.netcheck.service.MetricService;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.lang.NonNull;

import java.util.List;

/**
 * Created by Kyriakos Bompotis on 29/6/20.
 */
public class DomainMetricProcessor implements ItemProcessor<DomainEntity, List<DomainMetricEntity>> {

    private final MetricService metricService;

    private final MetricService.ScheduledPeriod period;

    public DomainMetricProcessor(MetricService.ScheduledPeriod period,
                                 MetricService metricService) {
        this.period = period;
        this.metricService = metricService;
    }

    @Override
    public List<DomainMetricEntity> process(@NonNull DomainEntity domainEntity) {
        return metricService.generateMetric(domainEntity,new MetricService.TimeFrame(this.period));
    }
}
