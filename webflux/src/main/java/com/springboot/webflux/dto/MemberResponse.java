package com.springboot.webflux.dto;

import com.springboot.webflux.entity.Member;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MemberResponse {

    private Long memberId;
    private String username;
    private Long activityCount;
    private Float positiveRate;
    private LocalDateTime registeredAt;

    public static MemberResponse fromEntity(Member member){

        return MemberResponse.builder()
                .memberId(member.getMemberId())
                .username(member.getUsername())
                .activityCount(member.getActivityCount())
                .positiveRate(member.getPositiveRate())
                .registeredAt(member.getRegisteredAt())
                .build();
    }

}
