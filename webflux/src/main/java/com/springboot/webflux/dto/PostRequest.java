package com.springboot.webflux.dto;

import com.springboot.webflux.repository.entity.Post;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

public class PostRequest {

    @Data
    @AllArgsConstructor
    public static class Write {

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

    @Data
    @AllArgsConstructor
    public static class Edit {

        private Long postId;
        private String contents;
    }
}
