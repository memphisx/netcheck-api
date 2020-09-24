package com.bompotis.netcheck.data.repository;

import com.bompotis.netcheck.data.entity.ServerMetricDefinitionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Server Metric Definition specific extension of {@link org.springframework.data.jpa.repository.JpaRepository}.
 *
 * @author Kyriakos Bompotis
 */
@Repository
public interface ServerMetricDefinitionRepository extends JpaRepository<ServerMetricDefinitionEntity, String> {
}
