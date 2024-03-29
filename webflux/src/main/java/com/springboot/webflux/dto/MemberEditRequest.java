package com.springboot.webflux.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MemberEditRequest {

    private Long memberId;
    private String username;
    private String password;
}
