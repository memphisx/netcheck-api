package com.bompotis.netcheck.tasks;

import com.bompotis.netcheck.data.repositories.DomainHistoricEntryRepository;
import com.bompotis.netcheck.data.repositories.DomainRepository;
import com.bompotis.netcheck.service.DomainService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.JobParametersInvalidException;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;
import org.springframework.batch.core.repository.JobRestartException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

/**
 * Created by Kyriakos Bompotis on 9/6/20.
 */
@Component
public class CheckDomainTask {

    private final JobLauncher jobLauncher;

    private final Job checkDomainsStatusJob;

    @Autowired
    public CheckDomainTask(JobLauncher jobLauncher, Job checkDomainsStatusJob) {
        this.jobLauncher = jobLauncher;
        this.checkDomainsStatusJob = checkDomainsStatusJob;
    }

    @Scheduled(cron = "0 */10 * * * *")
    public void reportCurrentTime() throws JobParametersInvalidException, JobExecutionAlreadyRunningException, JobRestartException, JobInstanceAlreadyCompleteException {
        JobParameters params = new JobParametersBuilder()
                .addString("JobID", String.valueOf(System.currentTimeMillis()))
                .toJobParameters();
        jobLauncher.run(checkDomainsStatusJob, params);
    }
}
