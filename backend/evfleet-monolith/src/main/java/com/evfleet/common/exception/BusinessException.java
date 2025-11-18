package com.evfleet.common.exception;

/**
 * Exception for business logic violations
 *
 * @author SEV Platform Team
 * @version 1.0.0
 */
public class BusinessException extends RuntimeException {

    public BusinessException(String message) {
        super(message);
    }

    public BusinessException(String message, Throwable cause) {
        super(message, cause);
    }
}
