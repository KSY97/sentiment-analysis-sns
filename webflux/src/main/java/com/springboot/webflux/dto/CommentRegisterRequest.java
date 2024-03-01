package com.springboot.webflux.dto;

import com.springboot.webflux.entity.Comment;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CommentRegisterRequest {
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
