package com.springboot.webflux.service;

import com.springboot.webflux.dto.TestRequest;
import com.springboot.webflux.security.AuthToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Service
public class TestService {

    @Autowired
    private WebClient webClient;

    public Mono<String> fromFlask(TestRequest testRequest){
        return webClient.post()
                .uri("/api/tospring")
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, AuthToken.token)
                .bodyValue(testRequest)
                .retrieve()
                .bodyToMono(String.class);
    }

    public Mono<String> getToken(){
        return webClient.get()
                .uri("/auth/token")
                .retrieve()
                .bodyToMono(String.class)
                .doOnNext(token -> AuthToken.token = token);
    }
}
