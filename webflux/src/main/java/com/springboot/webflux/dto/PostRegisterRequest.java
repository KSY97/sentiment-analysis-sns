package com.springboot.webflux.dto;

import com.springboot.webflux.entity.Post;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PostRegisterRequest {
    private Long memberId;
    private String contents;

    public Post toEntity(){
        return Post.builder()
                .memberId(this.memberId)
                .contents(this.contents)
                .wroteAt(LocalDateTime.now())
                .build();
    }
}
