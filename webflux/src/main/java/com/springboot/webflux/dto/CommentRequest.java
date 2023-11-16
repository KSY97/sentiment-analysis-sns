package com.springboot.webflux.dto;

import com.springboot.webflux.repository.entity.Comment;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

public class CommentRequest {

    @Data
    @AllArgsConstructor
    public static class Write {

        private Long memberId;
        private Long postId;
        private String contents;

        public Comment toEntity(){
            return Comment.builder()
                    .memberId(this.memberId)
                    .postId(this.postId)
                    .contents(this.contents)
                    .wroteAt(LocalDateTime.now())
                    .build();
        }
    }

    @Data
    @AllArgsConstructor
    public static class Edit {

        private Long commentId;
        private String contents;
    }
}
