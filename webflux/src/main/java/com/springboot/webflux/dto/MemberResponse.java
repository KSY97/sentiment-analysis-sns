package com.springboot.webflux.dto;

import com.springboot.webflux.repository.entity.Member;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class MemberResponse {

    private Long memberId;
    private String username;
    private Float positiveRate;
    private LocalDateTime registeredAt;

    public static MemberResponse fromEntity(Member member){

        return MemberResponse.builder()
                .memberId(member.getMemberId())
                .username(member.getUsername())
                .positiveRate(member.getPositiveRate())
                .registeredAt(member.getRegisteredAt())
                .build();
    }

}
