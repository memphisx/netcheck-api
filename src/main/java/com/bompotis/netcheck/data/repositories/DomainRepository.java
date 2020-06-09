package com.bompotis.netcheck.data.repositories;

import com.bompotis.netcheck.data.entities.DomainEntity;
import org.springframework.data.repository.PagingAndSortingRepository;

/**
 * Created by Kyriakos Bompotis on 8/6/20.
 */
public interface DomainRepository extends PagingAndSortingRepository<DomainEntity, String> {
}
