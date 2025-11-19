package com.evfleet.maintenance.service;

import com.evfleet.common.exception.ResourceNotFoundException;
import com.evfleet.fleet.model.Vehicle;
import com.evfleet.fleet.repository.VehicleRepository;
import com.evfleet.maintenance.dto.MaintenanceAlertResponse;
import com.evfleet.maintenance.dto.MaintenanceLineItemRequest;
import com.evfleet.maintenance.dto.MaintenanceLineItemResponse;
import com.evfleet.maintenance.dto.MaintenanceRecordRequest;
import com.evfleet.maintenance.dto.MaintenanceRecordResponse;
import com.evfleet.maintenance.model.MaintenanceLineItem;
import com.evfleet.maintenance.model.MaintenancePolicy;
import com.evfleet.maintenance.model.MaintenanceRecord;
import com.evfleet.maintenance.repository.MaintenanceLineItemRepository;
import com.evfleet.maintenance.repository.MaintenancePolicyRepository;
import com.evfleet.maintenance.repository.MaintenanceRecordRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Maintenance Service
 * Handles all maintenance-related business logic
 *
 * @author SEV Platform Team
 * @version 1.0.0
 */
@Service
@Slf4j
@RequiredArgsConstructor
@Transactional
public class MaintenanceService {

    private final MaintenanceRecordRepository maintenanceRecordRepository;
    private final VehicleRepository vehicleRepository;
    private final com.evfleet.analytics.service.AnalyticsService analyticsService;
    private final MaintenancePolicyRepository maintenancePolicyRepository;
    private final MaintenanceLineItemRepository maintenanceLineItemRepository;

    public MaintenanceRecordResponse createMaintenanceRecord(Long companyId, MaintenanceRecordRequest request) {
        log.info("POST /api/v1/maintenance/records - Creating maintenance record for vehicle: {}", request.getVehicleId());

        // Validate vehicle exists and get fuel type
        Vehicle vehicle = vehicleRepository.findById(request.getVehicleId())
                .orElseThrow(() -> new ResourceNotFoundException("Vehicle", "id", request.getVehicleId()));

        // Validate maintenance type is appropriate for vehicle fuel type
        MaintenanceRecord.MaintenanceType maintenanceType = MaintenanceRecord.MaintenanceType.valueOf(request.getType().toUpperCase());
        validateMaintenanceTypeForFuelType(maintenanceType, vehicle.getFuelType());

        // Convert attachment URLs list to comma-separated string
        String attachmentUrlsString = null;
        if (request.getAttachmentUrls() != null && !request.getAttachmentUrls().isEmpty()) {
            attachmentUrlsString = String.join(",", request.getAttachmentUrls());
        }

        MaintenanceRecord record = MaintenanceRecord.builder()
                .vehicleId(request.getVehicleId())
                .companyId(companyId)
                .type(maintenanceType)
                .scheduledDate(request.getScheduledDate())
                .completedDate(request.getCompletedDate())
                .status(request.getStatus() != null ?
                        MaintenanceRecord.MaintenanceStatus.valueOf(request.getStatus().toUpperCase()) :
                        MaintenanceRecord.MaintenanceStatus.SCHEDULED)
                .cost(request.getCost())
                .description(request.getDescription())
                .serviceProvider(request.getServiceProvider())
                .attachmentUrls(attachmentUrlsString)
                .build();

        MaintenanceRecord saved = maintenanceRecordRepository.save(record);

        // Save line items if provided
        if (request.getLineItems() != null && !request.getLineItems().isEmpty()) {
            for (MaintenanceLineItemRequest lineItemRequest : request.getLineItems()) {
                MaintenanceLineItem lineItem = lineItemRequest.toEntity(saved.getId());
                maintenanceLineItemRepository.save(lineItem);
            }

            // Recalculate total cost from line items
            BigDecimal totalCost = maintenanceLineItemRepository.calculateTotalCostForRecord(saved.getId());
            if (totalCost != null) {
                saved.setCost(totalCost);
                saved = maintenanceRecordRepository.save(saved);
            }
        }

        log.info("Maintenance record created successfully: {}", saved.getId());
        return getMaintenanceRecordResponseWithLineItems(saved);
    }

