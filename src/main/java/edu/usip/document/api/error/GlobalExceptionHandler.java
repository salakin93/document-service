package edu.usip.document.api.error;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<edu.usip.document.api.error.ApiErrorResponse> validation(MethodArgumentNotValidException ex, HttpServletRequest req) {
        Map<String, String> errors = new HashMap<>();
        for (FieldError fe : ex.getBindingResult().getFieldErrors()) {
            errors.put(fe.getField(), fe.getDefaultMessage());
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(edu.usip.document.api.error.ApiErrorResponse.of(400, "Validation failed", req.getRequestURI(), errors));
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<edu.usip.document.api.error.ApiErrorResponse> maxSize(MaxUploadSizeExceededException ex, HttpServletRequest req) {
        return ResponseEntity.status(HttpStatus.PAYLOAD_TOO_LARGE)
                .body(edu.usip.document.api.error.ApiErrorResponse.of(413, "File too large", req.getRequestURI(),
                        Map.of("message", "El archivo excede el tamaño permitido")));
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<edu.usip.document.api.error.ApiErrorResponse> denied(AccessDeniedException ex, HttpServletRequest req) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(edu.usip.document.api.error.ApiErrorResponse.of(403, "Forbidden", req.getRequestURI(), Map.of("message", ex.getMessage())));
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<edu.usip.document.api.error.ApiErrorResponse> runtime(RuntimeException ex, HttpServletRequest req) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(edu.usip.document.api.error.ApiErrorResponse.of(400, "Bad request", req.getRequestURI(), Map.of("message", ex.getMessage())));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<edu.usip.document.api.error.ApiErrorResponse> general(Exception ex, HttpServletRequest req) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(edu.usip.document.api.error.ApiErrorResponse.of(500, "Internal server error", req.getRequestURI(),
                        Map.of("message", "Ocurrió un error inesperado")));
    }
}