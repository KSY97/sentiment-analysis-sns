package com.springboot.webflux.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {

    @Value("${server.base.address}")
    String baseAddress;

    @Bean
    public WebClient webClient() {
        return WebClient.builder().baseUrl(baseAddress).build();
    }

}
