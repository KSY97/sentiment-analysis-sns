package com.springboot.webflux.service;

import com.springboot.webflux.dto.PostRequest;
import com.springboot.webflux.dto.PostResponse;
import com.springboot.webflux.repository.MemberRepository;
import com.springboot.webflux.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class PostService {

    private final PostRepository postRepository;
    private final MemberRepository memberRepository;
    private final PredictionService.PostPredict postPredictionService;

    public Mono<PostResponse> write(PostRequest.Write postRequest) {

        return memberRepository.findById(postRequest.getMemberId())
                .switchIfEmpty(Mono.error(new RuntimeException("존재하지 않는 사용자 입니다.")))
                .flatMap(member -> postRepository.save(postRequest.toEntity()))
                .flatMap(savedPost -> postPredictionService.callApiAndSave(savedPost))
                .map(PostResponse::fromEntity);

    }

    public Mono<PostResponse> edit(PostRequest.Edit postRequest){

        return postRepository.findById(postRequest.getPostId())
                .switchIfEmpty(Mono.error(new RuntimeException("존재하지 않는 게시물 입니다.")))
                .flatMap(post -> {
                    post.updatePost(postRequest.getContents());
                    return postRepository.save(post);
                })
                .flatMap(savedPost -> postPredictionService.callApiAndSave(savedPost))
                .map(PostResponse::fromEntity);
    }

    public Mono<Void> delete(Long postId){

        return postRepository.findById(postId)
                .switchIfEmpty(Mono.error(new RuntimeException("존재하지 않는 게시물 입니다.")))
                .flatMap(postRepository::delete);
    }

    public Mono<PostResponse> findById(Long postId) {

        return postRepository.findById(postId)
                .switchIfEmpty(Mono.error(new RuntimeException("존재하지 않는 게시글 입니다.")))
                .map(PostResponse::fromEntity);
    }

    public Flux<PostResponse> findByMemberId(Long memberId){

        return memberRepository.findById(memberId)
                .switchIfEmpty(Mono.error(new RuntimeException("존재하지 않는 사용자 입니다.")))
                .flatMapMany(member ->
                        postRepository.findByMemberId(member.getMemberId())
                )
                .map(PostResponse::fromEntity);
    }
}