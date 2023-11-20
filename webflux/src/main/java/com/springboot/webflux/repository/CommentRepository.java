package com.springboot.webflux.repository;

import com.springboot.webflux.repository.entity.Comment;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;

public interface CommentRepository extends ReactiveCrudRepository<Comment, Long> {

    Flux<Comment> findByMemberId(Long memberId);

    Flux<Comment> findByPostId(Long memberId);

}
