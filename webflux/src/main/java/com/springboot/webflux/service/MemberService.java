package com.springboot.webflux.service;

import com.springboot.webflux.dto.MemberRequest;
import com.springboot.webflux.dto.MemberResponse;
import com.springboot.webflux.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class MemberService {

    private final MemberRepository memberRepository;
    private final JwtService jwtService;

    public Mono<MemberResponse> findById(Long memberId){
        return memberRepository.findById(memberId)
                .map(MemberResponse::fromEntity);
    }

    public Mono<MemberResponse> signUp(MemberRequest.SignUp memberRequest){

        return memberRepository.existsByUsername(memberRequest.getUsername())
                .flatMap(exists -> {
                    if (exists) {
                        return Mono.error(new RuntimeException("이미 존재하는 사용자명 입니다"));
                    } else {
                        return memberRepository.save(memberRequest.toEntity())
                                .map(MemberResponse::fromEntity);
                    }
                });
    }

    public Mono<String> signIn(MemberRequest.SignIn memberRequest){

        return memberRepository.findByUsername(memberRequest.getUsername())
                .switchIfEmpty(Mono.error(new RuntimeException("존재하지 않는 사용자명 입니다.")))
                .flatMap(member -> Mono.just(jwtService.generateAccessToken(member.getMemberId())));
    }

    public Mono<MemberResponse> edit(MemberRequest.Edit memberRequest, Long memberId){

        return memberRepository.findById(memberRequest.getMemberId())
                .switchIfEmpty(Mono.error(new RuntimeException("존재하지 않는 사용자 입니다.")))
                .flatMap(member -> {
                    if(!(memberId == null)){
                        if(!(member.getMemberId() == memberId)){
                            return Mono.error(new RuntimeException("권한이 없습니다."));
                        }
                    }
                    return Mono.just(member);
                })
                .flatMap(member -> {
                    member.updateInfo(memberRequest.getUsername(), memberRequest.getPassword());
                    return memberRepository.save(member);
                })
                .map(MemberResponse::fromEntity);
    }

}