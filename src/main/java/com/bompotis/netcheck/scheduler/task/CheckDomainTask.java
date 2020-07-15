package com.bompotis.netcheck.scheduler.task;

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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Created by Kyriakos Bompotis on 9/6/20.
 */
@Component
public class CheckDomainTask {

    private static final Logger log = LoggerFactory.getLogger(CheckDomainTask.class);

    private final JobLauncher jobLauncher;

    private final Job fiveMinCheckDomainsStatusJob;

    private final Job tenMinCheckDomainsStatusJob;

    private final Job fifteenMinCheckDomainsStatusJob;

    private final Job generateHourlyMetricsJob;

    private final Job generateDailyMetricsJob;

    private final Job generateWeeklyMetricsJob;

    private final Job cleanUpDomainChecksJob;

    @Value("${settings.schedulers.cleanup.enabled:false}")
    private Boolean cleanupEnabled;

    @Autowired
    public CheckDomainTask(JobLauncher jobLauncher,
                           Job fiveMinCheckDomainsStatusJob,
                           Job tenMinCheckDomainsStatusJob,
                           Job fifteenMinCheckDomainsStatusJob,
                           Job generateHourlyMetricsJob,
                           Job generateDailyMetricsJob,
                           Job generateWeeklyMetricsJob,
                           Job cleanUpDomainChecksJob) {
        this.jobLauncher = jobLauncher;
        this.fiveMinCheckDomainsStatusJob = fiveMinCheckDomainsStatusJob;
        this.tenMinCheckDomainsStatusJob = tenMinCheckDomainsStatusJob;
        this.fifteenMinCheckDomainsStatusJob = fifteenMinCheckDomainsStatusJob;
        this.generateHourlyMetricsJob = generateHourlyMetricsJob;
        this.generateDailyMetricsJob = generateDailyMetricsJob;
        this.generateWeeklyMetricsJob = generateWeeklyMetricsJob;
        this.cleanUpDomainChecksJob = cleanUpDomainChecksJob;
    }

    @Scheduled(cron = "0 0 0 2 * *")
    public void deleteOldDomainChecks() throws JobParametersInvalidException, JobExecutionAlreadyRunningException, JobRestartException, JobInstanceAlreadyCompleteException {
        if (cleanupEnabled) {
            JobParameters params = new JobParametersBuilder()
                    .addString("JobID", String.valueOf(System.currentTimeMillis()))
                    .toJobParameters();
            jobLauncher.run(cleanUpDomainChecksJob, params);
        } else {
            log.info("Cleanup scheduler is disabled. Skipping cleanup.");
        }
    }

    @Scheduled(cron = "0 */5 * * * *")
    public void checkDomainsWith5MinFrequency() throws JobParametersInvalidException, JobExecutionAlreadyRunningException, JobRestartException, JobInstanceAlreadyCompleteException {
        JobParameters params = new JobParametersBuilder()
                .addString("JobID", String.valueOf(System.currentTimeMillis()))
                .toJobParameters();
        jobLauncher.run(fiveMinCheckDomainsStatusJob, params);
    }

    @Scheduled(cron = "0 */10 * * * *")
    public void checkDomainsWith10MinFrequency() throws JobParametersInvalidException, JobExecutionAlreadyRunningException, JobRestartException, JobInstanceAlreadyCompleteException {
        JobParameters params = new JobParametersBuilder()
                .addString("JobID", String.valueOf(System.currentTimeMillis()))
                .toJobParameters();
        jobLauncher.run(tenMinCheckDomainsStatusJob, params);
    }

    @Scheduled(cron = "0 */15 * * * *")
    public void checkDomainsWith15MinFrequency() throws JobParametersInvalidException, JobExecutionAlreadyRunningException, JobRestartException, JobInstanceAlreadyCompleteException {
        JobParameters params = new JobParametersBuilder()
                .addString("JobID", String.valueOf(System.currentTimeMillis()))
                .toJobParameters();
        jobLauncher.run(fifteenMinCheckDomainsStatusJob, params);
    }

    @Scheduled(cron = "0 0 * * * *")
    public void hourlyMetrics() throws JobParametersInvalidException, JobExecutionAlreadyRunningException, JobRestartException, JobInstanceAlreadyCompleteException {
        JobParameters params = new JobParametersBuilder()
                .addString("JobID", String.valueOf(System.currentTimeMillis()))
                .toJobParameters();
        jobLauncher.run(generateHourlyMetricsJob, params);
    }

    @Scheduled(cron = "0 0 0 * * *")
    public void dailyMetrics() throws JobParametersInvalidException, JobExecutionAlreadyRunningException, JobRestartException, JobInstanceAlreadyCompleteException {
        JobParameters params = new JobParametersBuilder()
                .addString("JobID", String.valueOf(System.currentTimeMillis()))
                .toJobParameters();
        jobLauncher.run(generateDailyMetricsJob, params);
    }

    @Scheduled(cron = "0 0 0 * * 0")
    public void weeklyMetrics() throws JobParametersInvalidException, JobExecutionAlreadyRunningException, JobRestartException, JobInstanceAlreadyCompleteException {
        JobParameters params = new JobParametersBuilder()
                .addString("JobID", String.valueOf(System.currentTimeMillis()))
                .toJobParameters();
        jobLauncher.run(generateWeeklyMetricsJob, params);
    }
}
