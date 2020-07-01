package com.bompotis.netcheck.scheduler.batch.processor;

import com.bompotis.netcheck.data.entity.DomainCheckEntity;
import com.bompotis.netcheck.data.entity.DomainEntity;
import com.bompotis.netcheck.data.entity.DomainMetricEntity;
import com.bompotis.netcheck.data.entity.ProtocolCheckEntity;
import com.bompotis.netcheck.data.repository.DomainCheckRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemProcessor;

import java.time.ZonedDateTime;
import java.util.Date;
import java.util.List;
import java.util.Optional;
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
        return generateMetric(domainEntity,new Timeframe(this.period));
    }

    public List<DomainMetricEntity> generateMetric(DomainEntity domainEntity, Timeframe timeframe) {
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
        var httpMetrics = checkEntities
                .stream()
                .mapToLong(DomainCheckEntity::getHttpResponseTimeNs)
                .summaryStatistics();
        var httpMetricEntity = new DomainMetricEntity.Builder()
                .domain(domainEntity.getDomain())
                .domainEntity(domainEntity)
                .minResponseTimeNs(httpMetrics.getMin())
                .maxResponseTimeNs(httpMetrics.getMax())
                .avgResponseTimeNs(Math.round(httpMetrics.getAverage()))
                .endPeriod(endDate)
                .startPeriod(startDate)
                .periodType(timeframe.getPeriod())
                .successfulChecks(httpUpChecks)
                .totalChecks(totalHttpChecks)
                .protocol("HTTP")
                .build();

        var httpsMetrics = checkEntities
                .stream()
                .mapToLong(DomainCheckEntity::getHttpsResponseTimeNs)
                .summaryStatistics();

        var httpsMetricEntity = new DomainMetricEntity.Builder()
                .domain(domainEntity.getDomain())
                .domainEntity(domainEntity)
                .minResponseTimeNs(httpsMetrics.getMin())
                .maxResponseTimeNs(httpsMetrics.getMax())
                .avgResponseTimeNs(Math.round(httpsMetrics.getAverage()))
                .endPeriod(endDate)
                .startPeriod(startDate)
                .periodType(timeframe.getPeriod())
                .successfulChecks(httpsUpChecks)
                .totalChecks(totalHttpsChecks)
                .protocol("HTTPS")
                .build();

        return List.of(httpMetricEntity,httpsMetricEntity);
    }

    private static class Timeframe {
        private final Date startDate;
        private final Date endDate;
        private final DomainMetricEntity.Period period;

        Timeframe(Period period) {
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
