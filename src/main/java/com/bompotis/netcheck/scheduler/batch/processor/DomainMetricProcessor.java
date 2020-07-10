package com.bompotis.netcheck.scheduler.batch.processor;

import com.bompotis.netcheck.data.entity.DomainCheckEntity;
import com.bompotis.netcheck.data.entity.DomainEntity;
import com.bompotis.netcheck.data.entity.DomainMetricEntity;
import com.bompotis.netcheck.data.entity.ProtocolCheckEntity;
import com.bompotis.netcheck.data.repository.DomainCheckRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemProcessor;

import java.lang.reflect.Method;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by Kyriakos Bompotis on 29/6/20.
 */
public class DomainMetricProcessor implements ItemProcessor<DomainEntity, List<DomainMetricEntity>> {

    public enum Period{
        LAST_HOUR,
        LAST_DAY,
        LAST_WEEK
    }

    private static final Logger log = LoggerFactory.getLogger(DomainMetricProcessor.class);

    private final DomainCheckRepository domainCheckRepository;

    private final Period period;

    public DomainMetricProcessor(Period period,
                                 DomainCheckRepository domainCheckRepository) {
        this.period = period;
        this.domainCheckRepository = domainCheckRepository;
    }

    @Override
    public List<DomainMetricEntity> process(DomainEntity domainEntity) {
        return generateMetric(domainEntity,new TimeFrame(this.period));
    }

    public List<DomainMetricEntity> generateMetric(DomainEntity domainEntity, TimeFrame timeframe) {
        log.info("Generating last {} metrics for {}", timeframe.getPeriod().name().toLowerCase(), domainEntity.getDomain());
        var endDate = timeframe.getEndDate();
        var startDate = timeframe.getStartDate();
        var checkEntities = domainCheckRepository.findAllBetweenDates(domainEntity.getDomain(),startDate,endDate);

        var httpUpChecks = 0;
        var totalHttpChecks = 0;
        var httpsUpChecks = 0;
        var totalHttpsChecks = 0;

        for (var checkEntity: checkEntities) {
            for (var httpCheck : checkEntity.getProtocolCheckEntities()) {
                final AtomicBoolean isUp = new AtomicBoolean(false);
                Optional.ofNullable(httpCheck.getStatusCode()).ifPresent(
                        (code) -> {
                            if (code < 400 ) {
                                isUp.set(true);
                            }
                        }
                );
                if (httpCheck.getProtocol().equals(ProtocolCheckEntity.Protocol.HTTP)) {
                    totalHttpChecks++;
                    httpUpChecks = isUp.get() ? httpUpChecks + 1 : httpUpChecks;
                } else {
                    totalHttpsChecks++;
                    httpsUpChecks = isUp.get() ? httpsUpChecks + 1 : httpsUpChecks;
                }
            }
        }
        var httpMetrics = new Stats(checkEntities, ProtocolCheckEntity.Protocol.HTTP);

        var httpMetricEntity = new DomainMetricEntity.Builder()
                .domain(domainEntity.getDomain())
                .domainEntity(domainEntity)
                .minResponseTimeNs(httpMetrics.getMinResponseTime())
                .maxResponseTimeNs(httpMetrics.getMaxResponseTime())
                .avgResponseTimeNs(httpMetrics.getAvgResponseTime())
                .endPeriod(endDate)
                .startPeriod(startDate)
                .periodType(timeframe.getPeriod())
                .successfulChecks(httpUpChecks)
                .totalChecks(totalHttpChecks)
                .protocol("HTTP")
                .build();

        var httpsMetrics = new Stats(checkEntities, ProtocolCheckEntity.Protocol.HTTP);

        var httpsMetricEntity = new DomainMetricEntity.Builder()
                .domain(domainEntity.getDomain())
                .domainEntity(domainEntity)
                .minResponseTimeNs(httpsMetrics.getMinResponseTime())
                .maxResponseTimeNs(httpsMetrics.getMaxResponseTime())
                .avgResponseTimeNs(httpsMetrics.getAvgResponseTime())
                .endPeriod(endDate)
                .startPeriod(startDate)
                .periodType(timeframe.getPeriod())
                .successfulChecks(httpsUpChecks)
                .totalChecks(totalHttpsChecks)
                .protocol("HTTPS")
                .build();

        return List.of(httpMetricEntity,httpsMetricEntity);
    }

