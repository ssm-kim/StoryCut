package com.stroycut.global.model.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum BaseResponseStatus {
    SUCCESS(true, HttpStatus.OK, 200, "요청에 성공하였습니다."),
    AUTHORIZATION_SUCCESS(true, HttpStatus.OK, 200, "토큰 발급에 성공하였습니다."),
    BAD_REQUEST(false, HttpStatus.BAD_REQUEST, 400, "입력값을 확인해주세요."),
    UNAUTHORIZED(false, HttpStatus.UNAUTHORIZED, 401, "인증이 필요합니다."),
    FORBIDDEN(false, HttpStatus.FORBIDDEN, 403, "권한이 없습니다."),
    NOT_FOUND(false, HttpStatus.NOT_FOUND, 404, "대상을 찾을 수 없습니다."),

    // 2000~ 2999 : 방 관련 에러
    NOT_FOUND_ROOM(false, HttpStatus.NOT_FOUND, 2000, "해당 방이 존재하지 않습니다."),
    NOT_VALID_HOST(false, HttpStatus.FORBIDDEN, 2001, "방의 호스트가 아니거나 이미 없는 방입니다."),
    ALREADY_MEMBER_ROOM(false, HttpStatus.BAD_REQUEST, 2002, "이미 방에 참여 중입니다."),
    NOT_VALID_PASSWORD(false, HttpStatus.BAD_REQUEST, 2003, "비밀번호가 일치하지 않습니다."),

    ;

    private final boolean isSuccess;
    @JsonIgnore
    private final HttpStatus httpStatus;
    private final int code;
    private final String message;

    BaseResponseStatus(boolean isSuccess, HttpStatus httpStatus, int code, String message) {
        this.isSuccess = isSuccess;
        this.httpStatus = httpStatus;
        this.code = code;
        this.message = message;
    }
}