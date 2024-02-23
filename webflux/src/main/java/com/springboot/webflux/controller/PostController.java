package com.springboot.webflux.controller;

import com.springboot.webflux.dto.PostEditRequest;
import com.springboot.webflux.dto.PostRegisterRequest;
import com.springboot.webflux.dto.PostResponse;
import com.springboot.webflux.service.PostService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.security.Principal;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/posts")
public class PostController {

    private final PostService postService;

    @PostMapping
    public Mono<PostResponse> write(
            @RequestBody PostRegisterRequest request,
            Mono<Principal> principalMono
    ){
        return principalMono
                .flatMap(principal -> postService.write(request, principal.getName()))
                .map(PostResponse::fromEntity);
    }

    @PatchMapping
    public Mono<PostResponse> edit(
            @RequestBody PostEditRequest request,
            Mono<Principal> principalMono
    ){
        return principalMono
                .flatMap(principal -> postService.edit(request, principal.getName()))
                .map(PostResponse::fromEntity);
    }

    @DeleteMapping("/{postId}")
    public Mono<Void> delete(
            @PathVariable Long postId,
            Mono<Principal> principalMono
    ){
        return principalMono
                .flatMap(principal -> postService.delete(postId, principal.getName()));
    }

    @GetMapping("/{postId}")
    public Mono<PostResponse> view(
            @PathVariable Long postId
    ){
        return postService.findById(postId)
                .map(PostResponse::fromEntity);
    }

    @GetMapping("/search/member/{memberId}")
    public Flux<PostResponse> findByMemberId(
            @PathVariable Long memberId
    ){
        return postService.findByMemberId(memberId)
                .map(PostResponse::fromEntity);
    }
}
