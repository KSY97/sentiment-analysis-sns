package com.springboot.webflux.service;

import com.springboot.webflux.dto.MemberEditRequest;
import com.springboot.webflux.dto.MemberSignInRequest;
import com.springboot.webflux.dto.MemberSignUpRequest;
import com.springboot.webflux.entity.Member;
import com.springboot.webflux.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import static com.springboot.webflux.constants.ExceptionStatus.*;

@Service
@RequiredArgsConstructor
public class MemberService {

    private final MemberRepository memberRepository;
    private final JwtService jwtService;

    public Mono<Member> findById(Long memberId){
        return memberRepository.findById(memberId)
                .switchIfEmpty(Mono.error(new RuntimeException(MEMBER_NOT_FOUND.getMessage())));
    }

    public Mono<Member> signUp(MemberSignUpRequest memberRequest){

        return memberRepository.existsByUsername(memberRequest.getUsername())
                .filter(exists -> !exists)
                .switchIfEmpty(Mono.error(new RuntimeException(MEMBER_ALREADY_EXISTS.getMessage())))
                .flatMap(exists -> memberRepository.save(memberRequest.toEntity()));
    }

    public Mono<String> signIn(MemberSignInRequest memberRequest){

        return memberRepository.findByUsername(memberRequest.getUsername())
                .switchIfEmpty(Mono.error(new RuntimeException(MEMBER_NOT_FOUND.getMessage())))
                .map(member -> jwtService.generateAccessToken(member.getMemberId()));
    }

    public Mono<Member> edit(MemberEditRequest memberRequest, String username){

        return memberRepository.findById(memberRequest.getMemberId())
                .filter(member -> member.getUsername().equals(username))
                .switchIfEmpty(Mono.error(new RuntimeException(INVALID_REQUEST.getMessage())))
                .flatMap(member -> {
                    member.setUsername(memberRequest.getUsername());
                    member.setPassword(memberRequest.getPassword());
                    return memberRepository.save(member);
                });
    }

}