package com.evfleet.maintenance.service;

import com.evfleet.maintenance.entity.MaintenanceSchedule;
import com.evfleet.maintenance.model.MaintenanceType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * MaintenanceScheduleBuilder Service
 * Generates appropriate maintenance schedules for vehicles based on their fuel type.
 * 
 * This service creates maintenance schedules tailored to:
 * - ICE vehicles: Oil changes, filter replacements, engine maintenance
 * - EV vehicles: Battery checks, motor inspection, charging port maintenance
 * - HYBRID vehicles: Both ICE and EV maintenance combined
 * 
 * @since 2.0.0 (Multi-fuel support - PR 11)
 */
@Service
@Slf4j
public class MaintenanceScheduleBuilder {
    
    /**
     * Generate all applicable maintenance schedules for a vehicle based on its fuel type
     * 
     * @param vehicleId The vehicle ID
     * @param fuelType The fuel type (ICE, EV, HYBRID)
     * @param currentMileage Current vehicle mileage in kilometers
     * @return List of maintenance schedules
     */
    public List<MaintenanceSchedule> generateSchedulesForVehicle(
            String vehicleId, 
            String fuelType, 
            Integer currentMileage) {
        
        if (vehicleId == null || fuelType == null) {
            log.error("Vehicle ID and fuel type are required to generate maintenance schedules");
            throw new IllegalArgumentException("Vehicle ID and fuel type are required");
        }
        
        log.info("Generating maintenance schedules for vehicle {} with fuel type {}", vehicleId, fuelType);
        
        List<MaintenanceSchedule> schedules = new ArrayList<>();
        int mileage = (currentMileage != null) ? currentMileage : 0;
        
        // Generate schedules for all applicable maintenance types
        for (MaintenanceType type : MaintenanceType.values()) {
            if (type.isApplicableToFuelType(fuelType)) {
                MaintenanceSchedule schedule = createScheduleForType(vehicleId, type, mileage);
                schedules.add(schedule);
            }
        }
        
        log.info("Generated {} maintenance schedules for vehicle {}", schedules.size(), vehicleId);
        return schedules;
    }
    
    /**
     * Generate schedules only for ICE-specific maintenance
     * 
     * @param vehicleId The vehicle ID
     * @param currentMileage Current vehicle mileage in kilometers
     * @return List of ICE-specific maintenance schedules
     */
    public List<MaintenanceSchedule> generateICESchedules(String vehicleId, Integer currentMileage) {
        log.info("Generating ICE-specific maintenance schedules for vehicle {}", vehicleId);
        
        List<MaintenanceSchedule> schedules = new ArrayList<>();
        int mileage = (currentMileage != null) ? currentMileage : 0;
        
        for (MaintenanceType type : MaintenanceType.values()) {
            if ("ICE".equals(type.getCategory())) {
                MaintenanceSchedule schedule = createScheduleForType(vehicleId, type, mileage);
                schedules.add(schedule);
            }
        }
        
        return schedules;
    }
    
    /**
     * Generate schedules only for EV-specific maintenance
     * 
     * @param vehicleId The vehicle ID
     * @param currentMileage Current vehicle mileage in kilometers
     * @return List of EV-specific maintenance schedules
     */
    public List<MaintenanceSchedule> generateEVSchedules(String vehicleId, Integer currentMileage) {
        log.info("Generating EV-specific maintenance schedules for vehicle {}", vehicleId);
        
        List<MaintenanceSchedule> schedules = new ArrayList<>();
        int mileage = (currentMileage != null) ? currentMileage : 0;
        
        for (MaintenanceType type : MaintenanceType.values()) {
            if ("EV".equals(type.getCategory())) {
                MaintenanceSchedule schedule = createScheduleForType(vehicleId, type, mileage);
                schedules.add(schedule);
            }
        }
        
        return schedules;
    }
    
    /**
     * Generate schedules only for common maintenance (applicable to all vehicles)
     * 
     * @param vehicleId The vehicle ID
     * @param currentMileage Current vehicle mileage in kilometers
     * @return List of common maintenance schedules
     */
    public List<MaintenanceSchedule> generateCommonSchedules(String vehicleId, Integer currentMileage) {
        log.info("Generating common maintenance schedules for vehicle {}", vehicleId);
        
        List<MaintenanceSchedule> schedules = new ArrayList<>();
        int mileage = (currentMileage != null) ? currentMileage : 0;
        
        for (MaintenanceType type : MaintenanceType.values()) {
            if ("COMMON".equals(type.getCategory())) {
                MaintenanceSchedule schedule = createScheduleForType(vehicleId, type, mileage);
                schedules.add(schedule);
            }
        }
        
        return schedules;
    }
    
