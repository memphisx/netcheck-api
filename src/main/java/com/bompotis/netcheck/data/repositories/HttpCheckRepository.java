package com.bompotis.netcheck.data.repositories;

import com.bompotis.netcheck.data.entities.HttpCheckEntity;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Created by Kyriakos Bompotis on 15/6/20.
 */
public interface HttpCheckRepository extends JpaRepository<HttpCheckEntity, String> {}
