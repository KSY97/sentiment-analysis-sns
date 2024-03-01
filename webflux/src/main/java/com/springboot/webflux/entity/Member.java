package com.springboot.webflux.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;

@Table("member")
@Builder
@Getter
@Setter
public class Member {

    @Id
    @Column("member_id")
    private Long memberId;

    private String username;

    @JsonIgnore
    private String password;

    @Column("activity_count")
    private Long activityCount;

    @Column("positive_rate")
    private Float positiveRate;

    @CreatedDate
    @Column("registered_at")
    private LocalDateTime registeredAt;

}
