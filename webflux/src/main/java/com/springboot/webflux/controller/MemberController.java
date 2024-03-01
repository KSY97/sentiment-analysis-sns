package com.springboot.webflux.controller;

import com.springboot.webflux.dto.MemberEditRequest;
import com.springboot.webflux.dto.MemberResponse;
import com.springboot.webflux.dto.MemberSignInRequest;
import com.springboot.webflux.dto.MemberSignUpRequest;
import com.springboot.webflux.service.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.security.Principal;

@RestController
@RequiredArgsConstructor
@RequestMapping("/member")
public class MemberController {

    private final MemberService memberService;

    @GetMapping("/{memberId}")
    public Mono<MemberResponse> findById(
            @PathVariable Long memberId
    ){
        return memberService.findById(memberId)
                .map(MemberResponse::fromEntity);
    }

    @PostMapping("/signup")
    public Mono<MemberResponse> signUp(
            @RequestBody MemberSignUpRequest request
    ){
        return memberService.signUp(request)
                .map(MemberResponse::fromEntity);
    }

    @PostMapping("/signin")
    public Mono<String> signIn(
            @RequestBody MemberSignInRequest request
    ){
        return memberService.signIn(request);
    }

    @PatchMapping
    public Mono<MemberResponse> edit(
            @RequestBody MemberEditRequest request,
            Mono<Principal> principalMono
    ){
        return principalMono
                .flatMap(principal -> memberService.edit(request, principal.getName()))
                .map(MemberResponse::fromEntity);
    }
}
