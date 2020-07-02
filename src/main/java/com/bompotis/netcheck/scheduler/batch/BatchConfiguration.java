package com.bompotis.netcheck.scheduler.batch;

import com.bompotis.netcheck.data.entity.DomainCheckEntity;
import com.bompotis.netcheck.data.entity.DomainEntity;
import com.bompotis.netcheck.data.entity.DomainMetricEntity;
import com.bompotis.netcheck.data.repository.DomainCheckRepository;
import com.bompotis.netcheck.scheduler.batch.processor.DomainCheckProcessor;
import com.bompotis.netcheck.scheduler.batch.processor.DomainMetricProcessor;
import com.bompotis.netcheck.scheduler.batch.writer.DomainMetricListWriter;
import com.bompotis.netcheck.scheduler.batch.writer.NotificationItemWriter;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.JpaItemWriter;
import org.springframework.batch.item.database.JpaPagingItemReader;
import org.springframework.batch.item.support.CompositeItemWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.core.task.TaskExecutor;

import javax.persistence.EntityManagerFactory;
import java.util.List;

/**
 * Created by Kyriakos Bompotis on 10/6/20.
 */
@Configuration
@EnableBatchProcessing
public class BatchConfiguration {

    private final JobBuilderFactory jobBuilderFactory;

    private final StepBuilderFactory stepBuilderFactory;

    private final EntityManagerFactory entityManagerFactory;

    private final DomainCheckRepository domainCheckRepository;

    @Autowired
    public BatchConfiguration(JobBuilderFactory jobBuilderFactory,
                              StepBuilderFactory stepBuilderFactory,
                              EntityManagerFactory entityManagerFactory,
                              DomainCheckRepository domainCheckRepository) {
        this.jobBuilderFactory = jobBuilderFactory;
        this.stepBuilderFactory = stepBuilderFactory;
        this.entityManagerFactory = entityManagerFactory;
        this.domainCheckRepository = domainCheckRepository;
    }

    @Bean
    public JpaPagingItemReader<DomainEntity> domainReader() {
        var reader = new JpaPagingItemReader<DomainEntity>();
        reader.setPageSize(10);
        reader.setQueryString("select d from DomainEntity d");
        reader.setEntityManagerFactory(entityManagerFactory);
        return reader;
    }

    @Bean
    public JpaPagingItemReader<DomainEntity> fiveMinFrequencyDomainReader() {
        var reader = new JpaPagingItemReader<DomainEntity>();
        reader.setPageSize(10);
        reader.setQueryString("select d from DomainEntity d where d.checkFrequency = 5");
        reader.setEntityManagerFactory(entityManagerFactory);
        return reader;
    }

    @Bean
    public JpaPagingItemReader<DomainEntity> tenMinFrequencyDomainReader() {
        var reader = new JpaPagingItemReader<DomainEntity>();
        reader.setPageSize(10);
        reader.setQueryString("select d from DomainEntity d where d.checkFrequency = 10");
        reader.setEntityManagerFactory(entityManagerFactory);
        return reader;
    }

    @Bean
    public JpaPagingItemReader<DomainEntity> fifteenMinFrequencyDomainReader() {
        var reader = new JpaPagingItemReader<DomainEntity>();
        reader.setPageSize(10);
        reader.setQueryString("select d from DomainEntity d where d.checkFrequency = 15");
        reader.setEntityManagerFactory(entityManagerFactory);
        return reader;
    }

    @Bean
    public ItemWriter<DomainCheckEntity> domainCheckEntityWriter(NotificationItemWriter notificationItemWriter) {
        var jpaItemWriter = new JpaItemWriter<DomainCheckEntity>();
        jpaItemWriter.setEntityManagerFactory(entityManagerFactory);
        if(notificationItemWriter.isEnabled()) {
            var compositeWriter = new CompositeItemWriter<DomainCheckEntity>();
            compositeWriter.setDelegates(List.of(jpaItemWriter,notificationItemWriter));
            return compositeWriter;
        } else {
            return jpaItemWriter;
        }
    }

    @Bean
    public DomainMetricListWriter domainMetricEntityListWriter() {
        var writer = new DomainMetricListWriter();
        writer.setEntityManagerFactory(entityManagerFactory);
        return writer;
    }

    @Bean
    public Job fiveMinCheckDomainsStatusJob(JobCompletionNotificationListener listener, Step fiveMinCheckDomainsStep) {
        return jobBuilderFactory.get("fiveMinCheckDomainsStatusJob")
                .incrementer(new RunIdIncrementer())
                .listener(listener)
                .flow(fiveMinCheckDomainsStep)
                .end()
                .build();
    }

