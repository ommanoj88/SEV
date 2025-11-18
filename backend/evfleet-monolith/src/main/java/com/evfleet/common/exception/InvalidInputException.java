package com.evfleet.common.exception;

/**
 * Exception for invalid input data
 *
 * @author SEV Platform Team
 * @version 1.0.0
 */
public class InvalidInputException extends RuntimeException {

    public InvalidInputException(String message) {
        super(message);
    }

    public InvalidInputException(String field, String reason) {
        super(String.format("Invalid %s: %s", field, reason));
    }

    public InvalidInputException(String message, Throwable cause) {
        super(message, cause);
    }
}
