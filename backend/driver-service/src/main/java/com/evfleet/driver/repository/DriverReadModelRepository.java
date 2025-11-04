package com.evfleet.driver.repository;

import com.evfleet.driver.entity.DriverReadModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DriverReadModelRepository extends JpaRepository<DriverReadModel, String> {

    List<DriverReadModel> findByOrderByRatingDesc();

    List<DriverReadModel> findByOrderByPerformanceRankAsc();
}
