package com.springboot.webflux.dto;

import com.springboot.webflux.repository.entity.Comment;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class CommentResponse {

    private Long commentId;
    private Long postId;
    private Long memberId;
    private String contents;
    private String predictResult;
    private Float predictPercent;
    private LocalDateTime wroteAt;
    private LocalDateTime editedAt;

    public static CommentResponse fromEntity(Comment comment){
        return CommentResponse.builder()
                .commentId(comment.getCommentId())
                .postId(comment.getPostId())
                .memberId(comment.getMemberId())
                .contents(comment.getContents())
                .predictResult(comment.getPredictResult())
                .predictPercent(comment.getPredictPercent())
                .wroteAt(comment.getWroteAt())
                .editedAt(comment.getEditedAt())
                .build();
    }
}
