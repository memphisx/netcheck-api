package com.bompotis.netcheck.data.repository;

import com.bompotis.netcheck.data.entity.ProtocolCheckEntity;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Created by Kyriakos Bompotis on 15/6/20.
 */
public interface ProtocolCheckRepository extends JpaRepository<ProtocolCheckEntity, String> {}
