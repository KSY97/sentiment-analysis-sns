package com.springboot.webflux.security;

import com.springboot.webflux.repository.MemberRepository;
import com.springboot.webflux.service.JwtService;
import lombok.AllArgsConstructor;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;


import reactor.core.publisher.Mono;

import java.util.ArrayList;

@Service
@AllArgsConstructor
public class AuthenticationManager implements ReactiveAuthenticationManager {

    private JwtService jwtService;
    private MemberRepository memberRepository;

    @Override
    public Mono<Authentication> authenticate(Authentication authentication) {
        String authToken = authentication.getCredentials().toString();
        Long uid = jwtService.getUidFromToken(authToken);
        return Mono.just(jwtService.validateToken(authToken))
                .filter(valid -> valid)
                .switchIfEmpty(Mono.empty())
                .flatMap(valid -> memberRepository.findById(uid))
                .map(user -> new UsernamePasswordAuthenticationToken(
                        new CustomUserPrincipal(user),
                        user.getPassword(),
                        new ArrayList<>())
                );
    }
}