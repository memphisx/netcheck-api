package com.bompotis.netcheck.data.repository;

import com.bompotis.netcheck.data.entity.DomainCheckEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

/**
 * Created by Kyriakos Bompotis on 9/6/20.
 */
@Repository
public interface DomainCheckRepository extends JpaRepository<DomainCheckEntity, String> {
    Page<DomainCheckEntity> findAllByDomain(String domain, Pageable pageable);

    @Query("select d from DomainCheckEntity d where d.createdAt = (" +
            "select max(d1.createdAt) from DomainCheckEntity d1 where d1.domain = d.domain ) " +
            "and not exists (select d2 from DomainCheckEntity d2  where d2.domain = d.domain and d2.domain > d.domain)")
    Page<DomainCheckEntity> findAllLastChecksPerDomain(PageRequest pageRequest);
}
