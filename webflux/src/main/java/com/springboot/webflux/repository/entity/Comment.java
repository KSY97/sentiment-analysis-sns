package com.springboot.webflux.repository.entity;

import lombok.Builder;
import lombok.Getter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;

@Table("comment")
@Builder
@Getter
public class Comment {

    @Id
    @Column("comment_id")
    private Long commentId;

    @Column("member_id")
    private Long memberId;

    @Column("post_id")
    private Long postId;

    private String contents;

    @Column("predict_result")
    private String predictResult;

    @Column("predict_percent")
    private Float predictPercent;

    @CreatedDate
    @Column("wrote_at")
    private LocalDateTime wroteAt;

    @LastModifiedDate
    @Column("edited_at")
    private LocalDateTime editedAt;

    public void updateComment(String contents){
        this.contents = contents;
        this.editedAt = LocalDateTime.now();
    }

    public void updatePrediction(String result, Float percent){
        this.predictResult = result;
        this.predictPercent = percent;
    }
}
