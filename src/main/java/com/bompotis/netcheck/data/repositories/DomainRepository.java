package com.bompotis.netcheck.data.repositories;

import com.bompotis.netcheck.data.entities.DomainEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Created by Kyriakos Bompotis on 8/6/20.
 */
@Repository
public interface DomainRepository extends JpaRepository<DomainEntity, String> {}
