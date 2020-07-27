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
package com.bompotis.netcheck.scheduler.batch.writer;

import com.bompotis.netcheck.data.entity.DomainMetricEntity;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.JpaItemWriter;

import javax.persistence.EntityManagerFactory;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Kyriakos Bompotis on 30/6/20.
 */
public class DomainMetricListWriter implements ItemWriter<List<DomainMetricEntity>> {
    protected static final Log logger = LogFactory.getLog(DomainMetricListWriter.class);
    private final JpaItemWriter<DomainMetricEntity> jpaItemWriter;

    public DomainMetricListWriter() {
        this.jpaItemWriter = new JpaItemWriter<>();
    }

    public void setEntityManagerFactory(EntityManagerFactory entityManagerFactory) {
        jpaItemWriter.setEntityManagerFactory(entityManagerFactory);
    }

    public void afterPropertiesSet() throws Exception {
        this.jpaItemWriter.afterPropertiesSet();
    }

    public void setUsePersist(boolean usePersist) {
        this.jpaItemWriter.setUsePersist(usePersist);
    }

    @Override
    public void write(List<? extends List<DomainMetricEntity>> list) throws Exception {
        List<DomainMetricEntity> metricEntities = new ArrayList<>();
        list.forEach(metricEntities::addAll);
        jpaItemWriter.write(metricEntities);
    }
}
