package com.bompotis.netcheck.scheduler.batch;

import com.bompotis.netcheck.data.entities.DomainCheckEntity;
import com.bompotis.netcheck.data.entities.DomainEntity;
import com.bompotis.netcheck.service.DomainService;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.item.database.JpaItemWriter;
import org.springframework.batch.item.database.JpaPagingItemReader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.persistence.EntityManagerFactory;

/**
 * Created by Kyriakos Bompotis on 10/6/20.
 */
@Configuration
@EnableBatchProcessing
public class BatchConfiguration {

    private final JobBuilderFactory jobBuilderFactory;

    private final StepBuilderFactory stepBuilderFactory;

    private final EntityManagerFactory entityManagerFactory;

    private final DomainService domainService;

    @Autowired
    public BatchConfiguration(JobBuilderFactory jobBuilderFactory,
                              StepBuilderFactory stepBuilderFactory,
                              DomainService domainService,
                              EntityManagerFactory entityManagerFactory) {
        this.jobBuilderFactory = jobBuilderFactory;
        this.stepBuilderFactory = stepBuilderFactory;
        this.entityManagerFactory = entityManagerFactory;
        this.domainService = domainService;
    }

    @Bean
    public JpaPagingItemReader<DomainEntity> reader() {
        var reader = new JpaPagingItemReader<DomainEntity>();
        reader.setPageSize(10);
        reader.setQueryString("select d from DomainEntity d");
        reader.setEntityManagerFactory(entityManagerFactory);
        return reader;
    }

    @Bean
    public DomainEntryProcessor processor() {
        return new DomainEntryProcessor(domainService);
    }

    @Bean
    public JpaItemWriter<DomainCheckEntity> writer() {
        var writer = new JpaItemWriter<DomainCheckEntity>();
        writer.setEntityManagerFactory(entityManagerFactory);
        return writer;
    }

    @Bean
    public Job checkDomainsStatusJob(JobCompletionNotificationListener listener, Step step1) {
        return jobBuilderFactory.get("importUserJob")
                .incrementer(new RunIdIncrementer())
                .listener(listener)
                .flow(step1)
                .end()
                .build();
    }

    @Bean
    public Step step1(JpaItemWriter<DomainCheckEntity> writer) {
        return stepBuilderFactory.get("step1")
                .<DomainEntity, DomainCheckEntity> chunk(10)
                .reader(reader())
                .processor(processor())
                .writer(writer)
                .allowStartIfComplete(true)
                .build();
    }
}