package com.bompotis.netcheck.scheduler.batch.writer;

import com.bompotis.netcheck.data.entity.DomainMetricEntity;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.JpaItemWriter;

import javax.persistence.EntityManagerFactory;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Kyriakos Bompotis on 30/6/20.
 */
public class DomainMetricListWriter implements ItemWriter<List<DomainMetricEntity>> {
    protected static final Log logger = LogFactory.getLog(DomainMetricListWriter.class);
    private final JpaItemWriter<DomainMetricEntity> jpaItemWriter;

    public DomainMetricListWriter() {
        this.jpaItemWriter = new JpaItemWriter<>();
    }

    public void setEntityManagerFactory(EntityManagerFactory entityManagerFactory) {
        jpaItemWriter.setEntityManagerFactory(entityManagerFactory);
    }

    public void afterPropertiesSet() throws Exception {
        this.jpaItemWriter.afterPropertiesSet();
    }

    public void setUsePersist(boolean usePersist) {
        this.jpaItemWriter.setUsePersist(usePersist);
    }

    @Override
    public void write(List<? extends List<DomainMetricEntity>> list) throws Exception {
        List<DomainMetricEntity> metricEntities = new ArrayList<>();
        list.forEach(metricEntities::addAll);
        jpaItemWriter.write(metricEntities);
    }
}
