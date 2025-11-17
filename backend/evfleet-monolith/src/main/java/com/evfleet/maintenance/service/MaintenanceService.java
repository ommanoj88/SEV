package com.evfleet.maintenance.service;

import com.evfleet.common.exception.ResourceNotFoundException;
import com.evfleet.maintenance.dto.MaintenanceRecordRequest;
import com.evfleet.maintenance.dto.MaintenanceRecordResponse;
import com.evfleet.maintenance.model.MaintenanceRecord;
import com.evfleet.maintenance.repository.MaintenanceRecordRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
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
}
