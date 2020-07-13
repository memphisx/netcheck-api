package com.bompotis.netcheck.scheduler.batch.reader;

import com.bompotis.netcheck.data.entity.DomainCheckEntity;
import com.bompotis.netcheck.data.repository.DomainCheckRepository;
import org.springframework.batch.item.data.RepositoryItemReader;
import org.springframework.data.domain.Sort;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Created by Kyriakos Bompotis on 13/7/20.
 */
public class OldDomainCheckItemReader extends RepositoryItemReader<DomainCheckEntity> {

    private final int threshold;

    public OldDomainCheckItemReader(DomainCheckRepository domainCheckRepository, Integer threshold) {
        this.setRepository(domainCheckRepository);
        this.setMethodName("findAllCheckedBeforeDate");
        this.setPageSize(10);
        this.setSort(Map.of("createdAt", Sort.Direction.ASC));
        if (threshold > 0) {
            this.threshold = threshold;
        }
        else {
            this.threshold = 3;
        }
    }

    @Override
    protected List<DomainCheckEntity> doPageRead() throws Exception {
        this.setArguments(List.of(Date.from(LocalDateTime.now().minusDays(1).minusMonths(threshold).toInstant(ZoneOffset.UTC))));
        return super.doPageRead();
    }
}
