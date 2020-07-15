package com.bompotis.netcheck.service;

import com.bompotis.netcheck.data.entity.DomainCheckEntity;
import com.bompotis.netcheck.data.entity.DomainEntity;
import com.bompotis.netcheck.data.entity.DomainMetricEntity;
import com.bompotis.netcheck.data.entity.ProtocolCheckEntity;
import com.bompotis.netcheck.data.repository.DomainCheckRepository;
import com.bompotis.netcheck.data.repository.DomainMetricRepository;
import com.bompotis.netcheck.data.repository.DomainRepository;
import com.bompotis.netcheck.service.dto.MetricDto;
import com.bompotis.netcheck.service.dto.PaginatedDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Service;

import java.time.ZonedDateTime;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by Kyriakos Bompotis on 13/7/20.
 */
@Service
public class MetricService extends AbstractService{

    private static final Logger log = LoggerFactory.getLogger(MetricService.class);

    private final DomainMetricRepository domainMetricRepository;

    private final DomainCheckRepository domainCheckRepository;

    private final DomainRepository domainRepository;

    @Autowired
    public MetricService(DomainMetricRepository domainMetricRepository,
                         DomainCheckRepository domainCheckRepository,
                         DomainRepository domainRepository) {
        this.domainMetricRepository = domainMetricRepository;
        this.domainCheckRepository = domainCheckRepository;
        this.domainRepository = domainRepository;
    }

    public enum CalculatedPeriod {
        THIS_HOUR,
        THIS_DAY,
        THIS_WEEK,
        THIS_MONTH
    }

    public enum ScheduledPeriod{
        LAST_HOUR,
        LAST_DAY,
        LAST_WEEK
    }

    public static class StringToPeriodConverter implements Converter<String, DomainMetricEntity.Period> {
        @Override
        public DomainMetricEntity.Period convert(String source) {
            try {
                return DomainMetricEntity.Period.valueOf(source.toUpperCase());
            } catch (IllegalArgumentException e) {
                return null;
            }
        }
    }

    public static class StringToCalculatedPeriodConverter implements Converter<String, CalculatedPeriod> {
        @Override
        public CalculatedPeriod convert(String source) {
            try {
                return CalculatedPeriod.valueOf(source.toUpperCase());
            } catch (IllegalArgumentException e) {
                return null;
            }
        }
    }

    public PaginatedDto<MetricDto> getDomainMetrics(String domain, String period, String protocol, Integer page, Integer size) {
        var domainMetricPeriod = Optional.ofNullable(new StringToPeriodConverter().convert(period));
        if (domainMetricPeriod.isPresent()) {
            return getStoredDomainMetrics(domain,domainMetricPeriod.get(),protocol,page,size);
        }
        var calculatedPeriod = Optional.ofNullable(new StringToCalculatedPeriodConverter().convert(period));
        if (calculatedPeriod.isPresent()) {
            return getCalculatedDomainMetrics(domain,calculatedPeriod.get(),protocol);
        }
        throw new IllegalArgumentException();
    }

    public PaginatedDto<MetricDto> getStoredDomainMetrics(String domain, DomainMetricEntity.Period period, String protocol, Integer page, Integer size) {
        var httpMetrics = new ArrayList<MetricDto>();
        var domainMetricEntities =
                domainMetricRepository.findAllByDomainAndProtocolAndPeriodType(
                        domain,
                        DomainMetricEntity.Protocol.valueOf(protocol),
                        period,
                        getDefaultPageRequest(page, size)
                );

        for (var domainMetricEntity : domainMetricEntities) {
            var httpMetric = new MetricDto.Builder()
                    .maxResponseTime(domainMetricEntity.getMaxResponseTimeNs())
                    .totalChecks(domainMetricEntity.getTotalChecks())
                    .successfulChecks(domainMetricEntity.getSuccessfulChecks())
                    .averageResponseTime(domainMetricEntity.getAvgResponseTimeNs())
                    .minResponseTime(domainMetricEntity.getMinResponseTimeNs())
                    .metricPeriodStart(domainMetricEntity.getStartPeriod())
                    .metricPeriodEnd(domainMetricEntity.getEndPeriod())
                    .protocol(protocol)
                    .build();
            httpMetrics.add(httpMetric);
        }
        return new PaginatedDto<>(
                httpMetrics,
                domainMetricEntities.getTotalElements(),
                domainMetricEntities.getTotalPages(),
                domainMetricEntities.getNumber(),
                domainMetricEntities.getNumberOfElements()
        );
    }

