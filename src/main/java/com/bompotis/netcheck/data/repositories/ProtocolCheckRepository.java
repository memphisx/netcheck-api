package com.bompotis.netcheck.data.repositories;

import com.bompotis.netcheck.data.entities.ProtocolCheckEntity;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Created by Kyriakos Bompotis on 15/6/20.
 */
public interface ProtocolCheckRepository extends JpaRepository<ProtocolCheckEntity, String> {}
