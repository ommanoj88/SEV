package com.evfleet.driver.repository;

import com.evfleet.driver.model.Driver;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DriverRepository extends JpaRepository<Driver, Long> {
    List<Driver> findByCompanyId(Long companyId);
    List<Driver> findByStatus(Driver.DriverStatus status);
    List<Driver> findByCompanyIdAndStatus(Long companyId, Driver.DriverStatus status);
    List<Driver> findByCompanyIdAndStatusAndCurrentVehicleIdIsNull(Long companyId, Driver.DriverStatus status);
    List<Driver> findByCompanyIdAndLicenseExpiryBefore(Long companyId, java.time.LocalDate date);
    Optional<Driver> findByLicenseNumber(String licenseNumber);
    Optional<Driver> findByPhone(String phone);
}
