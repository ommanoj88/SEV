package com.evfleet.maintenance.service;

import com.evfleet.maintenance.dto.MaintenanceRecordResponse;
import com.evfleet.maintenance.entity.BatteryHealth;
import com.evfleet.maintenance.entity.MaintenanceSchedule;

import java.util.List;

public interface MaintenanceService {

    List<MaintenanceRecordResponse> getAllMaintenanceRecords();

    List<MaintenanceRecordResponse> getMaintenanceRecordsByVehicle(String vehicleId);

    MaintenanceSchedule createMaintenanceSchedule(MaintenanceSchedule schedule);

    List<BatteryHealth> getBatteryHealthByVehicle(String vehicleId);
}
