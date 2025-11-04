package com.evfleet.billing.infrastructure.adapter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import java.math.BigDecimal;
import java.util.UUID;

@Slf4j
@Component
public class RazorpayAdapter {
    public String processPayment(String invoiceId, BigDecimal amount) {
        log.info("Processing payment for invoice: {}, amount: {}", invoiceId, amount);
        // Mock implementation - would integrate with Razorpay API
        String transactionId = "TXN_" + UUID.randomUUID().toString();
        log.info("Payment processed. Transaction ID: {}", transactionId);
        return transactionId;
    }

    public boolean refundPayment(String transactionId, BigDecimal amount) {
        log.info("Refunding payment: {}, amount: {}", transactionId, amount);
        // Mock implementation
        return true;
    }
}
