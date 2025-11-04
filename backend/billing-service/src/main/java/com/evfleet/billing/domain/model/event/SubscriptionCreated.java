package com.evfleet.billing.domain.model.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SubscriptionCreated {
    private String subscriptionId;
    private String companyId;
    private String planType;
    private BigDecimal amount;
    private LocalDateTime timestamp = LocalDateTime.now();

    public SubscriptionCreated(String subscriptionId, String companyId, String planType, BigDecimal amount) {
        this.subscriptionId = subscriptionId;
        this.companyId = companyId;
        this.planType = planType;
        this.amount = amount;
        this.timestamp = LocalDateTime.now();
    }
}
