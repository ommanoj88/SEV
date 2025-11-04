package com.evfleet.charging.repository;

import com.evfleet.charging.entity.ChargingNetwork;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ChargingNetworkRepository extends JpaRepository<ChargingNetwork, Long> {

    Optional<ChargingNetwork> findByName(String name);

    List<ChargingNetwork> findByProvider(String provider);

    List<ChargingNetwork> findByStatus(ChargingNetwork.NetworkStatus status);
}
