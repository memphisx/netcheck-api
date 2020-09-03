/*
 * Copyright 2020 the original author or authors.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This code is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package com.bompotis.netcheck.scheduler.batch;

import com.bompotis.netcheck.data.entity.DomainCheckEntity;
import com.bompotis.netcheck.data.entity.DomainEntity;
import com.bompotis.netcheck.data.entity.DomainMetricEntity;
import com.bompotis.netcheck.data.repository.DomainCheckRepository;
import com.bompotis.netcheck.data.repository.DomainRepository;
import com.bompotis.netcheck.scheduler.batch.processor.DomainCheckProcessor;
import com.bompotis.netcheck.scheduler.batch.processor.DomainMetricProcessor;
import com.bompotis.netcheck.scheduler.batch.reader.OldDomainCheckItemReader;
import com.bompotis.netcheck.scheduler.batch.writer.CheckEventItemWriter;
import com.bompotis.netcheck.scheduler.batch.writer.DomainMetricListWriter;
import com.bompotis.netcheck.scheduler.batch.writer.NotificationItemWriter;
import com.bompotis.netcheck.service.MetricService;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.integration.async.AsyncItemProcessor;
import org.springframework.batch.integration.async.AsyncItemWriter;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.data.RepositoryItemReader;
import org.springframework.batch.item.data.RepositoryItemWriter;
import org.springframework.batch.item.database.JpaItemWriter;
import org.springframework.batch.item.support.CompositeItemWriter;
import org.springframework.batch.item.support.PassThroughItemProcessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.ApplicationEventMulticaster;
import org.springframework.context.event.SimpleApplicationEventMulticaster;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.core.task.TaskExecutor;
import org.springframework.data.domain.Sort;
import org.springframework.scheduling.support.TaskUtils;

import javax.persistence.EntityManagerFactory;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;

/**
 * Created by Kyriakos Bompotis on 10/6/20.
 */
@Configuration
@EnableBatchProcessing
public class BatchConfiguration {

    private final JobBuilderFactory jobBuilderFactory;

    private final StepBuilderFactory stepBuilderFactory;

    private final DomainRepository domainRepository;

    private final MetricService metricService;

    private final DomainCheckRepository domainCheckRepository;

    private final EntityManagerFactory entityManagerFactory;

    @Value("${settings.schedulers.cleanup.deleteOlderThan:1}")
    private Integer cleanupThreshold;

    @Autowired
    public BatchConfiguration(JobBuilderFactory jobBuilderFactory,
                              StepBuilderFactory stepBuilderFactory,
                              DomainCheckRepository domainCheckRepository,
                              EntityManagerFactory entityManagerFactory,
                              DomainRepository domainRepository,
                              MetricService metricService) {
        this.jobBuilderFactory = jobBuilderFactory;
        this.stepBuilderFactory = stepBuilderFactory;
        this.entityManagerFactory = entityManagerFactory;
        this.domainCheckRepository = domainCheckRepository;
        this.domainRepository = domainRepository;
        this.metricService = metricService;
    }


    @Bean
    public ApplicationEventMulticaster applicationEventMulticaster() {
        SimpleApplicationEventMulticaster eventMulticaster = new SimpleApplicationEventMulticaster();
        eventMulticaster.setTaskExecutor(new SimpleAsyncTaskExecutor());
        eventMulticaster.setErrorHandler(TaskUtils.LOG_AND_SUPPRESS_ERROR_HANDLER);
        return eventMulticaster;
    }

    @Bean
    public ItemReader<DomainEntity> domainReader() {
        var reader = new RepositoryItemReader<DomainEntity>();
        reader.setPageSize(100);
        reader.setMethodName("findAll");
        reader.setSort(Map.of("createdAt", Sort.Direction.ASC));
        reader.setRepository(domainRepository);
        return reader;
    }

    @Bean
    public ItemReader<DomainCheckEntity> olderThanThreeMonthsDomainCheckReader() {
        return new OldDomainCheckItemReader(domainCheckRepository, cleanupThreshold);
    }


