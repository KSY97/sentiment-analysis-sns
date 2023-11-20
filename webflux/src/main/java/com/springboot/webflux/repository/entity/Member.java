package com.springboot.webflux.repository.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Builder;
import lombok.Getter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;

@Table("member")
@Builder
@Getter
public class Member {

    @Id
    @Column("member_id")
    private Long memberId;

    private String username;

    @JsonIgnore
    private String password;

    @Column("positive_rate")
    private Float positiveRate;

    @CreatedDate
    @Column("registered_at")
    private LocalDateTime registeredAt;

    public void updateInfo(String username, String password){
        this.username = username;
        this.password = password;
    }

    public void updatePositiveRate(Float rate){
        this.positiveRate = rate;
    }

}
