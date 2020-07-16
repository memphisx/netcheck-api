package com.bompotis.netcheck;

import com.bompotis.netcheck.scheduler.batch.notification.config.PushoverConfig;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.License;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.PropertySource;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;

@OpenAPIDefinition(info =
	@Info(
			title = "${info.app.name}",
			version = "${build.version}",
			description = "${info.app.description}",
			license = @License(name = "GPL 3.0", url = "https://www.gnu.org/licenses/gpl-3.0.en.html"),
			contact = @Contact(url = "https://github.com/memphisx", name = "Kyriakos Bompotis")
	)
)
@SpringBootApplication
@EnableJpaRepositories
@EnableScheduling
@EnableConfigurationProperties(PushoverConfig.class)
@PropertySource(value="classpath:META-INF/build-info.properties", ignoreResourceNotFound=true)
public class NetcheckApplication {

	public static void main(String[] args) {
		SpringApplication.run(NetcheckApplication.class, args);
	}

}
