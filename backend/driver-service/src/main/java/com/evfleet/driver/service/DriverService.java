package com.evfleet.driver.service;

import com.evfleet.driver.dto.*;
import com.evfleet.driver.entity.Driver;

import java.util.List;

public interface DriverService {

    DriverResponse createDriver(DriverRequest request);

    DriverResponse updateDriver(String id, DriverRequest request);

    DriverResponse getDriverById(String id);

    List<DriverResponse> getAllDrivers();

    List<DriverResponse> getDriversByCompany(String companyId);

    void deleteDriver(String id);

    List<DriverBehaviorResponse> getDriverBehavior(String driverId);

    DriverAssignmentResponse assignDriverToVehicle(String driverId, DriverAssignmentRequest request);

    List<DriverAssignmentResponse> getDriverAssignments(String driverId);
}
