package com.stroycut.global.exception;

import com.stroycut.global.response.ApiResponse;
import com.stroycut.global.model.dto.BaseResponse;
import com.stroycut.global.model.dto.BaseResponseStatus;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Enumeration;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * BusinessException 처리
     */
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<BaseResponse<BaseResponseStatus>> handleBusinessException(
        BusinessException e, HttpServletRequest req) {
        log.error("BusinessException occurred: {}", e.getMessage());

        return ResponseEntity.status(e.getBaseResponseStatus().getHttpStatus())
            .body(new BaseResponse<>(e.getBaseResponseStatus()));
    }

    /**
     * javax.validation.Valid 또는 @Validated 바인딩 에러시 발생
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    protected ResponseEntity<ApiResponse<Void>> handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        log.error("handleMethodArgumentNotValidException", e);
        String errorMessage = e.getBindingResult().getAllErrors().get(0).getDefaultMessage();
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(errorMessage));
    }

    /**
     * @ModelAttribute 으로 바인딩 에러시 발생
     */
    @ExceptionHandler(BindException.class)
    protected ResponseEntity<ApiResponse<Void>> handleBindException(BindException e) {
        log.error("handleBindException", e);
        String errorMessage = e.getBindingResult().getAllErrors().get(0).getDefaultMessage();
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(errorMessage));
    }

    /**
     * 엔티티를 찾을 수 없을 때 발생하는 예외 처리
     */
    @ExceptionHandler(EntityNotFoundException.class)
    protected ResponseEntity<ApiResponse<Void>> handleEntityNotFoundException(EntityNotFoundException e) {
        log.error("handleEntityNotFoundException", e);
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error(e.getMessage()));
    }

    /**
     * 유효하지 않은 값일 때 발생하는 예외 처리
     */
    @ExceptionHandler(InvalidValueException.class)
    protected ResponseEntity<ApiResponse<Void>> handleInvalidValueException(InvalidValueException e) {
        log.error("handleInvalidValueException", e);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(e.getMessage()));
    }

    /**
     * JSON 파싱 에러 처리
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<BaseResponse<BaseResponseStatus>> handleHttpMessageNotReadableException(
        HttpMessageNotReadableException e, HttpServletRequest req) {
        log.error("JSON Parse error occurred: {}", e.getMessage());

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .body(new BaseResponse<>(BaseResponseStatus.BAD_REQUEST));
    }

    /**
     * 그 외 모든 예외 처리
     */
    @ExceptionHandler(Exception.class)
    protected ResponseEntity<ApiResponse<Void>> handleException(Exception e) {
        log.error("handleException", e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("서버 오류가 발생했습니다."));
    }

    private String getParams(HttpServletRequest req) {
        StringBuilder params = new StringBuilder();
        Enumeration<String> keys = req.getParameterNames();
        while (keys.hasMoreElements()) {
            String key = keys.nextElement();
            params.append("- ").append(key).append(": ").append(req.getParameter(key)).append("\n");
        }
        return params.toString();
    }
}