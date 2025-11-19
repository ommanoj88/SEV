package com.evfleet.maintenance.dto;

import com.evfleet.maintenance.model.MaintenanceLineItem;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * DTO for MaintenanceLineItem responses
 *
 * @author SEV Platform Team
 * @version 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MaintenanceLineItemResponse {
    private Long id;
    private Long maintenanceRecordId;
    private String description;
    private String type;
    private Integer quantity;
    private BigDecimal unitPrice;
    private BigDecimal totalPrice;
    private String partNumber;
    private String supplier;

    public static MaintenanceLineItemResponse fromEntity(MaintenanceLineItem entity) {
        return MaintenanceLineItemResponse.builder()
                .id(entity.getId())
                .maintenanceRecordId(entity.getMaintenanceRecordId())
                .description(entity.getDescription())
                .type(entity.getType().name())
                .quantity(entity.getQuantity())
                .unitPrice(entity.getUnitPrice())
                .totalPrice(entity.getTotalPrice())
                .partNumber(entity.getPartNumber())
                .supplier(entity.getSupplier())
                .build();
    }
}
