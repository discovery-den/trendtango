package org.pandey;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.client.RestTemplate;

@SpringBootApplication
@EnableScheduling
@Async
public class ConsumingRestApplication {

    private static final Logger logger = LoggerFactory.getLogger(ConsumingRestApplication.class);

    public static void main(String[] args) {
        logger.info("Event Handler Part-1 App Started");
        SpringApplication.run(ConsumingRestApplication.class, args);
    }
}
