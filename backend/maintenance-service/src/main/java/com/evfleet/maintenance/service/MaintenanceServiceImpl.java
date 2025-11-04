package com.evfleet.maintenance.service;

import com.evfleet.maintenance.dto.MaintenanceRecordResponse;
import com.evfleet.maintenance.entity.BatteryHealth;
import com.evfleet.maintenance.entity.MaintenanceSchedule;
import com.evfleet.maintenance.entity.ServiceHistory;
import com.evfleet.maintenance.repository.BatteryHealthRepository;
import com.evfleet.maintenance.repository.MaintenanceScheduleRepository;
import com.evfleet.maintenance.repository.ServiceHistoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MaintenanceServiceImpl implements MaintenanceService {

    private final MaintenanceScheduleRepository maintenanceScheduleRepository;
    private final ServiceHistoryRepository serviceHistoryRepository;
    private final BatteryHealthRepository batteryHealthRepository;

    @Override
    @Transactional(readOnly = true)
    public List<MaintenanceRecordResponse> getAllMaintenanceRecords() {
        List<MaintenanceSchedule> schedules = maintenanceScheduleRepository.findAll();
        List<ServiceHistory> histories = serviceHistoryRepository.findAll();

        return combineRecords(schedules, histories);
    }

    @Override
    @Transactional(readOnly = true)
    public List<MaintenanceRecordResponse> getMaintenanceRecordsByVehicle(String vehicleId) {
        List<MaintenanceSchedule> schedules = maintenanceScheduleRepository.findByVehicleId(vehicleId);
        List<ServiceHistory> histories = serviceHistoryRepository.findByVehicleId(vehicleId);

        return combineRecords(schedules, histories);
    }

    @Override
    @Transactional
    public MaintenanceSchedule createMaintenanceSchedule(MaintenanceSchedule schedule) {
        return maintenanceScheduleRepository.save(schedule);
    }

    @Override
    @Transactional(readOnly = true)
    public List<BatteryHealth> getBatteryHealthByVehicle(String vehicleId) {
        return batteryHealthRepository.findByVehicleIdOrderByTimestampDesc(vehicleId);
    }

    private List<MaintenanceRecordResponse> combineRecords(
            List<MaintenanceSchedule> schedules,
            List<ServiceHistory> histories) {

        Map<String, List<ServiceHistory>> historyByVehicle = histories.stream()
                .collect(Collectors.groupingBy(ServiceHistory::getVehicleId));

        List<MaintenanceRecordResponse> records = new ArrayList<>();

        for (MaintenanceSchedule schedule : schedules) {
            List<ServiceHistory> vehicleHistories = historyByVehicle.get(schedule.getVehicleId());

            if (vehicleHistories != null && !vehicleHistories.isEmpty()) {
                for (ServiceHistory history : vehicleHistories) {
                    records.add(createRecord(schedule, history));
                }
            } else {
                records.add(createRecord(schedule, null));
            }
        }

        return records;
    }

    private MaintenanceRecordResponse createRecord(MaintenanceSchedule schedule, ServiceHistory history) {
        MaintenanceRecordResponse record = new MaintenanceRecordResponse();
        record.setVehicleId(schedule.getVehicleId());

        MaintenanceRecordResponse.ScheduleInfo scheduleInfo = new MaintenanceRecordResponse.ScheduleInfo();
        scheduleInfo.setId(schedule.getId());
        scheduleInfo.setServiceType(schedule.getServiceType());
        scheduleInfo.setDueDate(schedule.getDueDate());
        scheduleInfo.setDueMileage(schedule.getDueMileage());
        scheduleInfo.setStatus(schedule.getStatus());
        scheduleInfo.setPriority(schedule.getPriority());
        scheduleInfo.setDescription(schedule.getDescription());
        scheduleInfo.setCreatedAt(schedule.getCreatedAt());
        scheduleInfo.setUpdatedAt(schedule.getUpdatedAt());
        record.setSchedule(scheduleInfo);

        if (history != null) {
            MaintenanceRecordResponse.HistoryInfo historyInfo = new MaintenanceRecordResponse.HistoryInfo();
            historyInfo.setId(history.getId());
            historyInfo.setServiceDate(history.getServiceDate());
            historyInfo.setServiceType(history.getServiceType());
            historyInfo.setCost(history.getCost());
            historyInfo.setServiceCenter(history.getServiceCenter());
            historyInfo.setDescription(history.getDescription());
            historyInfo.setPartsReplaced(history.getPartsReplaced());
            historyInfo.setTechnician(history.getTechnician());
            historyInfo.setCreatedAt(history.getCreatedAt());
            record.setHistory(historyInfo);
        }

        return record;
    }
}
