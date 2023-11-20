package com.springboot.webflux.controller;

import com.springboot.webflux.dto.CommentRequest;
import com.springboot.webflux.dto.CommentResponse;
import com.springboot.webflux.service.CommentService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequiredArgsConstructor
@RequestMapping("/comment")
public class CommentController {

    private final CommentService commentService;

    @PostMapping("/write")
    public Mono<CommentResponse> write(
            @RequestBody CommentRequest.Write request
    ){
        return commentService.write(request);
    }

    @PutMapping("/edit")
    public Mono<CommentResponse> edit(
            @RequestBody CommentRequest.Edit request
    ){
        return commentService.edit(request);
    }

    @DeleteMapping("/delete")
    public Mono<Void> delete(
            @RequestParam("comment_id") Long postId
    ){
        return commentService.delete(postId);
    }

    @GetMapping("/view")
    public Mono<CommentResponse> view(
            @RequestParam("comment_id") Long commentId
    ){
        return commentService.findById(commentId);
    }

    @GetMapping("/search/member")
    public Flux<CommentResponse> findByMemberId(
            @RequestParam("member_id") Long memberId
    ){
        return commentService.findByMemberId(memberId);
    }

    @GetMapping("/search/posts")
    public Flux<CommentResponse> findByPostId(
            @RequestParam("post_id") Long postId
    ){
        return commentService.findByPostId(postId);
    }

}
