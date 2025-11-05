package com.evfleet.billing.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaymentProcessRequest {
    private BigDecimal amount;
    private String paymentMethodId;
    private String invoiceId;
}
