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
 * Created by Kyriakos Bompotis on 29/6/20.
 */
@Repository
public interface DomainMetricRepository extends JpaRepository<DomainMetricEntity, String> {
    @Query("select d from DomainMetricEntity d where d.domain = ?1 " +
            "and d.periodType = ?2 and d.createdAt >= ?3 and d.createdAt < ?4")
    Set<DomainMetricEntity> findAllBetweenDates(String domain, String period, Date startDate, Date endDate);

    Page<DomainMetricEntity> findAllByDomainAndProtocolAndPeriodType(String domain, DomainMetricEntity.Protocol protocol, DomainMetricEntity.Period periodType, Pageable pageable);
}
