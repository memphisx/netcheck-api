package com.bompotis.netcheck;

import com.bompotis.netcheck.scheduler.batch.notification.config.PushoverConfig;
import com.bompotis.netcheck.scheduler.batch.notification.config.WebhookConfig;
import org.junit.Assert;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class NetcheckApplicationTests {

	@Autowired
	private WebhookConfig webhookConfig;

	@Autowired
	private PushoverConfig pushoverConfig;

	@Test
	void contextLoads() {
		Assert.assertFalse(webhookConfig.getEnabled());
		Assert.assertFalse(pushoverConfig.getEnabled());
	}

}
