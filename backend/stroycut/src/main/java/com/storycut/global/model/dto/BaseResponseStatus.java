package com.storycut.global.model.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum BaseResponseStatus {
    // 성공 코드 (2xx)
    SUCCESS(true, HttpStatus.OK, 200, "요청에 성공하였습니다."),
    AUTHORIZATION_SUCCESS(true, HttpStatus.OK, 200, "토큰 발급에 성공하였습니다."),

    // 클라이언트 오류 코드 (4xx)
    BAD_REQUEST(false, HttpStatus.BAD_REQUEST, 400, "입력값을 확인해주세요."),
    UNAUTHORIZED(false, HttpStatus.UNAUTHORIZED, 401, "인증이 필요합니다."),
    FORBIDDEN(false, HttpStatus.FORBIDDEN, 403, "권한이 없습니다."),
    NOT_FOUND(false, HttpStatus.NOT_FOUND, 404, "대상을 찾을 수 없습니다."),

    // 서버 오류 코드 (5xx)
    INTERNAL_SERVER_ERROR(false, HttpStatus.INTERNAL_SERVER_ERROR, 500, "서버 내부 오류가 발생했습니다."),

    // 소셜 로그인 관련 오류 코드 (1000~1999)
    GOOGLE_LOGIN_ERROR(false, HttpStatus.INTERNAL_SERVER_ERROR, 1001, "구글 로그인 처리 중 오류가 발생했습니다."),
    INVALID_ID_TOKEN(false, HttpStatus.BAD_REQUEST, 1002, "유효하지 않은 ID 토큰입니다."),
    TOKEN_VERIFICATION_FAILED(false, HttpStatus.UNAUTHORIZED, 1003, "토큰 검증에 실패했습니다."),
    USER_NOT_FOUND(false, HttpStatus.NOT_FOUND, 1004, "사용자를 찾을 수 없습니다."),
    TOKEN_GENERATION_FAILED(false, HttpStatus.INTERNAL_SERVER_ERROR, 1005, "토큰 생성에 실패했습니다."),
    REFRESH_TOKEN_FAILED(false, HttpStatus.UNAUTHORIZED, 1006, "리프레시 토큰 처리에 실패했습니다."),
    INVALID_AUTH_STATE(false, HttpStatus.BAD_REQUEST, 1007, "유효하지 않은 인증 상태입니다."),
    YOUTUBE_AUTH_REQUIRED(false, HttpStatus.FORBIDDEN, 1008, "유튜브 권한이 필요합니다."),

    // 2000~ 2999 : 방 관련 에러
    NOT_FOUND_ROOM(false, HttpStatus.NOT_FOUND, 2000, "해당 방이 존재하지 않습니다."),
    NOT_VALID_HOST(false, HttpStatus.FORBIDDEN, 2001, "방의 호스트가 아니거나 이미 없는 방입니다."),
    ALREADY_MEMBER_ROOM(false, HttpStatus.BAD_REQUEST, 2002, "이미 방에 참여 중입니다."),
    NOT_VALID_PASSWORD(false, HttpStatus.BAD_REQUEST, 2003, "비밀번호가 일치하지 않습니다."),

    // 3000~ 3999 : 비디오 관련 에러
    NOT_FOUND_VIDEO(false, HttpStatus.NOT_FOUND, 3000, "해당 비디오가 존재하지 않습니다."),
    NOT_VALID_VIDEO(false, HttpStatus.BAD_REQUEST, 3001, "비디오가 유효하지 않습니다."),
    
    // 4000~ 4999 : 유튜브 API 관련 에러
    YOUTUBE_API_ERROR(false, HttpStatus.INTERNAL_SERVER_ERROR, 4000, "유튜브 API 호출 중 오류가 발생했습니다."),
    YOUTUBE_UPLOAD_FAILED(false, HttpStatus.INTERNAL_SERVER_ERROR, 4001, "유튜브 업로드에 실패했습니다."),
    YOUTUBE_ACCESS_DENIED(false, HttpStatus.FORBIDDEN, 4002, "유튜브 권한이 거부되었습니다.");

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