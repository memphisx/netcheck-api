package com.bompotis.netcheck.data.repositories;

import com.bompotis.netcheck.data.entities.DomainHistoricEntryEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.PagingAndSortingRepository;

/**
 * Created by Kyriakos Bompotis on 9/6/20.
 */
public interface DomainHistoryEntryRepository extends PagingAndSortingRepository<DomainHistoricEntryEntity, String> {
    Page<DomainHistoricEntryEntity> findAllByDomainEntityDomain(String domain, Pageable pageable);
}
