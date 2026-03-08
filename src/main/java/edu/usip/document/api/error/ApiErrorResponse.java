package edu.usip.document.api.error;

import lombok.Builder;
import lombok.Getter;

import java.time.Instant;

@Getter
@Builder
public class ApiErrorResponse {
    private String timestamp;
    private int status;
    private String error;
    private String path;
    private Object details;

    public static ApiErrorResponse of(int status, String error, String path, Object details) {
        return ApiErrorResponse.builder()
                .timestamp(Instant.now().toString())
                .status(status)
                .error(error)
                .path(path)
                .details(details)
                .build();
    }
}