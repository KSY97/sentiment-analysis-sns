package com.springboot.webflux.dto;

import com.springboot.webflux.repository.entity.Member;
import lombok.Data;

import java.time.LocalDateTime;

public class MemberRequest {

    @Data
    public static class SignUp{

        private String username;
        private String password;

        public Member toEntity(){
            return Member.builder()
                    .username(this.username)
                    .password(this.password)
                    .registeredAt(LocalDateTime.now())
                    .build();
        }

    }

    @Data
    public static class SignIn{

        private String username;
        private String passWord;
    }

    @Data
    public static class Edit{

        private Long memberId;
        private String username;
        private String password;
    }
}
