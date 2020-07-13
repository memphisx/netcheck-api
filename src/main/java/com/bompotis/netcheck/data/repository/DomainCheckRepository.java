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
 * Created by Kyriakos Bompotis on 9/6/20.
 */
@Repository
public interface DomainCheckRepository extends JpaRepository<DomainCheckEntity, String> {

    Optional<DomainCheckEntity> findFirstByDomainOrderByTimeCheckedOnDesc(String domain);

    Optional<DomainCheckEntity> findByIdAndDomain(String id, String domain);

    Page<DomainCheckEntity> findAllByDomain(String domain, Pageable pageable);

    @Query("select d from DomainCheckEntity d where d.timeCheckedOn < ?1")
    Page<DomainCheckEntity> findAllCheckedBeforeDate(Date beforeDate, Pageable pageable);

    @Query("select d from DomainCheckEntity d where d.createdAt = (" +
            "select max(d1.createdAt) from DomainCheckEntity d1 where d1.domain = d.domain ) ")
    Page<DomainCheckEntity> findAllLastChecksPerDomain(PageRequest pageRequest);

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
