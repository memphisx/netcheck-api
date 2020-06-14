package com.bompotis.netcheck.data.repositories;

import com.bompotis.netcheck.data.entities.DomainCertificateEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Created by Kyriakos Bompotis on 14/6/20.
 */
@Repository
public interface DomainCertificateRepository extends JpaRepository<DomainCertificateEntity, String> {}
