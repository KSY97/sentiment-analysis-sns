package com.springboot.webflux.controller;

import com.springboot.webflux.dto.MemberRequest;
import com.springboot.webflux.dto.MemberResponse;
import com.springboot.webflux.service.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@RequiredArgsConstructor
@RequestMapping("/member")
public class MemberController {

    private final MemberService memberService;

    @GetMapping
    public Mono<MemberResponse> findById(
            @RequestParam("member_id") Long memberId
    ){
        return memberService.findById(memberId);
    }

    @PostMapping("/signup")
    public Mono<MemberResponse> signUp(
            @RequestBody MemberRequest.SignUp request
    ){
        return memberService.signUp(request);
    }

    @PostMapping("/signin")
    public Mono<MemberResponse> signIn(
            @RequestBody MemberRequest.SignIn request
    ){
        return memberService.signIn(request);
    }

    @PutMapping("/edit")
    public Mono<MemberResponse> edit(
            @RequestBody MemberRequest.Edit request
    ){
        return memberService.edit(request);
    }
}
