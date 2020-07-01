package com.bompotis.netcheck.scheduler.batch.writer;

import com.bompotis.netcheck.data.entity.DomainCheckEntity;
import org.springframework.batch.item.ItemWriter;

import java.util.List;

/**
 * Created by Kyriakos Bompotis on 1/7/20.
 */
public abstract class NotificationItemWriter implements ItemWriter<DomainCheckEntity> {

    @Override
    public void write(List<? extends DomainCheckEntity> list) throws Exception {

    }
}
