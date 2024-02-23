package com.springboot.webflux.constants;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ExceptionStatus {

    INVALID_REQUEST("유효하지 않은 요청 입니다."),
    MEMBER_ALREADY_EXISTS("이미 존재하는 사용자명 입니다."),
    MEMBER_NOT_FOUND("멤버를 찾지 못했습니다."),
    POST_NOT_FOUND("게시글을 찾지 못했습니다."),
    COMMENT_NOT_FOUND("댓글을 찾지 못했습니다."),
    JSON_PARSE_FAILED("JSON 파싱 실패");

    private final String message;
}
