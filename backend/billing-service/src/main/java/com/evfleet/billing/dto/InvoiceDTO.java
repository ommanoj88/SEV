package com.evfleet.billing.dto;

import com.evfleet.billing.entity.Invoice;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;

/**
 * DTO for invoice response with full breakdown.
 *
 * PR 18: Invoice Generation
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InvoiceDTO {

    private String id;
    private String companyId;
    private String invoiceNumber;
    private LocalDate invoiceDate;
    private LocalDate dueDate;
    private LocalDate paidDate;
    private String status;
    private BigDecimal amount;
    private BigDecimal tax;
    private BigDecimal totalAmount;
    private Map<String, Object> items;

    /**
     * Convert Invoice entity to InvoiceDTO
     */
    public static InvoiceDTO fromEntity(Invoice invoice) {
        if (invoice == null) {
            return null;
        }

        return InvoiceDTO.builder()
                .id(invoice.getId())
                .companyId(invoice.getCompanyId())
                .invoiceNumber(invoice.getInvoiceNumber())
                .invoiceDate(invoice.getCreatedAt().toLocalDate())
                .dueDate(invoice.getDueDate())
                .paidDate(invoice.getPaidDate())
                .status(invoice.getStatus())
                .amount(invoice.getAmount())
                .tax(invoice.getTax())
                .totalAmount(invoice.getTotalAmount())
                .items(invoice.getItems())
                .build();
    }

    /**
     * Convert InvoiceDTO to Invoice entity
     */
    public static Invoice toEntity(InvoiceDTO dto) {
        if (dto == null) {
            return null;
        }

        Invoice invoice = new Invoice();
        invoice.setId(dto.getId());
        invoice.setCompanyId(dto.getCompanyId());
        invoice.setInvoiceNumber(dto.getInvoiceNumber());
        invoice.setDueDate(dto.getDueDate());
        invoice.setPaidDate(dto.getPaidDate());
        invoice.setStatus(dto.getStatus());
        invoice.setAmount(dto.getAmount());
        invoice.setTax(dto.getTax());
        invoice.setTotalAmount(dto.getTotalAmount());
        invoice.setItems(dto.getItems());

        return invoice;
    }
}