    private PaginatedDto<MetricDto> getCalculatedDomainMetrics(String domain, MetricService.CalculatedPeriod calculatedPeriod, String protocol) {
        var domainEntity = domainRepository.findById(domain);
        if (domainEntity.isPresent()) {
            for (var metric : generateMetric(domainEntity.get(),new TimeFrame(calculatedPeriod))) {
                if (metric.getProtocol().equals(DomainMetricEntity.Protocol.valueOf(protocol.toUpperCase()))) {
                    return new PaginatedDto<>(
                            List.of(new MetricDto.Builder().maxResponseTime(metric.getMaxResponseTimeNs())
                                    .totalChecks(metric.getTotalChecks())
                                    .successfulChecks(metric.getSuccessfulChecks())
                                    .averageResponseTime(metric.getAvgResponseTimeNs())
                                    .minResponseTime(metric.getMinResponseTimeNs())
                                    .metricPeriodStart(metric.getStartPeriod())
                                    .metricPeriodEnd(metric.getEndPeriod())
                                    .protocol(protocol)
                                    .build()),
                            1,
                            1,
                            0,
                            1
                    );
                }
            }
        }
        return null;
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

        var httpsMetrics = new Stats(checkEntities, ProtocolCheckEntity.Protocol.HTTPS);

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

    public static class TimeFrame {
        private final Date startDate;
        private final Date endDate;
        private final DomainMetricEntity.Period period;

        public TimeFrame(ScheduledPeriod period) {
            switch (period) {
                case LAST_HOUR:
                    this.period = DomainMetricEntity.Period.HOUR;
                    this.endDate = Date.from(ZonedDateTime.now().minusHours(1).withMinute(59).withSecond(59).toInstant());
                    this.startDate = Date.from(ZonedDateTime.now().minusHours(1).withMinute(0).withSecond(0).toInstant());
                    break;
                case LAST_DAY:
                    this.period = DomainMetricEntity.Period.DAY;
                    this.endDate = Date.from(ZonedDateTime.now().minusDays(1).withHour(23).withMinute(59).withSecond(59).toInstant());
                    this.startDate = Date.from(ZonedDateTime.now().minusDays(1).withHour(0).withMinute(0).withSecond(0).toInstant());
                    break;
                case LAST_WEEK:
                    this.period = DomainMetricEntity.Period.WEEK;
                    this.endDate = Date.from(ZonedDateTime.now().minusDays(1).withHour(23).withMinute(59).withSecond(59).toInstant());
                    this.startDate = Date.from(ZonedDateTime.now().minusDays(7).withHour(0).withMinute(0).withSecond(0).toInstant());
                    break;
                default:
                    this.period = DomainMetricEntity.Period.MONTH;
                    this.endDate = Date.from(ZonedDateTime.now().minusDays(1).withHour(23).withMinute(59).withSecond(59).toInstant());
                    this.startDate = Date.from(ZonedDateTime.now().minusMonths(1).withHour(0).withMinute(0).withSecond(0).toInstant());
                    break;
            }
        }

        public TimeFrame(CalculatedPeriod period) {
            this.endDate = Date.from(ZonedDateTime.now().toInstant());
            switch (period) {
                case THIS_HOUR:
                    this.period = DomainMetricEntity.Period.HOUR;
                    this.startDate = Date.from(ZonedDateTime.now().minusHours(1).toInstant());
                    break;
                case THIS_DAY:
                    this.period = DomainMetricEntity.Period.DAY;
                    this.startDate = Date.from(ZonedDateTime.now().minusDays(1).toInstant());
                    break;
                case THIS_WEEK:
                    this.period = DomainMetricEntity.Period.WEEK;
                    this.startDate = Date.from(ZonedDateTime.now().minusDays(7).toInstant());
                    break;
                case THIS_MONTH:
                    this.period = DomainMetricEntity.Period.MONTH;
                    this.startDate = Date.from(ZonedDateTime.now().minusMonths(1).toInstant());
                    break;
                default:
                    throw new IllegalArgumentException();
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