    @Bean
    public ItemWriter<DomainCheckEntity> domainCheckEntityWriter(
            NotificationItemWriter notificationItemWriter,
            CheckEventItemWriter checkEventItemWriter) {
        var jpaItemWriter = new JpaItemWriter<DomainCheckEntity>();
        jpaItemWriter.setEntityManagerFactory(entityManagerFactory);
        var compositeWriter = new CompositeItemWriter<DomainCheckEntity>();
        if(notificationItemWriter.isEnabled()) {
            compositeWriter.setDelegates(List.of(jpaItemWriter, notificationItemWriter, checkEventItemWriter));
        } else {
            compositeWriter.setDelegates(List.of(jpaItemWriter, checkEventItemWriter));
        }
        return compositeWriter;
    }


    @Bean
    public ItemWriter<DomainCheckEntity> domainCheckEntityDeleter() {
        var itemDeleter = new RepositoryItemWriter<DomainCheckEntity>();
        itemDeleter.setRepository(domainCheckRepository);
        itemDeleter.setMethodName("delete");
        return itemDeleter;
    }

    @Bean
    public DomainMetricListWriter domainMetricEntityListWriter() {
        var writer = new DomainMetricListWriter();
        writer.setEntityManagerFactory(entityManagerFactory);
        return writer;
    }

    @Bean
    public Job checkDomainsStatusJob(JobCompletionNotificationListener listener, Step checkDomainsStep) {
        return jobBuilderFactory.get("checkDomainsStatusJob")
                .incrementer(new RunIdIncrementer())
                .listener(listener)
                .flow(checkDomainsStep)
                .end()
                .build();
    }

    @Bean
    public Job cleanUpDomainChecksJob(JobCompletionNotificationListener listener, Step cleanUpDomainChecksStep) {
        return jobBuilderFactory.get("cleanUpDomainChecksJob")
                .incrementer(new RunIdIncrementer())
                .listener(listener)
                .flow(cleanUpDomainChecksStep)
                .end()
                .build();
    }

    @Bean
    public Job generateHourlyMetricsJob(JobCompletionNotificationListener listener, Step generateHourlyMetricsStep) {
        return jobBuilderFactory.get("generateHourlyMetricsJob")
                .incrementer(new RunIdIncrementer())
                .listener(listener)
                .flow(generateHourlyMetricsStep)
                .end()
                .build();
    }

    @Bean
    public Job generateDailyMetricsJob(JobCompletionNotificationListener listener, Step generateDailyMetricsStep) {
        return jobBuilderFactory.get("generateDailyMetricsJob")
                .incrementer(new RunIdIncrementer())
                .listener(listener)
                .flow(generateDailyMetricsStep)
                .end()
                .build();
    }

    @Bean
    public Job generateWeeklyMetricsJob(JobCompletionNotificationListener listener, Step generateWeeklyMetricsStep) {
        return jobBuilderFactory.get("generateWeeklyMetricsJob")
                .incrementer(new RunIdIncrementer())
                .listener(listener)
                .flow(generateWeeklyMetricsStep)
                .end()
                .build();
    }

    @Bean
    public TaskExecutor asyncItemProcessorExecutor(){
        return new SimpleAsyncTaskExecutor("item_processor_executor");
    }

    @Bean
    public TaskExecutor asyncDomainCheckExecutor(){
        return new SimpleAsyncTaskExecutor("domain_check_step_executor");
    }

    @Bean
    public TaskExecutor asyncCleanUpExecutor(){
        return new SimpleAsyncTaskExecutor("cleanup_step_executor");
    }

    @Bean
    public TaskExecutor asyncMetricExecutor(){
        return new SimpleAsyncTaskExecutor("metric_step_executor");
    }

    @Bean
    public AsyncItemProcessor<DomainEntity, DomainCheckEntity> asyncBatchCheckProcessor(
            DomainCheckProcessor domainCheckProcessor,
            TaskExecutor asyncItemProcessorExecutor) {
        var itemProcessor = new AsyncItemProcessor<DomainEntity, DomainCheckEntity>();
        itemProcessor.setDelegate(domainCheckProcessor);
        itemProcessor.setTaskExecutor(asyncItemProcessorExecutor);
        return itemProcessor;
    }