    @Transactional(readOnly = true)
    public List<MaintenanceRecordResponse> getAllMaintenanceRecords(Long companyId) {
        log.info("GET /api/v1/maintenance/records - Fetching all maintenance records for company: {}", companyId);
        List<MaintenanceRecord> records = maintenanceRecordRepository.findByCompanyId(companyId);
        return records.stream()
                .map(this::getMaintenanceRecordResponseWithLineItems)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public MaintenanceRecordResponse getMaintenanceRecordById(Long id) {
        log.info("GET /api/v1/maintenance/records/{} - Fetching maintenance record", id);
        MaintenanceRecord record = maintenanceRecordRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("MaintenanceRecord", "id", id));
        return getMaintenanceRecordResponseWithLineItems(record);
    }

    @Transactional(readOnly = true)
    public List<MaintenanceRecordResponse> getMaintenanceRecordsByVehicle(Long vehicleId) {
        log.info("GET /api/v1/maintenance/records/vehicle/{} - Fetching maintenance records", vehicleId);
        List<MaintenanceRecord> records = maintenanceRecordRepository.findByVehicleId(vehicleId);
        return records.stream()
                .map(this::getMaintenanceRecordResponseWithLineItems)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<MaintenanceRecordResponse> getUpcomingMaintenance(Long companyId) {
        log.info("GET /api/v1/maintenance/records/upcoming - Fetching upcoming maintenance for company: {}", companyId);
        List<MaintenanceRecord> records = maintenanceRecordRepository.findByCompanyIdAndStatusAndScheduledDateAfter(
                companyId,
                MaintenanceRecord.MaintenanceStatus.SCHEDULED,
                LocalDate.now()
        );
        return records.stream()
                .map(MaintenanceRecordResponse::fromEntity)
                .collect(Collectors.toList());
    }

    public MaintenanceRecordResponse updateMaintenanceRecord(Long id, MaintenanceRecordRequest request) {
        log.info("PUT /api/v1/maintenance/records/{} - Updating maintenance record", id);

        MaintenanceRecord record = maintenanceRecordRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("MaintenanceRecord", "id", id));

        if (request.getType() != null) {
            record.setType(MaintenanceRecord.MaintenanceType.valueOf(request.getType().toUpperCase()));
        }
        if (request.getScheduledDate() != null) {
            record.setScheduledDate(request.getScheduledDate());
        }
        if (request.getCompletedDate() != null) {
            record.setCompletedDate(request.getCompletedDate());
        }
        if (request.getStatus() != null) {
            record.setStatus(MaintenanceRecord.MaintenanceStatus.valueOf(request.getStatus().toUpperCase()));
        }
        if (request.getCost() != null) {
            record.setCost(request.getCost());
        }
        if (request.getDescription() != null) {
            record.setDescription(request.getDescription());
        }
        if (request.getServiceProvider() != null) {
            record.setServiceProvider(request.getServiceProvider());
        }

        MaintenanceRecord updated = maintenanceRecordRepository.save(record);
        log.info("Maintenance record updated successfully: {}", id);
        return MaintenanceRecordResponse.fromEntity(updated);
    }

    public MaintenanceRecordResponse completeMaintenance(Long id) {
        log.info("POST /api/v1/maintenance/records/{}/complete - Completing maintenance record", id);

        MaintenanceRecord record = maintenanceRecordRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("MaintenanceRecord", "id", id));

        record.setStatus(MaintenanceRecord.MaintenanceStatus.COMPLETED);
        record.setCompletedDate(LocalDate.now());

        MaintenanceRecord updated = maintenanceRecordRepository.save(record);
        
