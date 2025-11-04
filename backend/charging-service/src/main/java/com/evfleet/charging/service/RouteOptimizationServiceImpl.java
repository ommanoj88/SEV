package com.evfleet.charging.service;

import com.evfleet.charging.dto.RouteOptimizationRequest;
import com.evfleet.charging.dto.RouteOptimizationResponse;
import com.evfleet.charging.entity.ChargingStation;
import com.evfleet.charging.entity.RouteOptimization;
import com.evfleet.charging.exception.ResourceNotFoundException;
import com.evfleet.charging.repository.ChargingStationRepository;
import com.evfleet.charging.repository.RouteOptimizationRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class RouteOptimizationServiceImpl implements RouteOptimizationService {

    private final RouteOptimizationRepository routeRepository;
    private final ChargingStationRepository stationRepository;
    private final ObjectMapper objectMapper;

    @Override
    @Transactional
    public RouteOptimizationResponse optimizeRoute(RouteOptimizationRequest request) {
        log.info("Optimizing route for vehicle: {} from {} to {}",
                 request.getVehicleId(), request.getOrigin(), request.getDestination());

        // Simplified route optimization logic
        // In production, this would integrate with mapping APIs like Google Maps

        RouteOptimization route = new RouteOptimization();
        route.setVehicleId(request.getVehicleId());
        route.setOrigin(request.getOrigin());
        route.setOriginLat(request.getOriginLat());
        route.setOriginLng(request.getOriginLng());
        route.setDestination(request.getDestination());
        route.setDestinationLat(request.getDestinationLat());
        route.setDestinationLng(request.getDestinationLng());
        route.setCurrentBatteryLevel(request.getCurrentBatteryLevel());

        // Calculate estimated distance (simplified - normally would use mapping API)
        BigDecimal distance = calculateDistance(
            request.getOriginLat(), request.getOriginLng(),
            request.getDestinationLat(), request.getDestinationLng()
        );
        route.setTotalDistance(distance);

        // Calculate energy required
        BigDecimal avgConsumption = request.getAverageConsumption() != null ?
            request.getAverageConsumption() : new BigDecimal("0.15"); // Default 0.15 kWh/km
        BigDecimal energyRequired = distance.multiply(avgConsumption)
            .setScale(2, RoundingMode.HALF_UP);
        route.setEstimatedEnergyRequired(energyRequired);

        // Determine if charging stops are needed
        BigDecimal batteryCapacity = request.getBatteryCapacity() != null ?
            request.getBatteryCapacity() : new BigDecimal("60"); // Default 60 kWh
        BigDecimal currentEnergy = batteryCapacity.multiply(request.getCurrentBatteryLevel())
            .divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);

        List<Long> chargingStops = new ArrayList<>();
        BigDecimal totalCost = BigDecimal.ZERO;
        int numberOfStops = 0;

        if (energyRequired.compareTo(currentEnergy) > 0) {
            // Need charging stops
            log.info("Route requires charging stops");

            // Find stations along the route (simplified - would use route waypoints in production)
            List<ChargingStation> nearbyStations = findStationsAlongRoute(
                request.getOriginLat(), request.getOriginLng(),
                request.getDestinationLat(), request.getDestinationLng()
            );

            // Select optimal charging stops (simplified algorithm)
            BigDecimal energyDeficit = energyRequired.subtract(currentEnergy);
            for (ChargingStation station : nearbyStations) {
                if (energyDeficit.compareTo(BigDecimal.ZERO) <= 0) break;

                chargingStops.add(station.getId());
                BigDecimal chargeAmount = energyDeficit.min(new BigDecimal("30")); // Max 30 kWh per stop
                totalCost = totalCost.add(chargeAmount.multiply(station.getPricePerKwh()));
                energyDeficit = energyDeficit.subtract(chargeAmount);
                numberOfStops++;

                if (numberOfStops >= 3) break; // Max 3 stops
            }
        }

        try {
            route.setChargingStops(objectMapper.writeValueAsString(chargingStops));
        } catch (JsonProcessingException e) {
            log.error("Error serializing charging stops", e);
            route.setChargingStops("[]");
        }

        route.setNumberOfStops(numberOfStops);
        route.setEstimatedCost(totalCost);

        // Calculate estimated time (simplified)
        long estimatedMinutes = distance.multiply(new BigDecimal("60"))
            .divide(new BigDecimal("80"), 0, RoundingMode.HALF_UP)
            .longValue(); // Assume 80 km/h average
        estimatedMinutes += numberOfStops * 30; // 30 min per charging stop
        route.setEstimatedTimeMinutes(estimatedMinutes);

        // Calculate final battery level
        BigDecimal finalBatteryLevel = request.getCurrentBatteryLevel()
            .add(new BigDecimal(numberOfStops * 50)) // Assume 50% charge per stop
            .subtract(energyRequired.multiply(new BigDecimal("100"))
                .divide(batteryCapacity, 2, RoundingMode.HALF_UP));
        route.setEstimatedFinalBatteryLevel(finalBatteryLevel.max(BigDecimal.ZERO));

        RouteOptimization savedRoute = routeRepository.save(route);
        log.info("Route optimized successfully with {} charging stops", numberOfStops);

        return RouteOptimizationResponse.from(savedRoute);
    }

    @Override
    @Transactional(readOnly = true)
    public RouteOptimizationResponse getRouteById(Long id) {
        log.info("Fetching route optimization with ID: {}", id);

        RouteOptimization route = routeRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Route not found with ID: " + id));

        return RouteOptimizationResponse.from(route);
    }

    @Override
    @Transactional(readOnly = true)
    public List<RouteOptimizationResponse> getRoutesByVehicle(Long vehicleId) {
        log.info("Fetching routes for vehicle: {}", vehicleId);

        return routeRepository.findByVehicleId(vehicleId).stream()
            .map(RouteOptimizationResponse::from)
            .collect(Collectors.toList());
    }

    private BigDecimal calculateDistance(Double lat1, Double lon1, Double lat2, Double lon2) {
        // Haversine formula
        final int R = 6371; // Radius of the earth in km

        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        double distance = R * c;

        return new BigDecimal(distance).setScale(2, RoundingMode.HALF_UP);
    }

    private List<ChargingStation> findStationsAlongRoute(Double originLat, Double originLon,
                                                          Double destLat, Double destLon) {
        // Simplified: find stations in a bounding box between origin and destination
        Double minLat = Math.min(originLat, destLat) - 0.5;
        Double maxLat = Math.max(originLat, destLat) + 0.5;
        Double minLon = Math.min(originLon, destLon) - 0.5;
        Double maxLon = Math.max(originLon, destLon) + 0.5;

        return stationRepository.findStationsInBounds(minLat, maxLat, minLon, maxLon);
    }
}
