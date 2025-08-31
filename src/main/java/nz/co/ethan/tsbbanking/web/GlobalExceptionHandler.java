package nz.co.ethan.tsbbanking.web;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import nz.co.ethan.tsbbanking.common.ApiError;
import nz.co.ethan.tsbbanking.common.BizException;
import nz.co.ethan.tsbbanking.common.ErrorCodes;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.ErrorResponseException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BizException.class)
    public ResponseEntity<ApiError> handleBiz(BizException ex, HttpServletRequest req) {
        log.warn("BizException at {} {}: {}", req.getMethod(), req.getRequestURI(), ex.getMessage());
        var body = ApiError.of(ex.getCode(), ex.getMessage(), HttpStatus.BAD_REQUEST.value(), req.getRequestURI());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleValidation(MethodArgumentNotValidException ex, HttpServletRequest req) {
        var fe = ex.getBindingResult().getFieldErrors().stream().findFirst();
        String msg = fe.map(e -> e.getField() + " " + e.getDefaultMessage()).orElse("Validation failed");
        log.warn("Validation error at {} {}: {}", req.getMethod(), req.getRequestURI(), msg);
        var body = ApiError.of(ErrorCodes.VALIDATION_ERROR.code(), msg, HttpStatus.BAD_REQUEST.value(), req.getRequestURI());
        return ResponseEntity.badRequest().body(body);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiError> handleConstraint(ConstraintViolationException ex, HttpServletRequest req) {
        String msg = ex.getConstraintViolations().stream().findFirst()
                .map(v -> v.getPropertyPath() + " " + v.getMessage())
                .orElse("Constraint violation");
        log.warn("Constraint violation at {} {}: {}", req.getMethod(), req.getRequestURI(), msg);
        var body = ApiError.of(ErrorCodes.VALIDATION_ERROR.code(), msg, HttpStatus.BAD_REQUEST.value(), req.getRequestURI());
        return ResponseEntity.badRequest().body(body);
    }

    // ★ 常见：非空/唯一/外键等数据库约束
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiError> handleIntegrity(DataIntegrityViolationException ex, HttpServletRequest req) {
        log.error("DB constraint error at {} {}", req.getMethod(), req.getRequestURI(), ex);
        var body = ApiError.of(ErrorCodes.ERROR.code(), "Database constraint violated", 409, req.getRequestURI());
        return ResponseEntity.status(409).body(body);
    }

    @ExceptionHandler({ MissingRequestHeaderException.class, MissingServletRequestParameterException.class })
    public ResponseEntity<ApiError> handleMissingParam(Exception ex, HttpServletRequest req) {
        log.warn("Missing param/header at {} {}: {}", req.getMethod(), req.getRequestURI(), ex.getMessage());
        var body = ApiError.of(ErrorCodes.BAD_REQUEST.code(), ex.getMessage(), HttpStatus.BAD_REQUEST.value(), req.getRequestURI());
        return ResponseEntity.badRequest().body(body);
    }

    @ExceptionHandler(ErrorResponseException.class)
    public ResponseEntity<ApiError> handleErrorResponse(ErrorResponseException ex, HttpServletRequest req) {
        log.warn("ErrorResponseException {} at {} {}: {}", ex.getStatusCode().value(), req.getMethod(), req.getRequestURI(), ex.getMessage());
        var status = ex.getStatusCode().value();
        var body = ApiError.of(ErrorCodes.ERROR.code(), ex.getMessage(), status, req.getRequestURI());
        return ResponseEntity.status(status).body(body);
    }

    // ★ 兜底：一定要打 ERROR 栈！
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleOther(Exception ex, HttpServletRequest req) {
        log.error("Unhandled error at {} {}", req.getMethod(), req.getRequestURI(), ex);
        var body = ApiError.of(ErrorCodes.INTERNAL_ERROR.code(), "Internal server error",
                HttpStatus.INTERNAL_SERVER_ERROR.value(), req.getRequestURI());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(body);
    }
}
