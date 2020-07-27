/*
 * Copyright 2020 the original author or authors.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This code is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
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
