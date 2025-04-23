package com.stroycut.global.common.exception.handler;

import com.stroycut.global.common.exception.exception.BusinessException;
import com.stroycut.global.common.model.dto.BaseResponse;
import com.stroycut.global.common.model.dto.BaseResponseStatus;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Enumeration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Order(0)
@RestControllerAdvice(annotations = RestController.class)
public class GlobalExceptionHandler {

    private static final Logger LOG = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<BaseResponse<BaseResponseStatus>> handleBusinessException(
        BusinessException e, HttpServletRequest req) {
        LOG.error("BusinessException occurred: {}", e.getMessage());

        return ResponseEntity.status(e.getBaseResponseStatus().getHttpStatus())
            .body(new BaseResponse<>(e.getBaseResponseStatus()));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<BaseResponse<BaseResponseStatus>> handleHttpMessageNotReadableException(
        HttpMessageNotReadableException e, HttpServletRequest req) {
        LOG.error("JSON Parse error occurred: {}", e.getMessage());

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .body(new BaseResponse<>(BaseResponseStatus.BAD_REQUEST));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> handleException(Exception e, HttpServletRequest req) {
        LOG.error("Exception occurred: {}", e.getMessage());
        return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
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
