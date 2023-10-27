package com.springboot.webflux.controller;

import com.springboot.webflux.dto.TestRequest;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@RestController
public class TestController {

    private final WebClient webClient;

    public TestController(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.baseUrl("http://127.0.0.1:5000").build();
    }

    @GetMapping("/test")
    public Mono<String> print() {
        return Mono.just("Hello Webflux!!");
    }

    @PostMapping("/fromflask")
    public Mono<String> flask(
            @RequestBody TestRequest testRequest
            ) {

        String contents = testRequest.getContents();

        System.out.println(contents);

        return webClient.post()
                .uri("/api/tospring")
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(testRequest)
                .retrieve()
                .bodyToMono(String.class);
    }
}
