package com.bompotis.netcheck;

import com.bompotis.netcheck.scheduler.batch.notification.config.PushoverConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableJpaRepositories
@EnableScheduling
@EnableConfigurationProperties(PushoverConfig.class)
public class NetcheckApplication {

	public static void main(String[] args) {
		SpringApplication.run(NetcheckApplication.class, args);
	}

}
