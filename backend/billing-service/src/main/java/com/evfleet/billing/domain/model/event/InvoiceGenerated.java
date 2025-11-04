package com.evfleet.billing.domain.model.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class InvoiceGenerated {
    private String invoiceId;
    private String companyId;
    private String invoiceNumber;
    private BigDecimal amount;
    private LocalDateTime timestamp = LocalDateTime.now();

    public InvoiceGenerated(String invoiceId, String companyId, String invoiceNumber, BigDecimal amount) {
        this.invoiceId = invoiceId;
        this.companyId = companyId;
        this.invoiceNumber = invoiceNumber;
        this.amount = amount;
        this.timestamp = LocalDateTime.now();
    }
}
