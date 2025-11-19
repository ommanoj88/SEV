package com.evfleet.routing.dto;

import com.evfleet.routing.model.RoutePlan;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for creating/updating a route plan
 *
 * @author SEV Platform Team
 * @version 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RoutePlanRequest {

    @NotNull(message = "Company ID is required")
    private Long companyId;

    private Long vehicleId;

    private Long driverId;

    @NotBlank(message = "Route name is required")
    private String routeName;

    @NotNull(message = "Optimization criteria is required")
    private RoutePlan.OptimizationCriteria optimizationCriteria;

    private String notes;
}
