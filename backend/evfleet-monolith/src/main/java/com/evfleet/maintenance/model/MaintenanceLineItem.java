package com.evfleet.maintenance.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Maintenance Line Item Entity
 * Represents individual parts or labor items within a maintenance record
 *
 * @author SEV Platform Team
 * @version 1.0.0
 */
@Entity
@Table(name = "maintenance_line_items", indexes = {
    @Index(name = "idx_line_item_record", columnList = "maintenance_record_id")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MaintenanceLineItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "maintenance_record_id", nullable = false)
    private Long maintenanceRecordId;

    @Column(nullable = false, length = 200)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private LineItemType type;

    @Column(nullable = false)
    private Integer quantity;

    @Column(name = "unit_price", nullable = false, precision = 10, scale = 2)
    private BigDecimal unitPrice;

    @Column(name = "total_price", nullable = false, precision = 10, scale = 2)
    private BigDecimal totalPrice;

    @Column(length = 100)
    private String partNumber;

    @Column(length = 100)
    private String supplier;

    public enum LineItemType {
        PART,
        LABOR,
        TAX,
        OTHER
    }

    /**
     * Calculate total price from quantity and unit price
     */
    public void calculateTotalPrice() {
        if (quantity != null && unitPrice != null) {
            totalPrice = unitPrice.multiply(BigDecimal.valueOf(quantity));
        }
    }
}
