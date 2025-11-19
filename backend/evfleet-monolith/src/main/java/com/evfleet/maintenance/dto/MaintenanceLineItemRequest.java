package com.evfleet.maintenance.dto;

import com.evfleet.maintenance.model.MaintenanceLineItem;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * DTO for MaintenanceLineItem requests
 *
 * @author SEV Platform Team
 * @version 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MaintenanceLineItemRequest {
    private String description;
    private String type; // PART, LABOR, TAX, OTHER
    private Integer quantity;
    private BigDecimal unitPrice;
    private String partNumber;
    private String supplier;

    public MaintenanceLineItem toEntity(Long maintenanceRecordId) {
        MaintenanceLineItem lineItem = MaintenanceLineItem.builder()
                .maintenanceRecordId(maintenanceRecordId)
                .description(description)
                .type(MaintenanceLineItem.LineItemType.valueOf(type.toUpperCase()))
                .quantity(quantity)
                .unitPrice(unitPrice)
                .partNumber(partNumber)
                .supplier(supplier)
                .build();
        lineItem.calculateTotalPrice();
        return lineItem;
    }
}
