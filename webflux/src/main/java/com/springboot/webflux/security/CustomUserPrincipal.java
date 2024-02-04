package com.springboot.webflux.security;

import java.io.Serializable;
import java.util.ArrayList;

import com.springboot.webflux.repository.entity.Member;
import org.springframework.security.core.userdetails.User;

import lombok.Getter;

@Getter
public class CustomUserPrincipal extends User implements Serializable {

    private final Member member;

    public CustomUserPrincipal(Member member) {
        super(member.getMemberId().toString(), member.getPassword().toString(), new ArrayList<>());
        this.member = member;
    }
}