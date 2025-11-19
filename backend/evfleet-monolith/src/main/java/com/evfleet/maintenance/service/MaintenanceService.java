package com.evfleet.maintenance.service;

import com.evfleet.common.exception.ResourceNotFoundException;
import com.evfleet.fleet.model.Vehicle;
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
    private final MaintenancePolicyRepository maintenancePolicyRepository;
    private final MaintenanceLineItemRepository maintenanceLineItemRepository;

    public MaintenanceRecordResponse createMaintenanceRecord(Long companyId, MaintenanceRecordRequest request) {
        log.info("POST /api/v1/maintenance/records - Creating maintenance record for vehicle: {}", request.getVehicleId());

        // Convert attachment URLs list to comma-separated string
        String attachmentUrlsString = null;
        if (request.getAttachmentUrls() != null && !request.getAttachmentUrls().isEmpty()) {
            attachmentUrlsString = String.join(",", request.getAttachmentUrls());
        }

        MaintenanceRecord record = MaintenanceRecord.builder()
                .vehicleId(request.getVehicleId())
                .companyId(companyId)
                .type(MaintenanceRecord.MaintenanceType.valueOf(request.getType().toUpperCase()))
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
}

