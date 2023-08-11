package com.bompotis.netcheck;

import com.bompotis.netcheck.scheduler.batch.notification.config.PushoverConfig;
import com.bompotis.netcheck.scheduler.batch.notification.config.WebhookConfig;
import io.zonky.test.db.AutoConfigureEmbeddedDatabase;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@AutoConfigureEmbeddedDatabase
class NetcheckApplicationStartupTests {

	@Autowired
	private WebhookConfig webhookConfig;

	@Autowired
	private PushoverConfig pushoverConfig;

	@Test
	void contextLoads() {
		assertThat(webhookConfig.getEnabled()).isTrue();
		assertThat(pushoverConfig.getEnabled()).isFalse();
	}

}
