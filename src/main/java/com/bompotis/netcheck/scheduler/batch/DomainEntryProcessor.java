package com.bompotis.netcheck.scheduler.batch;

import com.bompotis.netcheck.data.entities.DomainEntity;
import com.bompotis.netcheck.data.entities.DomainHistoricEntryEntity;
import com.bompotis.netcheck.service.DomainService;
import org.springframework.batch.item.ItemProcessor;

/**
 * Created by Kyriakos Bompotis on 10/6/20.
 */
public class DomainEntryProcessor implements ItemProcessor<DomainEntity, DomainHistoricEntryEntity> {

    private final DomainService domainService;

    public DomainEntryProcessor(DomainService domainService) {
        this.domainService = domainService;
    }

    @Override
    public DomainHistoricEntryEntity process(DomainEntity domainEntity) throws Exception {
        var status = domainService.buildAndCheck(domainEntity.getDomain());
        var historicEntry = status.getHistoricEntry();
        historicEntry.setDomainEntity(domainEntity);
        return historicEntry;
    }
}
