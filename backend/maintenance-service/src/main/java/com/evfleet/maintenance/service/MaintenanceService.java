package com.evfleet.maintenance.service;

import com.evfleet.maintenance.dto.MaintenanceRecordResponse;
import com.evfleet.maintenance.entity.BatteryHealth;
import com.evfleet.maintenance.entity.MaintenanceSchedule;
import com.evfleet.maintenance.entity.ServiceHistory;

import java.util.List;
import java.util.Optional;

public interface MaintenanceService {

    // Maintenance Records (Combined view)
    List<MaintenanceRecordResponse> getAllMaintenanceRecords();
    Optional<MaintenanceRecordResponse> getMaintenanceRecordById(String id);
    List<MaintenanceRecordResponse> getMaintenanceRecordsByVehicle(String vehicleId);
    
    // Service History CRUD
    ServiceHistory createServiceHistory(ServiceHistory serviceHistory);
    Optional<ServiceHistory> getServiceHistoryById(String id);
    ServiceHistory updateServiceHistory(String id, ServiceHistory serviceHistory);
    void deleteServiceHistory(String id);
    List<ServiceHistory> getServiceHistoryByVehicle(String vehicleId);
    
    // Maintenance Schedules CRUD
    MaintenanceSchedule createMaintenanceSchedule(MaintenanceSchedule schedule);
    List<MaintenanceSchedule> getAllMaintenanceSchedules();
    Optional<MaintenanceSchedule> getMaintenanceScheduleById(String id);
    List<MaintenanceSchedule> getMaintenanceSchedulesByVehicle(String vehicleId);
    MaintenanceSchedule updateMaintenanceSchedule(String id, MaintenanceSchedule schedule);
    void deleteMaintenanceSchedule(String id);
    
    // Battery Health
    List<BatteryHealth> getBatteryHealthByVehicle(String vehicleId);
    BatteryHealth createBatteryHealth(BatteryHealth batteryHealth);
    
    // Service Reminders
    List<MaintenanceSchedule> getServiceReminders();
}
