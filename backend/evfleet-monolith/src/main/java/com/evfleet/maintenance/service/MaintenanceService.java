package com.evfleet.maintenance.service;

import com.evfleet.common.exception.ResourceNotFoundException;
import com.evfleet.fleet.model.Vehicle;
import com.evfleet.fleet.repository.VehicleRepository;
import com.evfleet.maintenance.dto.MaintenanceAlertResponse;
import com.evfleet.maintenance.dto.MaintenanceRecordRequest;
import com.evfleet.maintenance.dto.MaintenanceRecordResponse;
import com.evfleet.maintenance.model.MaintenanceRecord;
import com.evfleet.maintenance.repository.MaintenanceRecordRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
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

    public MaintenanceRecordResponse createMaintenanceRecord(Long companyId, MaintenanceRecordRequest request) {
        log.info("POST /api/v1/maintenance/records - Creating maintenance record for vehicle: {}", request.getVehicleId());

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
                .build();

        MaintenanceRecord saved = maintenanceRecordRepository.save(record);
        log.info("Maintenance record created successfully: {}", saved.getId());
        return MaintenanceRecordResponse.fromEntity(saved);
    }

    @Transactional(readOnly = true)
    public List<MaintenanceRecordResponse> getAllMaintenanceRecords(Long companyId) {
        log.info("GET /api/v1/maintenance/records - Fetching all maintenance records for company: {}", companyId);
        List<MaintenanceRecord> records = maintenanceRecordRepository.findByCompanyId(companyId);
        return records.stream()
                .map(MaintenanceRecordResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public MaintenanceRecordResponse getMaintenanceRecordById(Long id) {
        log.info("GET /api/v1/maintenance/records/{} - Fetching maintenance record", id);
        MaintenanceRecord record = maintenanceRecordRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("MaintenanceRecord", "id", id));
        return MaintenanceRecordResponse.fromEntity(record);
    }

    @Transactional(readOnly = true)
    public List<MaintenanceRecordResponse> getMaintenanceRecordsByVehicle(Long vehicleId) {
        log.info("GET /api/v1/maintenance/records/vehicle/{} - Fetching maintenance records", vehicleId);
        List<MaintenanceRecord> records = maintenanceRecordRepository.findByVehicleId(vehicleId);
        return records.stream()
                .map(MaintenanceRecordResponse::fromEntity)
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
    }
}
