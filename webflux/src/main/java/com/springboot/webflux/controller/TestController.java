package com.springboot.webflux.controller;

import com.springboot.webflux.dto.TestRequest;
import com.springboot.webflux.service.TestService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequiredArgsConstructor
public class TestController {

    private final TestService testService;

    @GetMapping("/")
    public Mono<String> print() {
        return Mono.just("Hello Webflux!!");
    }

    @PostMapping("/fromflask")
    public Mono<String> flask(
            @RequestBody TestRequest testRequest
    ) {
        return testService.fromFlask(testRequest);
    }

    @GetMapping("/servertoken")
    public Mono<String> getToken() {
        return testService.getToken();
    }
}
