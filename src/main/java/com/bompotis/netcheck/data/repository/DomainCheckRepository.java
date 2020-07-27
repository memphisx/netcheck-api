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

import com.bompotis.netcheck.data.entity.DomainCheckEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.Optional;
import java.util.Set;

/**
 * Domain Check specific extension of {@link org.springframework.data.jpa.repository.JpaRepository}.
 *
 * @author Kyriakos Bompotis
 */
@Repository
public interface DomainCheckRepository extends JpaRepository<DomainCheckEntity, String> {

    Optional<DomainCheckEntity> findFirstByDomainOrderByTimeCheckedOnDesc(String domain);

    Optional<DomainCheckEntity> findByIdAndDomain(String id, String domain);

    Page<DomainCheckEntity> findAllByDomain(String domain, Pageable pageable);

    @Query("select d from DomainCheckEntity d where d.timeCheckedOn < ?1 and d.changeType = 'NO_CHANGE'")
    Page<DomainCheckEntity> findAllNonFirstCheckedBeforeDate(Date beforeDate, Pageable pageable);

    @Query("select d from DomainCheckEntity d where d.createdAt = (" +
            "select max(d1.createdAt) from DomainCheckEntity d1 where d1.domain = d.domain ) ")
    Page<DomainCheckEntity> findAllLastChecksPerDomain(PageRequest pageRequest);

    @Query("select d from DomainCheckEntity d where d.domain = ?1 and d.createdAt = (" +
            "select max(d1.createdAt) from DomainCheckEntity d1 where d1.domain = d.domain ) ")
    Optional<DomainCheckEntity> findDomainWithItsLastChecks(String domain);

    @Query("select d from DomainCheckEntity d where d.domain = ?1 " +
            "and not d.changeType = 'NO_CHANGE' ")
    Page<DomainCheckEntity> findAllStateChanges(String domain, PageRequest pageRequest);

    @Query("select d from DomainCheckEntity d where d.domain = ?1 " +
            "and (" +
                "d.changeType = 'FIRST_CHECK' or " +
                "d.changeType = 'HTTP_CHANGE' or " +
                "d.changeType = 'HTTP_CERTS_CHANGE' or " +
                "d.changeType = 'HTTP_HTTPS_CHANGE' or " +
                "d.changeType = 'HTTP_HTTPS_CERTS_CHANGE')")
    Page<DomainCheckEntity> findAllHttpStateChanges(String domain, PageRequest pageRequest);

    @Query("select d from DomainCheckEntity d where d.domain = ?1 " +
            "and (" +
            "d.changeType = 'FIRST_CHECK' or " +
            "d.changeType = 'HTTPS_CHANGE' or " +
            "d.changeType = 'HTTPS_CERTS_CHANGE' or " +
            "d.changeType = 'HTTP_HTTPS_CHANGE' or " +
            "d.changeType = 'HTTP_HTTPS_CERTS_CHANGE')")
    Page<DomainCheckEntity> findAllHttpsStateChanges(String domain, PageRequest pageRequest);

    @Query("select d from DomainCheckEntity d where d.domain = ?1 " +
            "and (" +
            "d.changeType = 'FIRST_CHECK' or " +
            "d.changeType = 'HTTPS_CHANGE' or " +
            "d.changeType = 'HTTP_CERTS_CHANGE' or " +
            "d.changeType = 'HTTPS_CERTS_CHANGE' or " +
            "d.changeType = 'CERTS_CHANGE' or " +
            "d.changeType = 'HTTPS_CERTS_CHANGE' or " +
            "d.changeType = 'HTTP_HTTPS_CHANGE' or " +
            "d.changeType = 'HTTP_HTTPS_CERTS_CHANGE')")
    Page<DomainCheckEntity> findAllHttpsAndCertificateStateChanges(String domain, PageRequest pageRequest);

    @Query("select d from DomainCheckEntity d where d.domain = ?1 " +
            "and d.createdAt >= ?2 and d.createdAt < ?3")
    Set<DomainCheckEntity> findAllBetweenDates(String domain, Date startDate, Date endDate);
}
