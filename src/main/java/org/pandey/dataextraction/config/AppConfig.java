package org.pandey.dataextraction.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;
import org.springframework.http.HttpHeaders;


/**
 * Configuration class for setting up application-wide beans and configurations.
 * Provides bean definitions for initializing a RestClient with custom settings,
 * including setting up API token authentication using an Authorization header.
 */
@Configuration
public class AppConfig {

    @Value("${api.token}")
    private String apiToken;

    @Value("${api.baseUrl:'https://www.alphavantage.co/query'}")
    private String baseUrl;

    /**
     * Creates and configures a RestClient instance for making HTTP requests.
     * Sets up a base URL and includes default headers, such as Authorization with the API token.
     *
     * @return Configured RestClient instance.
     */
    @Bean
    public RestClient restClient(){
        return RestClient
                .builder()
                .baseUrl(baseUrl)
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + apiToken)
                .build();
    }
}