    @Bean
    public Job tenMinCheckDomainsStatusJob(JobCompletionNotificationListener listener, Step tenMinCheckDomainsStep) {
        return jobBuilderFactory.get("tenMinCheckDomainsStatusJob")
                .incrementer(new RunIdIncrementer())
                .listener(listener)
                .flow(tenMinCheckDomainsStep)
                .end()
                .build();
    }

    @Bean
    public Job fifteenMinCheckDomainsStatusJob(JobCompletionNotificationListener listener, Step fifteenMinCheckDomainsStep) {
        return jobBuilderFactory.get("fifteenMinCheckDomainsStatusJob")
                .incrementer(new RunIdIncrementer())
                .listener(listener)
                .flow(fifteenMinCheckDomainsStep)
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
    public TaskExecutor asyncTaskExecutor(){
        return new SimpleAsyncTaskExecutor("spring_batch");
    }

    @Bean
    public Step fiveMinCheckDomainsStep(ItemWriter<DomainCheckEntity> domainCheckEntityWriter,
                                        TaskExecutor asyncTaskExecutor,
                                        DomainCheckProcessor domainCheckProcessor) {
        return stepBuilderFactory.get("fiveMinCheckDomainsStep")
                .<DomainEntity, DomainCheckEntity> chunk(10)
                .reader(fiveMinFrequencyDomainReader())
                .processor(domainCheckProcessor)
                .writer(domainCheckEntityWriter)
                .taskExecutor(asyncTaskExecutor)
                .throttleLimit(10)
                .allowStartIfComplete(true)
                .build();
    }

    @Bean
    public Step tenMinCheckDomainsStep(ItemWriter<DomainCheckEntity> domainCheckEntityWriter,
                                       TaskExecutor asyncTaskExecutor,
                                       DomainCheckProcessor domainCheckProcessor) {
        return stepBuilderFactory.get("tenMinCheckDomainsStep")
                .<DomainEntity, DomainCheckEntity> chunk(10)
                .reader(tenMinFrequencyDomainReader())
                .processor(domainCheckProcessor)
                .writer(domainCheckEntityWriter)
                .taskExecutor(asyncTaskExecutor)
                .throttleLimit(10)
                .allowStartIfComplete(true)
                .build();
    }

    @Bean
    public Step fifteenMinCheckDomainsStep(ItemWriter<DomainCheckEntity> domainCheckEntityWriter,
                                           TaskExecutor asyncTaskExecutor,
                                           DomainCheckProcessor domainCheckProcessor) {
        return stepBuilderFactory.get("fifteenMinCheckDomainsStep")
                .<DomainEntity, DomainCheckEntity> chunk(10)
                .reader(fifteenMinFrequencyDomainReader())
                .processor(domainCheckProcessor)
                .writer(domainCheckEntityWriter)
                .taskExecutor(asyncTaskExecutor)
                .throttleLimit(10)
                .allowStartIfComplete(true)
                .build();
    }

    @Bean
    public Step generateHourlyMetricsStep(DomainMetricListWriter domainMetricEntityListWriter, TaskExecutor asyncTaskExecutor) {
        return generateMetricsStep(domainMetricEntityListWriter,asyncTaskExecutor, DomainMetricProcessor.Period.LAST_HOUR);
    }

    @Bean
    public Step generateDailyMetricsStep(DomainMetricListWriter domainMetricEntityListWriter, TaskExecutor asyncTaskExecutor) {
        return generateMetricsStep(domainMetricEntityListWriter,asyncTaskExecutor, DomainMetricProcessor.Period.LAST_DAY);
    }

    @Bean
    public Step generateWeeklyMetricsStep(DomainMetricListWriter domainMetricEntityListWriter, TaskExecutor asyncTaskExecutor) {
        return generateMetricsStep(domainMetricEntityListWriter,asyncTaskExecutor, DomainMetricProcessor.Period.LAST_WEEK);
    }

    private Step generateMetricsStep(DomainMetricListWriter domainMetricEntityListWriter,
                                           TaskExecutor asyncTaskExecutor,
                                           DomainMetricProcessor.Period period) {
        return stepBuilderFactory.get("generate"+ period.name().replace("_","") +"MetricsStep")
                .<DomainEntity, List<DomainMetricEntity>> chunk(10)
                .reader(domainReader())
                .processor(new DomainMetricProcessor(period,domainCheckRepository))
                .writer(domainMetricEntityListWriter)
                .taskExecutor(asyncTaskExecutor)
                .throttleLimit(1)
                .allowStartIfComplete(true)
                .build();
    }
}