package com.springboot.webflux.service;

import com.springboot.webflux.dto.CommentEditRequest;
import com.springboot.webflux.dto.CommentRegisterRequest;
import com.springboot.webflux.entity.Comment;
import com.springboot.webflux.repository.CommentRepository;
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
public class CommentService {

    private final CommentRepository commentRepository;
    private final PostRepository postRepository;
    private final MemberRepository memberRepository;
    private final CommentPredictionService commentPredictionService;

    public Mono<Comment> write(CommentRegisterRequest commentRequest, String username){

        return memberRepository.findById(commentRequest.getMemberId())
                .filter(member -> member.getUsername().equals(username))
                .switchIfEmpty(Mono.error(new RuntimeException(INVALID_REQUEST.getMessage())))
                .flatMap(member -> postRepository.findById(commentRequest.getPostId()))
                .switchIfEmpty(Mono.error(new RuntimeException(POST_NOT_FOUND.getMessage())))
                .flatMap(post -> commentRepository.save(commentRequest.toEntity()))
                .flatMap(commentPredictionService::callSentimentAnalysisApiAndSaveResultForRegister);
    }

    public Mono<Comment> edit(CommentEditRequest commentRequest, String username){

        return commentRepository.findById(commentRequest.getCommentId())
                .zipWhen(comment -> memberRepository.findById(comment.getMemberId()))
                .filter(tuple -> tuple.getT2().getUsername().equals(username))
                .switchIfEmpty(Mono.error(new RuntimeException(INVALID_REQUEST.getMessage())))
                .flatMap(tuple -> {
                    Comment comment = tuple.getT1();

                    comment.setContents(commentRequest.getContents());
                    comment.setEditedAt(LocalDateTime.now());
                    return commentRepository.save(comment);
                })
                .flatMap(commentPredictionService::callSentimentAnalysisApiAndSaveResultForEdit);
    }

    public Mono<Void> delete(Long commentId, String username){

        return commentRepository.findById(commentId)
                .zipWhen(comment -> memberRepository.findById(comment.getMemberId()))
                .filter(tuple -> tuple.getT2().getUsername().equals(username))
                .switchIfEmpty(Mono.error(new RuntimeException(INVALID_REQUEST.getMessage())))
                .flatMap(tuple -> commentRepository.delete(tuple.getT1()));
    }

    public Mono<Comment> findById(Long commentId) {

        return commentRepository.findById(commentId)
                .switchIfEmpty(Mono.error(new RuntimeException(COMMENT_NOT_FOUND.getMessage())));
    }

    public Flux<Comment> findByMemberId(Long memberId) {
         
        return memberRepository.findById(memberId)
                .switchIfEmpty(Mono.error(new RuntimeException(MEMBER_NOT_FOUND.getMessage())))
                .flatMapMany(member -> commentRepository.findByMemberId(member.getMemberId()));
    }

    public Flux<Comment> findByPostId(Long postId) {

        return postRepository.findById(postId)
                .switchIfEmpty(Mono.error(new RuntimeException(POST_NOT_FOUND.getMessage())))
                .flatMapMany(post -> commentRepository.findByPostId(post.getPostId()));
    }
}