    @Bean
    public AsyncItemWriter<DomainCheckEntity> asyncDomainCheckEntityWriter(
            ItemWriter<DomainCheckEntity> domainCheckEntityWriter) {
        var asyncItemWriter = new AsyncItemWriter<DomainCheckEntity>();
        asyncItemWriter.setDelegate(domainCheckEntityWriter);
        return asyncItemWriter;
    }

    @Bean
    public Step checkDomainsStep(AsyncItemWriter<DomainCheckEntity> asyncDomainCheckEntityWriter,
                                 TaskExecutor asyncDomainCheckExecutor,
                                 AsyncItemProcessor<DomainEntity, DomainCheckEntity> asyncBatchCheckProcessor,
                                 ItemReader<DomainEntity> domainReader) {
        return stepBuilderFactory.get("checkDomainsStep")
                .<DomainEntity, Future<DomainCheckEntity>> chunk(100)
                .reader(domainReader)
                .processor(asyncBatchCheckProcessor)
                .writer(asyncDomainCheckEntityWriter)
                .taskExecutor(asyncDomainCheckExecutor)
                .throttleLimit(10)
                .allowStartIfComplete(true)
                .build();
    }

    @Bean
    public Step cleanUpDomainChecksStep(ItemReader<DomainCheckEntity> olderThanThreeMonthsDomainCheckReader,
                                        ItemWriter<DomainCheckEntity> domainCheckEntityDeleter,
                                        TaskExecutor asyncCleanUpExecutor) {
        return stepBuilderFactory.get("cleanUpDomainChecksStep")
                .<DomainCheckEntity, DomainCheckEntity> chunk(100)
                .reader(olderThanThreeMonthsDomainCheckReader)
                .processor(new PassThroughItemProcessor<>())
                .writer(domainCheckEntityDeleter)
                .taskExecutor(asyncCleanUpExecutor)
                .throttleLimit(10)
                .allowStartIfComplete(true)
                .build();
    }

    @Bean
    public Step generateHourlyMetricsStep(DomainMetricListWriter domainMetricEntityListWriter,
                                          TaskExecutor asyncCleanUpExecutor,
                                          ItemReader<DomainEntity> domainReader) {
        return generateMetricsStep(
                domainMetricEntityListWriter,
                asyncCleanUpExecutor,
                MetricService.ScheduledPeriod.LAST_HOUR,
                domainReader
        );
    }

    @Bean
    public Step generateDailyMetricsStep(DomainMetricListWriter domainMetricEntityListWriter,
                                         TaskExecutor asyncMetricExecutor,
                                         ItemReader<DomainEntity> domainReader) {
        return generateMetricsStep(
                domainMetricEntityListWriter,
                asyncMetricExecutor,
                MetricService.ScheduledPeriod.LAST_DAY,
                domainReader);
    }

    @Bean
    public Step generateWeeklyMetricsStep(DomainMetricListWriter domainMetricEntityListWriter,
                                          TaskExecutor asyncMetricExecutor,
                                          ItemReader<DomainEntity> domainReader) {
        return generateMetricsStep(
                domainMetricEntityListWriter,
                asyncMetricExecutor,
                MetricService.ScheduledPeriod.LAST_WEEK,
                domainReader
        );
    }

    private Step generateMetricsStep(DomainMetricListWriter domainMetricEntityListWriter,
                                     TaskExecutor asyncTaskExecutor,
                                     MetricService.ScheduledPeriod period,
                                     ItemReader<DomainEntity> domainReader) {
        return stepBuilderFactory.get("generate"+ period.name().replace("_","") +"MetricsStep")
                .<DomainEntity, List<DomainMetricEntity>> chunk(10)
                .reader(domainReader)
                .processor(new DomainMetricProcessor(period,metricService))
                .writer(domainMetricEntityListWriter)
                .taskExecutor(asyncTaskExecutor)
                .throttleLimit(1)
                .allowStartIfComplete(true)
                .build();
    }
}