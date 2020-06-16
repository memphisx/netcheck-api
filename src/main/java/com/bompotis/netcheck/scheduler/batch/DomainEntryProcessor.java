package com.bompotis.netcheck.scheduler.batch;

import com.bompotis.netcheck.data.entities.DomainCheckEntity;
import com.bompotis.netcheck.data.entities.DomainEntity;
import com.bompotis.netcheck.service.DomainService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemProcessor;

/**
 * Created by Kyriakos Bompotis on 10/6/20.
 */
public class DomainEntryProcessor implements ItemProcessor<DomainEntity, DomainCheckEntity> {

    private final DomainService domainService;

    private static final Logger log = LoggerFactory.getLogger(DomainEntryProcessor.class);

    public DomainEntryProcessor(DomainService domainService) {
        this.domainService = domainService;
    }

    @Override
    public DomainCheckEntity process(DomainEntity domainEntity) throws Exception {
        var status = domainService.check(domainEntity.getDomain());
        log.info("Checking {}",domainEntity.getDomain());
        var historicEntry = domainService.convertToDomainCheckEntity(status);
        log.info("Successfully checked {}. Passing to writer", domainEntity.getDomain());
        historicEntry.setDomainEntity(domainEntity);
        return historicEntry;
    }
}
