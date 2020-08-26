package net.scrape.containers;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@EnableConfigurationProperties
@SpringBootApplication
public class ContainerStatusScrapper {

	public static void main(String[] args) {
		SpringApplication.run(ContainerStatusScrapper.class, args);
	}

}
