package com.bompotis.netcheck.scheduler.batch.processor;

import com.bompotis.netcheck.data.entity.DomainEntity;
import com.bompotis.netcheck.data.entity.DomainMetricEntity;
import com.bompotis.netcheck.service.MetricService;
import org.springframework.batch.item.ItemProcessor;

import java.util.List;

/**
 * Created by Kyriakos Bompotis on 29/6/20.
 */
public class DomainMetricProcessor implements ItemProcessor<DomainEntity, List<DomainMetricEntity>> {

    private final MetricService metricService;

    private final MetricService.ScheduledPeriod period;

    public DomainMetricProcessor(MetricService.ScheduledPeriod period,
                                 MetricService metricService) {
        this.period = period;
        this.metricService = metricService;
    }

    @Override
    public List<DomainMetricEntity> process(DomainEntity domainEntity) {
        return metricService.generateMetric(domainEntity,new MetricService.TimeFrame(this.period));
    }
}
