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

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
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
    public Optional<MaintenanceRecordResponse> getMaintenanceRecordById(String id) {
        // Try to find as schedule first
        Optional<MaintenanceSchedule> schedule = maintenanceScheduleRepository.findById(id);
        if (schedule.isPresent()) {
            List<ServiceHistory> histories = serviceHistoryRepository.findByVehicleId(schedule.get().getVehicleId());
            return Optional.of(createRecord(schedule.get(), histories.isEmpty() ? null : histories.get(0)));
        }
        
        // Try to find as service history
        Optional<ServiceHistory> history = serviceHistoryRepository.findById(id);
        if (history.isPresent()) {
            List<MaintenanceSchedule> schedules = maintenanceScheduleRepository.findByVehicleId(history.get().getVehicleId());
            return Optional.of(createRecord(schedules.isEmpty() ? createDummySchedule(history.get()) : schedules.get(0), history.get()));
        }
        
        return Optional.empty();
    }

    @Override
    @Transactional(readOnly = true)
    public List<MaintenanceRecordResponse> getMaintenanceRecordsByVehicle(String vehicleId) {
        List<MaintenanceSchedule> schedules = maintenanceScheduleRepository.findByVehicleId(vehicleId);
        List<ServiceHistory> histories = serviceHistoryRepository.findByVehicleId(vehicleId);

        return combineRecords(schedules, histories);
    }
    
    // ========== SERVICE HISTORY CRUD ==========
    
    @Override
    @Transactional
    public ServiceHistory createServiceHistory(ServiceHistory serviceHistory) {
        if (serviceHistory.getId() == null || serviceHistory.getId().isEmpty()) {
            serviceHistory.setId(UUID.randomUUID().toString());
        }
        return serviceHistoryRepository.save(serviceHistory);
    }
    
    @Override
    @Transactional(readOnly = true)
    public Optional<ServiceHistory> getServiceHistoryById(String id) {
        return serviceHistoryRepository.findById(id);
    }
    
    @Override
    @Transactional
    public ServiceHistory updateServiceHistory(String id, ServiceHistory serviceHistory) {
        Optional<ServiceHistory> existing = serviceHistoryRepository.findById(id);
        if (existing.isEmpty()) {
            throw new RuntimeException("Service history not found with id: " + id);
        }
        serviceHistory.setId(id);
        return serviceHistoryRepository.save(serviceHistory);
    }
    
    @Override
    @Transactional
    public void deleteServiceHistory(String id) {
        serviceHistoryRepository.deleteById(id);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<ServiceHistory> getServiceHistoryByVehicle(String vehicleId) {
        return serviceHistoryRepository.findByVehicleIdOrderByServiceDateDesc(vehicleId);
    }
    
    // ========== MAINTENANCE SCHEDULES CRUD ==========

    @Override
    @Transactional
    public MaintenanceSchedule createMaintenanceSchedule(MaintenanceSchedule schedule) {
        if (schedule.getId() == null || schedule.getId().isEmpty()) {
            schedule.setId(UUID.randomUUID().toString());
        }
        return maintenanceScheduleRepository.save(schedule);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<MaintenanceSchedule> getAllMaintenanceSchedules() {
        return maintenanceScheduleRepository.findAll();
    }
    
    @Override
    @Transactional(readOnly = true)
    public Optional<MaintenanceSchedule> getMaintenanceScheduleById(String id) {
        return maintenanceScheduleRepository.findById(id);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<MaintenanceSchedule> getMaintenanceSchedulesByVehicle(String vehicleId) {
        return maintenanceScheduleRepository.findByVehicleId(vehicleId);
    }
    
    @Override
    @Transactional
    public MaintenanceSchedule updateMaintenanceSchedule(String id, MaintenanceSchedule schedule) {
        Optional<MaintenanceSchedule> existing = maintenanceScheduleRepository.findById(id);
        if (existing.isEmpty()) {
            throw new RuntimeException("Maintenance schedule not found with id: " + id);
        }
        schedule.setId(id);
        return maintenanceScheduleRepository.save(schedule);
    }
    
    @Override
    @Transactional
    public void deleteMaintenanceSchedule(String id) {
        maintenanceScheduleRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<BatteryHealth> getBatteryHealthByVehicle(String vehicleId) {
        return batteryHealthRepository.findByVehicleIdOrderByTimestampDesc(vehicleId);
    }
    
    @Override
    @Transactional
    public BatteryHealth createBatteryHealth(BatteryHealth batteryHealth) {
        if (batteryHealth.getId() == null || batteryHealth.getId().isEmpty()) {
            batteryHealth.setId(UUID.randomUUID().toString());
        }
        return batteryHealthRepository.save(batteryHealth);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<MaintenanceSchedule> getServiceReminders() {
        // Get all schedules that are due within the next 30 days or overdue
        LocalDate thirtyDaysFromNow = LocalDate.now().plusDays(30);
        return maintenanceScheduleRepository.findByDueDateBefore(thirtyDaysFromNow);
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
    
    private MaintenanceSchedule createDummySchedule(ServiceHistory history) {
        MaintenanceSchedule schedule = new MaintenanceSchedule();
        schedule.setId(history.getId());
        schedule.setVehicleId(history.getVehicleId());
        schedule.setServiceType(history.getServiceType());
        schedule.setStatus("COMPLETED");
        schedule.setPriority("MEDIUM");
        schedule.setDescription(history.getDescription());
        return schedule;
    }
}
