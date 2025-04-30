package com.stroycut.global.exception;

import com.stroycut.global.model.dto.BaseResponseStatus;
import lombok.Getter;

@Getter
public class BusinessException extends RuntimeException {

    private final BaseResponseStatus baseResponseStatus;

    public BusinessException(BaseResponseStatus baseResponseStatus) {
        super(baseResponseStatus.getMessage());
        this.baseResponseStatus = baseResponseStatus;
    }
}