        // Update analytics with maintenance cost
        if (updated.getCost() != null && updated.getCost().compareTo(java.math.BigDecimal.ZERO) > 0) {
            try {
                analyticsService.updateMaintenanceCost(
                    updated.getCompanyId(), 
                    updated.getCompletedDate(), 
                    updated.getCost()
                );
                log.info("Analytics updated with maintenance cost: {}", updated.getCost());
            } catch (Exception e) {
                log.error("Failed to update analytics for maintenance completion", e);
                // Don't fail the operation if analytics update fails
            }
        }
        
        log.info("Maintenance record completed successfully: {}", id);
        return MaintenanceRecordResponse.fromEntity(updated);
    }

    public void deleteMaintenanceRecord(Long id) {
        log.info("DELETE /api/v1/maintenance/records/{} - Deleting maintenance record", id);

        if (!maintenanceRecordRepository.existsById(id)) {
            throw new ResourceNotFoundException("MaintenanceRecord", "id", id);
        }

        maintenanceRecordRepository.deleteById(id);
        log.info("Maintenance record deleted successfully: {}", id);
    }

    /**
     * Get maintenance alerts for a company
     * Returns upcoming maintenance within the next 30 days, prioritized by urgency
     */
    @Transactional(readOnly = true)
    public List<MaintenanceAlertResponse> getMaintenanceAlerts(Long companyId, Integer daysAhead) {
        log.info("GET /api/v1/maintenance/alerts - Fetching alerts for company: {}, daysAhead: {}", 
                companyId, daysAhead);
        
        LocalDate endDate = LocalDate.now().plusDays(daysAhead != null ? daysAhead : 30);
        List<MaintenanceRecord> upcomingRecords = maintenanceRecordRepository.findUpcomingMaintenanceAlerts(
                companyId, endDate);
        
        return upcomingRecords.stream()
                .map(record -> {
                    Vehicle vehicle = vehicleRepository.findById(record.getVehicleId()).orElse(null);
                    LocalDate now = LocalDate.now();
                    long daysUntil = ChronoUnit.DAYS.between(now, record.getScheduledDate());
                    
                    MaintenanceAlertResponse.Priority priority;
                    if (daysUntil < 0) {
                        priority = MaintenanceAlertResponse.Priority.HIGH; // Overdue
                    } else if (daysUntil <= 7) {
                        priority = MaintenanceAlertResponse.Priority.MEDIUM; // Due within 7 days
                    } else {
                        priority = MaintenanceAlertResponse.Priority.LOW; // Due within 30 days
                    }
                    
                    return MaintenanceAlertResponse.builder()
                            .id(record.getId())
                            .vehicleId(record.getVehicleId())
                            .vehicleNumber(vehicle != null ? vehicle.getLicensePlate() : "Unknown")
                            .fuelType(vehicle != null ? vehicle.getFuelType() : null)
                            .maintenanceType(record.getType().name())
                            .scheduledDate(record.getScheduledDate())
                            .status(daysUntil < 0 ? "OVERDUE" : record.getStatus().name())
                            .priority(priority)
                            .description(record.getDescription())
                            .daysUntilDue((int) daysUntil)
                            .build();
                })
                .sorted((a, b) -> {
                    // Sort by priority first, then by scheduled date
                    int priorityCompare = a.getPriority().compareTo(b.getPriority());
                    if (priorityCompare != 0) {
                        return priorityCompare;
                    }
                    return a.getScheduledDate().compareTo(b.getScheduledDate());
                })
                .collect(Collectors.toList());
     * Check if vehicle requires maintenance based on mileage policies
     * Called automatically after trip completion
     */
    public void checkAndCreateMaintenanceByMileage(Long vehicleId, Long companyId, Vehicle.VehicleType vehicleType, Double currentDistance) {
        log.debug("Checking maintenance policies for vehicle: {} at distance: {} km", vehicleId, currentDistance);

        // Get active policies for this vehicle type
        List<MaintenancePolicy> policies = maintenancePolicyRepository
                .findByCompanyIdAndVehicleTypeAndActiveTrue(companyId, vehicleType);

        for (MaintenancePolicy policy : policies) {
            if (policy.getMileageIntervalKm() == null || policy.getMileageIntervalKm() <= 0) {
                continue;
            }

            // Find last completed maintenance of this type for this vehicle
            Optional<MaintenanceRecord> lastMaintenance = maintenanceRecordRepository
                    .findTopByVehicleIdAndTypeAndStatusOrderByCompletedDateDesc(
                            vehicleId, 
                            policy.getMaintenanceType(), 
                            MaintenanceRecord.MaintenanceStatus.COMPLETED
                    );

            Double lastMaintenanceDistance = lastMaintenance
                    .map(MaintenanceRecord::getVehicleDistanceKm)
                    .orElse(0.0);

            // Check if policy should trigger
            if (policy.shouldTriggerByMileage(lastMaintenanceDistance, currentDistance)) {
                // Check if maintenance is already scheduled
                boolean alreadyScheduled = maintenanceRecordRepository
                        .existsByVehicleIdAndTypeAndPolicyIdAndStatusIn(
                                vehicleId,
                                policy.getMaintenanceType(),
                                policy.getId(),
                                List.of(MaintenanceRecord.MaintenanceStatus.SCHEDULED, 
                                       MaintenanceRecord.MaintenanceStatus.IN_PROGRESS)
                        );

                if (!alreadyScheduled) {
                    // Create new scheduled maintenance record
                    MaintenanceRecord newRecord = MaintenanceRecord.builder()
                            .vehicleId(vehicleId)
                            .companyId(companyId)
                            .type(policy.getMaintenanceType())
                            .scheduledDate(LocalDate.now().plusDays(7)) // Schedule for next week
                            .status(MaintenanceRecord.MaintenanceStatus.SCHEDULED)
                            .description(String.format("Auto-scheduled: %s - Vehicle reached %,.0f km", 
                                    policy.getName(), currentDistance))
                            .vehicleDistanceKm(currentDistance)
                            .policyId(policy.getId())
                            .build();

                    maintenanceRecordRepository.save(newRecord);
                    log.info("Auto-created maintenance record for vehicle {} - Type: {}, Policy: {}", 
                            vehicleId, policy.getMaintenanceType(), policy.getName());
                }
            }
        }
    }

    /**
     * Helper method to build MaintenanceRecordResponse with line items
     */
    private MaintenanceRecordResponse getMaintenanceRecordResponseWithLineItems(MaintenanceRecord record) {
        MaintenanceRecordResponse response = MaintenanceRecordResponse.fromEntity(record);
        
        // Load line items
        List<MaintenanceLineItem> lineItems = maintenanceLineItemRepository.findByMaintenanceRecordId(record.getId());
        response.setLineItems(lineItems.stream()
                .map(MaintenanceLineItemResponse::fromEntity)
                .collect(Collectors.toList()));
        
        return response;
    }

    /**
     * Validates if a maintenance type is appropriate for a vehicle's fuel type
     */
    private void validateMaintenanceTypeForFuelType(MaintenanceRecord.MaintenanceType maintenanceType, 
                                                      com.evfleet.fleet.model.FuelType fuelType) {
        if (fuelType == null) {
            // If fuel type is not set, allow all maintenance types
            return;
        }

        // Define ICE-specific maintenance types
        List<MaintenanceRecord.MaintenanceType> iceOnlyTypes = List.of(
            MaintenanceRecord.MaintenanceType.OIL_CHANGE,
            MaintenanceRecord.MaintenanceType.FILTER_REPLACEMENT,
            MaintenanceRecord.MaintenanceType.EMISSION_TEST,
            MaintenanceRecord.MaintenanceType.COOLANT_FLUSH,
            MaintenanceRecord.MaintenanceType.TRANSMISSION_SERVICE,
            MaintenanceRecord.MaintenanceType.ENGINE_DIAGNOSTICS
        );

        // Define EV-specific maintenance types
        List<MaintenanceRecord.MaintenanceType> evOnlyTypes = List.of(
            MaintenanceRecord.MaintenanceType.BATTERY_CHECK,
            MaintenanceRecord.MaintenanceType.HV_SYSTEM_CHECK,
            MaintenanceRecord.MaintenanceType.FIRMWARE_UPDATE,
            MaintenanceRecord.MaintenanceType.CHARGING_PORT_INSPECTION,
            MaintenanceRecord.MaintenanceType.THERMAL_MANAGEMENT_CHECK
        );

        // Check if maintenance type is invalid for the fuel type
        if (fuelType == com.evfleet.fleet.model.FuelType.EV && iceOnlyTypes.contains(maintenanceType)) {
            throw new IllegalArgumentException(
                String.format("Maintenance type %s is not applicable for Electric Vehicles", maintenanceType)
            );
        }

        if (fuelType == com.evfleet.fleet.model.FuelType.ICE && evOnlyTypes.contains(maintenanceType)) {
            throw new IllegalArgumentException(
                String.format("Maintenance type %s is not applicable for Internal Combustion Engine vehicles", maintenanceType)
            );
        }

        // HYBRID vehicles can have both ICE and EV maintenance types, so no restrictions
    }

    /**
     * Get all available maintenance types
     */
    public List<String> getAllMaintenanceTypes() {
        return java.util.Arrays.stream(MaintenanceRecord.MaintenanceType.values())
                .map(Enum::name)
                .collect(Collectors.toList());
    }

    /**
     * Get valid maintenance types for a specific vehicle based on its fuel type
     */
    public List<String> getValidMaintenanceTypesForVehicle(Long vehicleId) {
        Vehicle vehicle = vehicleRepository.findById(vehicleId)
                .orElseThrow(() -> new ResourceNotFoundException("Vehicle", "id", vehicleId));

        com.evfleet.fleet.model.FuelType fuelType = vehicle.getFuelType();
        
        if (fuelType == null) {
            // If fuel type is not set, return all types
            return getAllMaintenanceTypes();
        }

        // Define common types (applicable to all)
        List<MaintenanceRecord.MaintenanceType> commonTypes = List.of(
            MaintenanceRecord.MaintenanceType.ROUTINE_SERVICE,
            MaintenanceRecord.MaintenanceType.TIRE_REPLACEMENT,
            MaintenanceRecord.MaintenanceType.BRAKE_SERVICE,
            MaintenanceRecord.MaintenanceType.EMERGENCY_REPAIR
        );

        // Define ICE-specific types
        List<MaintenanceRecord.MaintenanceType> iceTypes = List.of(
            MaintenanceRecord.MaintenanceType.OIL_CHANGE,
            MaintenanceRecord.MaintenanceType.FILTER_REPLACEMENT,
            MaintenanceRecord.MaintenanceType.EMISSION_TEST,
            MaintenanceRecord.MaintenanceType.COOLANT_FLUSH,
            MaintenanceRecord.MaintenanceType.TRANSMISSION_SERVICE,
            MaintenanceRecord.MaintenanceType.ENGINE_DIAGNOSTICS
        );

        // Define EV-specific types
        List<MaintenanceRecord.MaintenanceType> evTypes = List.of(
            MaintenanceRecord.MaintenanceType.BATTERY_CHECK,
            MaintenanceRecord.MaintenanceType.HV_SYSTEM_CHECK,
            MaintenanceRecord.MaintenanceType.FIRMWARE_UPDATE,
            MaintenanceRecord.MaintenanceType.CHARGING_PORT_INSPECTION,
            MaintenanceRecord.MaintenanceType.THERMAL_MANAGEMENT_CHECK
        );

        List<MaintenanceRecord.MaintenanceType> validTypes = new java.util.ArrayList<>(commonTypes);

        switch (fuelType) {
            case ICE:
                validTypes.addAll(iceTypes);
                break;
            case EV:
                validTypes.addAll(evTypes);
                break;
            case HYBRID:
                validTypes.addAll(iceTypes);
                validTypes.addAll(evTypes);
                validTypes.add(MaintenanceRecord.MaintenanceType.HYBRID_SYSTEM_CHECK);
                break;
        }

        return validTypes.stream()
                .map(Enum::name)
                .collect(Collectors.toList());
    }
}

