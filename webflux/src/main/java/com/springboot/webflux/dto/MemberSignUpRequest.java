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
public class MemberSignUpRequest {

    private String username;
    private String password;

    public Member toEntity(){
        return Member.builder()
                .username(this.username)
                .password(this.password)
                .activityCount(0L)
                .registeredAt(LocalDateTime.now())
                .build();
    }
}
