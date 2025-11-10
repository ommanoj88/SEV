package com.evfleet.billing.exception;

/**
 * Custom exception for billing-related errors.
 * Used in invoice generation and payment processing.
 *
 * PR 18: Invoice Generation - Exception handling
 */
public class BillingException extends RuntimeException {

    public BillingException(String message) {
        super(message);
    }

    public BillingException(String message, Throwable cause) {
        super(message, cause);
    }

    public BillingException(Throwable cause) {
        super(cause);
    }
}
