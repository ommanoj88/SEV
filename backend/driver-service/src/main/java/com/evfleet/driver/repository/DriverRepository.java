package com.evfleet.driver.repository;

import com.evfleet.driver.entity.Driver;
import com.evfleet.driver.enums.DriverStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DriverRepository extends JpaRepository<Driver, String> {

    List<Driver> findByCompanyId(String companyId);

    List<Driver> findByStatus(DriverStatus status);

    Optional<Driver> findByLicenseNumber(String licenseNumber);

    Optional<Driver> findByEmail(String email);

    List<Driver> findByCompanyIdAndStatus(String companyId, DriverStatus status);
}
