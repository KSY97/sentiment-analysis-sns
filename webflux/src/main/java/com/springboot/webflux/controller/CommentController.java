package com.springboot.webflux.controller;

import com.springboot.webflux.dto.CommentEditRequest;
import com.springboot.webflux.dto.CommentRegisterRequest;
import com.springboot.webflux.dto.CommentResponse;
import com.springboot.webflux.service.CommentService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.security.Principal;

@RestController
@RequiredArgsConstructor
@RequestMapping("/comment")
public class CommentController {

    private final CommentService commentService;

    @PostMapping
    public Mono<CommentResponse> write(
            @RequestBody CommentRegisterRequest request,
            Mono<Principal> principalMono
    ){
        return principalMono
                .flatMap(principal -> commentService.write(request, principal.getName()))
                .map(CommentResponse::fromEntity);
    }

    @PatchMapping
    public Mono<CommentResponse> edit(
            @RequestBody CommentEditRequest request,
            Mono<Principal> principalMono
    ){
        return principalMono
                .flatMap(principal -> commentService.edit(request, principal.getName()))
                .map(CommentResponse::fromEntity);
    }

    @DeleteMapping("/{commentId}")
    public Mono<Void> delete(
            @PathVariable Long commentId,
            Mono<Principal> principalMono
    ){
        return principalMono
                .flatMap(principal -> commentService.delete(commentId, principal.getName()));
    }

    @GetMapping("/{commentId}")
    public Mono<CommentResponse> view(
            @PathVariable Long commentId
    ){
        return commentService.findById(commentId)
                .map(CommentResponse::fromEntity);
    }

    @GetMapping("/search/member/{memberId}")
    public Flux<CommentResponse> findByMemberId(
            @PathVariable Long memberId
    ){
        return commentService.findByMemberId(memberId)
                .map(CommentResponse::fromEntity);
    }

    @GetMapping("/search/posts/{postId}")
    public Flux<CommentResponse> findByPostId(
            @PathVariable Long postId
    ){
        return commentService.findByPostId(postId)
                .map(CommentResponse::fromEntity);
    }

}
