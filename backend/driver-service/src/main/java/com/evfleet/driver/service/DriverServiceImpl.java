package com.evfleet.driver.service;

import com.evfleet.driver.dto.*;
import com.evfleet.driver.entity.Driver;
import com.evfleet.driver.entity.DriverAssignment;
import com.evfleet.driver.entity.DriverBehavior;
import com.evfleet.driver.enums.AssignmentStatus;
import com.evfleet.driver.enums.DriverStatus;
import com.evfleet.driver.repository.DriverAssignmentRepository;
import com.evfleet.driver.repository.DriverBehaviorRepository;
import com.evfleet.driver.repository.DriverRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DriverServiceImpl implements DriverService {

    private final DriverRepository driverRepository;
    private final DriverBehaviorRepository driverBehaviorRepository;
    private final DriverAssignmentRepository driverAssignmentRepository;

    @Override
    @Transactional
    public DriverResponse createDriver(DriverRequest request) {
        Driver driver = new Driver();
        driver.setId(generateDriverId());
        driver.setCompanyId(request.getCompanyId());
        driver.setName(request.getName());
        driver.setLicenseNumber(request.getLicenseNumber());
        driver.setPhone(request.getPhone());
        driver.setEmail(request.getEmail());
        driver.setStatus(request.getStatus() != null ? request.getStatus() : DriverStatus.ACTIVE);
        driver.setRating(request.getRating() != null ? request.getRating() : BigDecimal.valueOf(4.0));
        driver.setTotalTrips(request.getTotalTrips() != null ? request.getTotalTrips() : 0);
        driver.setJoinedDate(request.getJoinedDate() != null ? request.getJoinedDate() : LocalDate.now());

        Driver savedDriver = driverRepository.save(driver);
        return mapToResponse(savedDriver);
    }

    @Override
    @Transactional
    public DriverResponse updateDriver(String id, DriverRequest request) {
        Driver driver = driverRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Driver not found with id: " + id));

        driver.setCompanyId(request.getCompanyId());
        driver.setName(request.getName());
        driver.setLicenseNumber(request.getLicenseNumber());
        driver.setPhone(request.getPhone());
        driver.setEmail(request.getEmail());
        if (request.getStatus() != null) {
            driver.setStatus(request.getStatus());
        }
        if (request.getRating() != null) {
            driver.setRating(request.getRating());
        }
        if (request.getTotalTrips() != null) {
            driver.setTotalTrips(request.getTotalTrips());
        }
        if (request.getJoinedDate() != null) {
            driver.setJoinedDate(request.getJoinedDate());
        }

        Driver updatedDriver = driverRepository.save(driver);
        return mapToResponse(updatedDriver);
    }

    @Override
    @Transactional(readOnly = true)
    public DriverResponse getDriverById(String id) {
        Driver driver = driverRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Driver not found with id: " + id));
        return mapToResponse(driver);
    }

    @Override
    @Transactional(readOnly = true)
    public List<DriverResponse> getAllDrivers() {
        return driverRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<DriverResponse> getDriversByCompany(String companyId) {
        return driverRepository.findByCompanyId(companyId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void deleteDriver(String id) {
        if (!driverRepository.existsById(id)) {
            throw new RuntimeException("Driver not found with id: " + id);
        }
        driverRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<DriverBehaviorResponse> getDriverBehavior(String driverId) {
        List<DriverBehavior> behaviors = driverBehaviorRepository.findByDriverIdOrderByTimestampDesc(driverId);
        return behaviors.stream()
                .map(this::mapToBehaviorResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public DriverAssignmentResponse assignDriverToVehicle(String driverId, DriverAssignmentRequest request) {
        Driver driver = driverRepository.findById(driverId)
                .orElseThrow(() -> new RuntimeException("Driver not found with id: " + driverId));

        DriverAssignment assignment = new DriverAssignment();
        assignment.setId(generateAssignmentId());
        assignment.setDriverId(driverId);
        assignment.setVehicleId(request.getVehicleId());
        assignment.setShiftStart(request.getShiftStart());
        assignment.setShiftEnd(request.getShiftEnd());
        assignment.setStatus(AssignmentStatus.ACTIVE);
        assignment.setAssignedBy(request.getAssignedBy());

        DriverAssignment savedAssignment = driverAssignmentRepository.save(assignment);
        return mapToAssignmentResponse(savedAssignment);
    }

    @Override
    @Transactional(readOnly = true)
    public List<DriverAssignmentResponse> getDriverAssignments(String driverId) {
        List<DriverAssignment> assignments = driverAssignmentRepository.findByDriverId(driverId);
        return assignments.stream()
                .map(this::mapToAssignmentResponse)
                .collect(Collectors.toList());
    }

    private DriverResponse mapToResponse(Driver driver) {
        DriverResponse response = new DriverResponse();
        response.setId(driver.getId());
        response.setCompanyId(driver.getCompanyId());
        response.setName(driver.getName());
        response.setLicenseNumber(driver.getLicenseNumber());
        response.setPhone(driver.getPhone());
        response.setEmail(driver.getEmail());
        response.setStatus(driver.getStatus());
        response.setRating(driver.getRating());
        response.setTotalTrips(driver.getTotalTrips());
        response.setJoinedDate(driver.getJoinedDate());
        response.setCreatedAt(driver.getCreatedAt());
        response.setUpdatedAt(driver.getUpdatedAt());
        return response;
    }

    private DriverBehaviorResponse mapToBehaviorResponse(DriverBehavior behavior) {
        DriverBehaviorResponse response = new DriverBehaviorResponse();
        response.setId(behavior.getId());
        response.setDriverId(behavior.getDriverId());
        response.setTripId(behavior.getTripId());
        response.setHarshBraking(behavior.getHarshBraking());
        response.setHarshAcceleration(behavior.getHarshAcceleration());
        response.setOverspeeding(behavior.getOverspeeding());
        response.setIdleTime(behavior.getIdleTime());
        response.setScore(behavior.getScore());
        response.setTimestamp(behavior.getTimestamp());
        return response;
    }

    private DriverAssignmentResponse mapToAssignmentResponse(DriverAssignment assignment) {
        DriverAssignmentResponse response = new DriverAssignmentResponse();
        response.setId(assignment.getId());
        response.setDriverId(assignment.getDriverId());
        response.setVehicleId(assignment.getVehicleId());
        response.setShiftStart(assignment.getShiftStart());
        response.setShiftEnd(assignment.getShiftEnd());
        response.setStatus(assignment.getStatus());
        response.setAssignedBy(assignment.getAssignedBy());
        response.setCreatedAt(assignment.getCreatedAt());
        return response;
    }

    private String generateDriverId() {
        return "DRV" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    private String generateAssignmentId() {
        return "ASG" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
}
