package com.bompotis.netcheck.batch;

import com.bompotis.netcheck.data.entities.DomainEntity;
import com.bompotis.netcheck.data.entities.DomainHistoricEntryEntity;
import com.bompotis.netcheck.data.repositories.DomainHistoricEntryRepository;
import com.bompotis.netcheck.data.repositories.DomainRepository;
import com.bompotis.netcheck.service.DomainService;
import org.springframework.batch.item.ItemProcessor;

/**
 * Created by Kyriakos Bompotis on 10/6/20.
 */
public class DomainEntryProcessor implements ItemProcessor<DomainEntity, DomainHistoricEntryEntity> {

    private final DomainRepository domainRepository;

    private final DomainHistoricEntryRepository domainHistoricEntryRepository;

    public DomainEntryProcessor(DomainRepository domainRepository, DomainHistoricEntryRepository domainHistoricEntryRepository) {
        this.domainHistoricEntryRepository = domainHistoricEntryRepository;
        this.domainRepository = domainRepository;
    }

    @Override
    public DomainHistoricEntryEntity process(DomainEntity domainEntity) throws Exception {
        var service = new DomainService(domainEntity.getDomain(),domainRepository, domainHistoricEntryRepository);
        var status = service.checkCerts();
        var historicEntry = status.getHistoricEntry();
        historicEntry.setDomainEntity(domainEntity);
        return historicEntry;
    }
}
