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
package com.bompotis.netcheck.data.repository;

import com.bompotis.netcheck.data.entity.DomainMetricEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.Set;

/**
 * Domain Metric specific extension of {@link org.springframework.data.jpa.repository.JpaRepository}.
 *
 * @author Kyriakos Bompotis
 */
@Repository
public interface DomainMetricRepository extends JpaRepository<DomainMetricEntity, String> {
    @Query("select d from DomainMetricEntity d where d.domain = ?1 " +
            "and d.periodType = ?2 and d.createdAt >= ?3 and d.createdAt < ?4")
    Set<DomainMetricEntity> findAllBetweenDates(String domain, String period, Date startDate, Date endDate);

    Page<DomainMetricEntity> findAllByDomainAndProtocolAndPeriodType(String domain, DomainMetricEntity.Protocol protocol, DomainMetricEntity.Period periodType, Pageable pageable);
}