    private static class Stats {
        private final Long minResponseTime;
        private final Long maxResponseTime;
        private final Long avgResponseTime;

        private Stats(Set<DomainCheckEntity> checkEntities, ProtocolCheckEntity.Protocol protocol) {
            Long min;
            Long max;
            Long avg;

            try {
                var stats = (protocol.equals(ProtocolCheckEntity.Protocol.HTTP)) ?  getHttpStats(checkEntities) : getHttpsStats(checkEntities);
                min = stats.getMin();
                max = stats.getMax();
                avg = Math.round(stats.getAverage());
            } catch (NullPointerException e) {
                min = null;
                max = null;
                avg = null;
            }
            this.maxResponseTime = max;
            this.minResponseTime = min;
            this.avgResponseTime = avg;
        }

        public Long getMinResponseTime() {
            return minResponseTime;
        }

        public Long getMaxResponseTime() {
            return maxResponseTime;
        }

        public Long getAvgResponseTime() {
            return avgResponseTime;
        }

        private LongSummaryStatistics getHttpStats(Set<DomainCheckEntity> checkEntities) {
            return checkEntities
                    .stream()
                    .filter(Objects::nonNull)
                    .mapToLong(DomainCheckEntity::getHttpResponseTimeNs)
                    .summaryStatistics();
        }

        private LongSummaryStatistics getHttpsStats(Set<DomainCheckEntity> checkEntities) {
            return checkEntities
                    .stream()
                    .filter(Objects::nonNull)
                    .mapToLong(DomainCheckEntity::getHttpsResponseTimeNs)
                    .summaryStatistics();
        }
    }

    private static class TimeFrame {
        private final Date startDate;
        private final Date endDate;
        private final DomainMetricEntity.Period period;

        TimeFrame(Period period) {
            if (period.equals(Period.LAST_HOUR)) {
                this.period = DomainMetricEntity.Period.HOUR;
                this.endDate = Date.from(ZonedDateTime.now().minusHours(1).withMinute(59).withSecond(59).toInstant());
                this.startDate = Date.from(ZonedDateTime.now().minusHours(1).withMinute(0).withSecond(0).toInstant());
            } else if (period.equals(Period.LAST_DAY)) {
                this.period = DomainMetricEntity.Period.DAY;
                this.endDate = Date.from(ZonedDateTime.now().minusDays(1).withHour(23).withMinute(59).withSecond(59).toInstant());
                this.startDate = Date.from(ZonedDateTime.now().minusDays(1).withHour(0).withMinute(0).withSecond(0).toInstant());
            } else if (period.equals(Period.LAST_WEEK)) {
                this.period = DomainMetricEntity.Period.WEEK;
                this.endDate = Date.from(ZonedDateTime.now().minusDays(1).withHour(23).withMinute(59).withSecond(59).toInstant());
                this.startDate = Date.from(ZonedDateTime.now().minusDays(7).withHour(0).withMinute(0).withSecond(0).toInstant());
            } else {
                this.period = DomainMetricEntity.Period.MONTH;
                this.endDate = Date.from(ZonedDateTime.now().minusDays(1).withHour(23).withMinute(59).withSecond(59).toInstant());
                this.startDate = Date.from(ZonedDateTime.now().minusMonths(1).withHour(0).withMinute(0).withSecond(0).toInstant());
            }
        }

        public Date getStartDate() {
            return startDate;
        }

        public Date getEndDate() {
            return endDate;
        }

        public DomainMetricEntity.Period getPeriod() {
            return period;
        }
    }
}
