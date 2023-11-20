package com.springboot.webflux.dto;

import com.springboot.webflux.repository.entity.Post;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class PostResponse {

    private Long postId;
    private Long memberId;
    private String contents;
    private String predictResult;
    private Float predictPercent;
    private LocalDateTime wroteAt;
    private LocalDateTime editedAt;

    public static PostResponse fromEntity(Post post){

        return PostResponse.builder()
                .postId(post.getPostId())
                .memberId(post.getMemberId())
                .contents(post.getContents())
                .predictResult(post.getPredictResult())
                .predictPercent(post.getPredictPercent())
                .wroteAt(post.getWroteAt())
                .editedAt(post.getEditedAt())
                .build();
    }

}
