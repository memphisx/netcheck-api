package com.bompotis.netcheck.data.repositories;

import com.bompotis.netcheck.data.entities.DomainHistoricEntryEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Created by Kyriakos Bompotis on 9/6/20.
 */
@Repository
public interface DomainHistoricEntryRepository extends JpaRepository<DomainHistoricEntryEntity, String> {
    Page<DomainHistoricEntryEntity> findAllByDomain(String domain, Pageable pageable);
}
