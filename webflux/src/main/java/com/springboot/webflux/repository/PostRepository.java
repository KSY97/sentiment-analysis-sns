package com.springboot.webflux.repository;

import com.springboot.webflux.repository.entity.Post;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;


public interface PostRepository extends ReactiveCrudRepository<Post, Long> {

    Flux<Post> findByMemberId(Long memberId);

    Mono<Long> countByMemberId(Long memberId);
}
