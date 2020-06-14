package com.bompotis.netcheck.data.repositories;

import com.bompotis.netcheck.data.entities.HttpsCheckEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Created by Kyriakos Bompotis on 14/6/20.
 */
@Repository
public interface HttpsCheckRepository extends JpaRepository<HttpsCheckEntity, String> {}
