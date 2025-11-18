package com.evfleet.common.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Standard API Response wrapper for all endpoints
 *
 * Provides a consistent structure for API responses across all modules.
 * Use this for successful responses with data.
 *
 * Usage:
 * <pre>
 * {@code
 * return ResponseEntity.ok(
 *     ApiResponse.success("User created", userResponse)
 * );
 * }
 * </pre>
 *
 * @author SEV Platform Team
 * @version 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {

    /**
     * Response timestamp
     */
    @Builder.Default
    private LocalDateTime timestamp = LocalDateTime.now();

    /**
     * Success flag (true for success, false for errors)
     */
    private boolean success;

    /**
     * Human-readable message
     */
    private String message;

    /**
     * Response data (generic type)
     */
    private T data;

    /**
     * HTTP status code
     */
    private Integer status;

    /**
     * Create a success response with data
     */
    public static <T> ApiResponse<T> success(T data) {
        return ApiResponse.<T>builder()
                .timestamp(LocalDateTime.now())
                .success(true)
                .message("Success")
                .data(data)
                .status(200)
                .build();
    }

    /**
     * Create a success response with custom message and data
     */
    public static <T> ApiResponse<T> success(String message, T data) {
        return ApiResponse.<T>builder()
                .timestamp(LocalDateTime.now())
                .success(true)
                .message(message)
                .data(data)
                .status(200)
                .build();
    }

    /**
     * Create a success response with message only (no data)
     */
    public static <T> ApiResponse<T> success(String message) {
        return ApiResponse.<T>builder()
                .timestamp(LocalDateTime.now())
                .success(true)
                .message(message)
                .status(200)
                .build();
    }

    /**
     * Create an error response with message
     */
    public static <T> ApiResponse<T> error(String message) {
        return ApiResponse.<T>builder()
                .timestamp(LocalDateTime.now())
                .success(false)
                .message(message)
                .status(500)
                .build();
    }

    /**
     * Create an error response with custom status and message
     */
    public static <T> ApiResponse<T> error(int status, String message) {
        return ApiResponse.<T>builder()
                .timestamp(LocalDateTime.now())
                .success(false)
                .message(message)
                .status(status)
                .build();
    }
}
