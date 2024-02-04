package com.springboot.webflux.service;

import com.springboot.webflux.dto.CommentRequest;
import com.springboot.webflux.dto.CommentResponse;
import com.springboot.webflux.repository.CommentRepository;
import com.springboot.webflux.repository.MemberRepository;
import com.springboot.webflux.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class CommentService {

    private final CommentRepository commentRepository;
    private final PostRepository postRepository;
    private final MemberRepository memberRepository;
    private final PredictionService.CommentPredict commentPredictionService;

    public Mono<CommentResponse> write(CommentRequest.Write commentRequest, Long memberId){

        return memberRepository.findById(commentRequest.getMemberId())
                .switchIfEmpty(Mono.error(new RuntimeException("존재하지 않는 사용자 입니다.")))
                .flatMap(member -> {
                    if(!(memberId == null)){
                        if(!(member.getMemberId() == memberId)){
                            return Mono.error(new RuntimeException("권한이 없습니다."));
                        }
                    }
                    return Mono.just(member);
                })
                .flatMap(member ->
                        postRepository.findById(commentRequest.getPostId())
                                .switchIfEmpty(Mono.error(new RuntimeException("존재하지 않는 게시글 입니다.")))
                )
                .flatMap(post -> commentRepository.save(commentRequest.toEntity()))
                .flatMap(savedComment -> commentPredictionService.callApiAndSave(savedComment))
                .map(CommentResponse::fromEntity);
    }

    public Mono<CommentResponse> edit(CommentRequest.Edit commentRequest, Long memberId){

        return commentRepository.findById(commentRequest.getCommentId())
                .switchIfEmpty(Mono.error(new RuntimeException("존재하지 않는 댓글 입니다.")))
                .flatMap(comment -> {
                    if(!(memberId == null)){
                        if(!(comment.getMemberId() == memberId)){
                            return Mono.error(new RuntimeException("권한이 없습니다."));
                        }
                    }
                    return Mono.just(comment);
                })
                .flatMap(comment -> {
                    comment.updateComment(commentRequest.getContents());
                    return commentRepository.save(comment);
                })
                .flatMap(savedComment -> commentPredictionService.callApiAndSave(savedComment))
                .map(CommentResponse::fromEntity);
    }

    public Mono<Void> delete(Long commentId, Long memberId){

        return commentRepository.findById(commentId)
                .switchIfEmpty(Mono.error(new RuntimeException("존재하지 않는 댓글 입니다.")))
                .flatMap(comment -> {
                    if(!(memberId == null)){
                        if(!(comment.getMemberId() == memberId)){
                            return Mono.error(new RuntimeException("권한이 없습니다."));
                        }
                    }
                    return Mono.just(comment);
                })
                .flatMap(commentRepository::delete);
    }

    public Mono<CommentResponse> findById(Long commentId) {

        return commentRepository.findById(commentId)
                .switchIfEmpty(Mono.error(new RuntimeException("존재하지 않는 댓글 입니다.")))
                .map(CommentResponse::fromEntity);
    }

    public Flux<CommentResponse> findByMemberId(Long memberId) {

        return memberRepository.findById(memberId)
                .switchIfEmpty(Mono.error(new RuntimeException("존재하지 않는 사용자 입니다.")))
                .flatMapMany(member ->
                        commentRepository.findByMemberId(member.getMemberId())
                )
                .map(CommentResponse::fromEntity);
    }

    public Flux<CommentResponse> findByPostId(Long postId) {

        return postRepository.findById(postId)
                .switchIfEmpty(Mono.error(new RuntimeException("존재하지 않는 게시글 입니다.")))
                .flatMapMany(post ->
                        commentRepository.findByPostId(post.getPostId())
                )
                .map(CommentResponse::fromEntity);
    }
}
