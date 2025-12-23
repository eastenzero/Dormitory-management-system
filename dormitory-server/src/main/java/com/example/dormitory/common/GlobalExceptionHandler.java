package com.example.dormitory.common;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.servlet.resource.NoResourceFoundException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiResponse<Void> handleValidation(MethodArgumentNotValidException ex) {
        String msg = ex.getBindingResult().getAllErrors().isEmpty()
                ? "validation failed"
                : ex.getBindingResult().getAllErrors().get(0).getDefaultMessage();
        return ApiResponse.fail(40001, msg);
    }

     @ExceptionHandler(BizException.class)
     public ResponseEntity<ApiResponse<Void>> handleBiz(BizException ex) {
         HttpStatus status;
         if (ex.getCode() >= 40900 && ex.getCode() < 41000) {
             status = HttpStatus.CONFLICT;
         } else {
             status = HttpStatus.BAD_REQUEST;
         }
         return ResponseEntity.status(status)
                 .body(ApiResponse.fail(ex.getCode(), ex.getMessage()));
     }

     @ExceptionHandler(DataIntegrityViolationException.class)
     public ResponseEntity<ApiResponse<Void>> handleDataIntegrity(DataIntegrityViolationException ex) {
         return ResponseEntity.status(HttpStatus.CONFLICT)
                 .body(ApiResponse.fail(40900, "data integrity violation"));
     }

     @ExceptionHandler(NoResourceFoundException.class)
     public ResponseEntity<ApiResponse<Void>> handleNoResourceFound(NoResourceFoundException ex) {
         return ResponseEntity.status(HttpStatus.NOT_FOUND)
                 .body(ApiResponse.fail(40400, "not found"));
     }

    @ExceptionHandler(AccessDeniedException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public ApiResponse<Void> handleAccessDenied(AccessDeniedException ex) {
        return ApiResponse.fail(40301, "forbidden");
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiResponse<Void> handleAny(Exception ex) {
        return ApiResponse.fail(50000, ex.getMessage() == null ? "system error" : ex.getMessage());
    }
}
