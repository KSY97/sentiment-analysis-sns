package com.springboot.webflux.entity;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;

@Table("post")
@Getter
@Setter
@Builder
public class Post {

    @Id
    @Column("post_id")
    private Long postId;

    @Column("member_id")
    private Long memberId;

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
}
