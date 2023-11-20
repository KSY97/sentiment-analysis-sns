package com.springboot.webflux.controller;

import com.springboot.webflux.dto.PostRequest;
import com.springboot.webflux.dto.PostResponse;
import com.springboot.webflux.security.CustomUserPrincipal;
import com.springboot.webflux.service.PostService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequiredArgsConstructor
@RequestMapping("/posts")
public class PostController {

    private final PostService postService;

    @PostMapping("/write")
    public Mono<PostResponse> write(
            @RequestBody PostRequest.Write request,
            @AuthenticationPrincipal CustomUserPrincipal principal
    ){
        return postService.write(request, principal.getMember().getMemberId());
    }

    @PutMapping("/edit")
    public Mono<PostResponse> edit(
            @RequestBody PostRequest.Edit request,
            @AuthenticationPrincipal CustomUserPrincipal principal
    ){
        return postService.edit(request, principal.getMember().getMemberId());
    }

    @DeleteMapping("/delete")
    public Mono<Void> delete(
            @RequestParam("post_id") Long postId,
            @AuthenticationPrincipal CustomUserPrincipal principal
    ){
        return postService.delete(postId, principal.getMember().getMemberId());
    }

    @GetMapping("/view")
    public Mono<PostResponse> view(
            @RequestParam("post_id") Long postId
    ){
        return postService.findById(postId);
    }

    @GetMapping("/search")
    public Flux<PostResponse> findByMemberId(
            @RequestParam("member_id") Long memberId
    ){
        return postService.findByMemberId(memberId);
    }
}