    /**
     * Create a single maintenance schedule for a specific maintenance type
     * 
     * @param vehicleId The vehicle ID
     * @param type The maintenance type
     * @param currentMileage Current vehicle mileage
     * @return A maintenance schedule
     */
    private MaintenanceSchedule createScheduleForType(
            String vehicleId, 
            MaintenanceType type, 
            int currentMileage) {
        
        MaintenanceSchedule schedule = new MaintenanceSchedule();
        schedule.setId(UUID.randomUUID().toString());
        schedule.setVehicleId(vehicleId);
        schedule.setServiceType(type.name());
        
        // Calculate due mileage
        int mileageInterval = type.getDefaultMileageInterval();
        if (mileageInterval > 0) {
            // Calculate next due mileage based on interval
            int nextDueMileage = ((currentMileage / mileageInterval) + 1) * mileageInterval;
            schedule.setDueMileage(nextDueMileage);
        }
        
        // Calculate due date
        int monthsInterval = type.getDefaultTimeInterval();
        if (monthsInterval > 0) {
            schedule.setDueDate(LocalDate.now().plusMonths(monthsInterval));
        }
        
        // Set status and priority based on maintenance type
        schedule.setStatus("SCHEDULED");
        schedule.setPriority(determinePriority(type));
        
        // Set description
        schedule.setDescription(type.getDisplayName() + " - Recommended every " + 
                (mileageInterval > 0 ? mileageInterval + " km" : "") +
                (mileageInterval > 0 && monthsInterval > 0 ? " or " : "") +
                (monthsInterval > 0 ? monthsInterval + " months" : ""));
        
        schedule.setCreatedAt(LocalDateTime.now());
        schedule.setUpdatedAt(LocalDateTime.now());
        
        return schedule;
    }
    
    /**
     * Determine priority for a maintenance type
     * 
     * @param type The maintenance type
     * @return Priority level (HIGH, MEDIUM, LOW)
     */
    private String determinePriority(MaintenanceType type) {
        // High priority for critical safety and reliability items
        switch (type) {
            case OIL_CHANGE:
            case OIL_FILTER:
            case BRAKE_INSPECTION:
            case BRAKE_PADS:
            case BRAKE_FLUID:
            case BATTERY_CHECK:
            case HIGH_VOLTAGE_CHECK:
            case SAFETY_INSPECTION:
            case TIRE_REPLACEMENT:
                return "HIGH";
            
            // Medium priority for regular maintenance
            case AIR_FILTER:
            case FUEL_FILTER:
            case BATTERY_MAINTENANCE:
            case MOTOR_INSPECTION:
            case TIRE_ROTATION:
            case SUSPENSION_CHECK:
            case WHEEL_ALIGNMENT:
            case ENGINE_TUNE_UP:
            case COOLANT_FLUSH:
            case TRANSMISSION_FLUID:
                return "MEDIUM";
            
            // Low priority for less critical items
            default:
                return "LOW";
        }
    }
    
    /**
     * Get the recommended maintenance schedule for a specific maintenance type and vehicle
     * 
     * @param vehicleId The vehicle ID
     * @param maintenanceType The maintenance type
     * @param currentMileage Current vehicle mileage
     * @return A maintenance schedule for the specified type
     */
    public MaintenanceSchedule getScheduleForMaintenanceType(
            String vehicleId, 
            MaintenanceType maintenanceType, 
            Integer currentMileage) {
        
        log.info("Creating schedule for maintenance type {} for vehicle {}", maintenanceType, vehicleId);
        int mileage = (currentMileage != null) ? currentMileage : 0;
        return createScheduleForType(vehicleId, maintenanceType, mileage);
    }
    
    /**
     * Check if a maintenance type is applicable to a fuel type
     * 
     * @param maintenanceType The maintenance type to check
     * @param fuelType The fuel type (ICE, EV, HYBRID)
     * @return true if applicable, false otherwise
     */
    public boolean isMaintenanceApplicable(MaintenanceType maintenanceType, String fuelType) {
        return maintenanceType.isApplicableToFuelType(fuelType);
    }
    
    /**
     * Get all maintenance types applicable to a fuel type
     * 
     * @param fuelType The fuel type (ICE, EV, HYBRID)
     * @return List of applicable maintenance types
     */
    public List<MaintenanceType> getApplicableMaintenanceTypes(String fuelType) {
        List<MaintenanceType> applicableTypes = new ArrayList<>();
        
        for (MaintenanceType type : MaintenanceType.values()) {
            if (type.isApplicableToFuelType(fuelType)) {
                applicableTypes.add(type);
            }
        }
        
        log.info("Found {} applicable maintenance types for fuel type {}", applicableTypes.size(), fuelType);
        return applicableTypes;
    }
}
