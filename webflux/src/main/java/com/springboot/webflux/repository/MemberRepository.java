package com.springboot.webflux.repository;

import com.springboot.webflux.repository.entity.Member;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Mono;

public interface MemberRepository extends ReactiveCrudRepository<Member, Long> {
    Mono<Boolean> existsByUsername(String username);

    Mono<Member> findByUsername(String username);
}
