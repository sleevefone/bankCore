package com.payhub.bankcore.controller;

import java.util.HashMap;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;
import org.springframework.web.server.ResponseStatusException;

@Slf4j
@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, Object> handleValidation(MethodArgumentNotValidException ex) {
        log.warn("Request validation failed", ex);
        Map<String, String> errors = new HashMap<>();
        for (FieldError fieldError : ex.getBindingResult().getFieldErrors()) {
            errors.put(fieldError.getField(), fieldError.getDefaultMessage());
        }
        return Map.of(
                "code", "VALIDATION_ERROR",
                "message", "Request validation failed",
                "errors", errors
        );
    }

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<Map<String, Object>> handleStatus(ResponseStatusException ex) {
        if (ex.getStatusCode().is5xxServerError()) {
            log.error("Request failed with status exception: status={}, reason={}", ex.getStatusCode(), ex.getReason(), ex);
        } else {
            log.warn("Request failed with status exception: status={}, reason={}", ex.getStatusCode(), ex.getReason());
        }
        return ResponseEntity.status(ex.getStatusCode()).body(Map.of(
                "code", ex.getStatusCode().toString(),
                "message", ex.getReason()
        ));
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<Void> handleNoResource(NoResourceFoundException ex) {
        String resourcePath = ex.getResourcePath();
        if (resourcePath != null && (
                resourcePath.equals("favicon.ico")
                        || resourcePath.startsWith(".well-known/")
                        || resourcePath.startsWith("apple-touch-icon")
                        || resourcePath.startsWith("browserconfig")
                        || resourcePath.startsWith("site.webmanifest")
        )) {
            log.debug("Ignore browser probe static resource: {}", resourcePath);
            return ResponseEntity.noContent().build();
        }
        log.warn("Static resource not found: {}", resourcePath);
        return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleUnexpected(Exception ex) {
        log.error("Unhandled exception in bank-core", ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "code", "INTERNAL_SERVER_ERROR",
                "message", "Internal server error"
        ));
    }
}
