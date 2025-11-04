package com.evfleet.billing.domain.model.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaymentReceived {
    private String paymentId;
    private String invoiceId;
    private BigDecimal amount;
    private String transactionId;
    private LocalDateTime timestamp = LocalDateTime.now();

    public PaymentReceived(String paymentId, String invoiceId, BigDecimal amount, String transactionId) {
        this.paymentId = paymentId;
        this.invoiceId = invoiceId;
        this.amount = amount;
        this.transactionId = transactionId;
        this.timestamp = LocalDateTime.now();
    }
}
