package com.springboot.webflux.service;

import com.springboot.webflux.dto.PostEditRequest;
import com.springboot.webflux.dto.PostRegisterRequest;
import com.springboot.webflux.entity.Post;
import com.springboot.webflux.repository.MemberRepository;
import com.springboot.webflux.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

import static com.springboot.webflux.constants.ExceptionStatus.*;

@Service
@RequiredArgsConstructor
public class PostService {

    private final PostRepository postRepository;
    private final MemberRepository memberRepository;
    private final PostPredictionService postPredictionService;

    public Mono<Post> write(PostRegisterRequest postRequest, String username) {

        return memberRepository.findById(postRequest.getMemberId())
                .filter(member -> member.getUsername().equals(username))
                .switchIfEmpty(Mono.error(new RuntimeException(INVALID_REQUEST.getMessage())))
                .flatMap(member -> postRepository.save(postRequest.toEntity()))
                .flatMap(postPredictionService::callSentimentAnalysisApiAndSaveResultForRegister);
    }

    public Mono<Post> edit(PostEditRequest postRequest, String username){

        return postRepository.findById(postRequest.getPostId())
                .zipWhen(post -> memberRepository.findById(post.getMemberId()))
                .filter(tuple -> tuple.getT2().getUsername().equals(username))
                .switchIfEmpty(Mono.error(new RuntimeException(INVALID_REQUEST.getMessage())))
                .flatMap(tuple -> {
                    Post post = tuple.getT1();

                    post.setContents(postRequest.getContents());
                    post.setEditedAt(LocalDateTime.now());
                    return postRepository.save(post);
                })
                .flatMap(postPredictionService::callSentimentAnalysisApiAndSaveResultForEdit);
    }

    public Mono<Void> delete(Long postId, String username){

        return postRepository.findById(postId)
                .zipWhen(post -> memberRepository.findById(post.getMemberId()))
                .filter(tuple -> tuple.getT2().getUsername().equals(username))
                .switchIfEmpty(Mono.error(new RuntimeException(INVALID_REQUEST.getMessage())))
                .flatMap(tuple -> postRepository.delete(tuple.getT1()));
    }

    public Mono<Post> findById(Long postId) {

        return postRepository.findById(postId)
                .switchIfEmpty(Mono.error(new RuntimeException(POST_NOT_FOUND.getMessage())));
    }

    public Flux<Post> findByMemberId(Long memberId){

        return memberRepository.findById(memberId)
                .flatMapMany(member -> postRepository.findByMemberId(member.getMemberId()))
                .switchIfEmpty(Flux.error(new RuntimeException(MEMBER_NOT_FOUND.getMessage())));
    }
}
