package com.evfleet.fleet.service;

import com.evfleet.fleet.dto.TripCostAnalysisDTO;
import com.evfleet.fleet.exception.ResourceNotFoundException;
import com.evfleet.fleet.model.FuelType;
import com.evfleet.fleet.model.Trip;
import com.evfleet.fleet.model.Vehicle;
import com.evfleet.fleet.repository.TripRepository;
import com.evfleet.fleet.repository.VehicleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Multi-Fleet Analytics Service
 * Provides cost analysis and trip analytics for all fuel types (EV, ICE, HYBRID)
 * 
 * This service intelligently routes cost calculations based on vehicle fuel type:
 * - EV vehicles: Use EVCostCalculator for energy costs
 * - ICE vehicles: Use ICECostCalculator for fuel costs
 * - HYBRID vehicles: Use both calculators and combine results
 * 
 * @since 2.0.0 (Multi-fuel support - PR 7)
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class MultiFleetAnalyticsService {

    private final TripRepository tripRepository;
    private final VehicleRepository vehicleRepository;
    private final EVCostCalculator evCostCalculator;
    private final ICECostCalculator iceCostCalculator;

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ISO_DATE_TIME;

    /**
     * Get trip cost analysis for a specific trip
     * Automatically selects appropriate cost calculator based on vehicle fuel type
     * 
     * @param tripId The trip ID
     * @return Trip cost analysis DTO
     * @throws ResourceNotFoundException if trip or vehicle not found
     */
    public TripCostAnalysisDTO getTripCostAnalysis(Long tripId) {
        log.info("Calculating cost analysis for trip ID: {}", tripId);

        Trip trip = tripRepository.findById(tripId)
                .orElseThrow(() -> new ResourceNotFoundException("Trip not found with ID: " + tripId));

        Vehicle vehicle = vehicleRepository.findById(trip.getVehicleId())
                .orElseThrow(() -> new ResourceNotFoundException("Vehicle not found with ID: " + trip.getVehicleId()));

        return buildTripCostAnalysis(trip, vehicle);
    }

    /**
     * Get trip cost analysis for multiple trips by vehicle
     * 
     * @param vehicleId The vehicle ID
     * @param startTime Optional start time filter
     * @param endTime Optional end time filter
     * @return List of trip cost analyses
     */
    public List<TripCostAnalysisDTO> getTripCostAnalysisByVehicle(Long vehicleId, 
                                                                   LocalDateTime startTime, 
                                                                   LocalDateTime endTime) {
        log.info("Calculating cost analysis for vehicle ID: {} (period: {} to {})", vehicleId, startTime, endTime);

        Vehicle vehicle = vehicleRepository.findById(vehicleId)
                .orElseThrow(() -> new ResourceNotFoundException("Vehicle not found with ID: " + vehicleId));

        List<Trip> trips;
        if (startTime != null && endTime != null) {
            trips = tripRepository.findTripsByVehicleAndTimeRange(vehicleId, startTime, endTime);
        } else {
            trips = tripRepository.findByVehicleId(vehicleId, null);
        }

        return trips.stream()
                .map(trip -> buildTripCostAnalysis(trip, vehicle))
                .collect(Collectors.toList());
    }

    /**
     * Get aggregated cost summary by company
     * 
     * @param companyId The company ID
     * @param startTime Start time for analysis period
     * @param endTime End time for analysis period
     * @return Map with aggregated cost metrics
     */
    public Map<String, Object> getCompanyCostSummary(Long companyId, 
                                                     LocalDateTime startTime, 
                                                     LocalDateTime endTime) {
        log.info("Calculating company cost summary for company ID: {} (period: {} to {})", 
                 companyId, startTime, endTime);

        List<Trip> trips = tripRepository.findTripsByCompanyAndTimeRange(companyId, startTime, endTime);
        
        if (trips.isEmpty()) {
            log.warn("No trips found for company {} in the specified period", companyId);
            return Collections.emptyMap();
        }

        // Get all vehicles involved in these trips
        Set<Long> vehicleIds = trips.stream()
                .map(Trip::getVehicleId)
                .collect(Collectors.toSet());
        
        List<Vehicle> vehicles = vehicleRepository.findAllById(vehicleIds);
        Map<Long, Vehicle> vehicleMap = vehicles.stream()
                .collect(Collectors.toMap(Vehicle::getId, v -> v));

        // Calculate costs by fuel type
        double totalEVCost = 0.0;
        double totalICECost = 0.0;
        double totalHybridCost = 0.0;
        double totalCarbonFootprint = 0.0;
        double totalDistance = 0.0;

        int evTripCount = 0;
        int iceTripCount = 0;
        int hybridTripCount = 0;

        for (Trip trip : trips) {
            Vehicle vehicle = vehicleMap.get(trip.getVehicleId());
            if (vehicle == null) continue;

            if (trip.getDistance() != null) {
                totalDistance += trip.getDistance();
            }

            switch (vehicle.getFuelType()) {
                case EV:
                    totalEVCost += evCostCalculator.calculateEnergyCost(trip, vehicle);
                    totalCarbonFootprint += evCostCalculator.calculateCarbonFootprint(trip);
                    evTripCount++;
                    break;
                case ICE:
                    totalICECost += iceCostCalculator.calculateFuelCost(trip, vehicle);
                    totalCarbonFootprint += iceCostCalculator.calculateCarbonFootprint(trip, vehicle);
                    iceTripCount++;
                    break;
                case HYBRID:
                    // For hybrid, calculate both (in reality, would need to determine split)
                    double hybridEnergyCost = evCostCalculator.calculateEnergyCost(trip, vehicle);
                    double hybridFuelCost = iceCostCalculator.calculateFuelCost(trip, vehicle);
                    totalHybridCost += hybridEnergyCost + hybridFuelCost;
                    totalCarbonFootprint += evCostCalculator.calculateCarbonFootprint(trip);
                    totalCarbonFootprint += iceCostCalculator.calculateCarbonFootprint(trip, vehicle);
                    hybridTripCount++;
                    break;
            }
        }

        double totalCost = totalEVCost + totalICECost + totalHybridCost;
        double avgCostPerKm = totalDistance > 0 ? totalCost / totalDistance : 0.0;

        Map<String, Object> summary = new HashMap<>();
        summary.put("companyId", companyId);
        summary.put("periodStart", startTime.format(DATE_TIME_FORMATTER));
        summary.put("periodEnd", endTime.format(DATE_TIME_FORMATTER));
        summary.put("totalTrips", trips.size());
        summary.put("evTripCount", evTripCount);
        summary.put("iceTripCount", iceTripCount);
        summary.put("hybridTripCount", hybridTripCount);
        summary.put("totalDistance", Math.round(totalDistance * 100.0) / 100.0);
        summary.put("totalCost", Math.round(totalCost * 100.0) / 100.0);
        summary.put("totalEVCost", Math.round(totalEVCost * 100.0) / 100.0);
        summary.put("totalICECost", Math.round(totalICECost * 100.0) / 100.0);
        summary.put("totalHybridCost", Math.round(totalHybridCost * 100.0) / 100.0);
        summary.put("avgCostPerKm", Math.round(avgCostPerKm * 100.0) / 100.0);
        summary.put("totalCarbonFootprint", Math.round(totalCarbonFootprint * 100.0) / 100.0);

        log.info("Company cost summary calculated: {} trips, ₹{} total cost", trips.size(), totalCost);
        return summary;
    }

    /**
     * Compare costs between EV and ICE fleets
     * 
     * @param companyId The company ID
     * @param startTime Start time for comparison period
     * @param endTime End time for comparison period
     * @return Map with comparison metrics
     */
    public Map<String, Object> compareFuelTypeCosts(Long companyId, 
                                                    LocalDateTime startTime, 
                                                    LocalDateTime endTime) {
        log.info("Comparing fuel type costs for company ID: {}", companyId);

        Map<String, Object> summary = getCompanyCostSummary(companyId, startTime, endTime);
        
        if (summary.isEmpty()) {
            return Collections.emptyMap();
        }

        double totalEVCost = (double) summary.getOrDefault("totalEVCost", 0.0);
        double totalICECost = (double) summary.getOrDefault("totalICECost", 0.0);
        int evTripCount = (int) summary.getOrDefault("evTripCount", 0);
        int iceTripCount = (int) summary.getOrDefault("iceTripCount", 0);

        double avgEVCostPerTrip = evTripCount > 0 ? totalEVCost / evTripCount : 0.0;
        double avgICECostPerTrip = iceTripCount > 0 ? totalICECost / iceTripCount : 0.0;

        Map<String, Object> comparison = new HashMap<>();
        comparison.put("companyId", companyId);
        comparison.put("periodStart", summary.get("periodStart"));
        comparison.put("periodEnd", summary.get("periodEnd"));
        comparison.put("evTotalCost", Math.round(totalEVCost * 100.0) / 100.0);
        comparison.put("iceTotalCost", Math.round(totalICECost * 100.0) / 100.0);
        comparison.put("evTripCount", evTripCount);
        comparison.put("iceTripCount", iceTripCount);
        comparison.put("avgEVCostPerTrip", Math.round(avgEVCostPerTrip * 100.0) / 100.0);
        comparison.put("avgICECostPerTrip", Math.round(avgICECostPerTrip * 100.0) / 100.0);
        
        if (avgICECostPerTrip > 0) {
            double costSavingsPercent = ((avgICECostPerTrip - avgEVCostPerTrip) / avgICECostPerTrip) * 100.0;
            comparison.put("evCostSavingsPercent", Math.round(costSavingsPercent * 100.0) / 100.0);
        }

        return comparison;
    }

    /**
     * Build trip cost analysis DTO from trip and vehicle
     * 
     * @param trip The trip entity
     * @param vehicle The vehicle entity
     * @return Trip cost analysis DTO
     */
    private TripCostAnalysisDTO buildTripCostAnalysis(Trip trip, Vehicle vehicle) {
        TripCostAnalysisDTO.TripCostAnalysisDTOBuilder builder = TripCostAnalysisDTO.builder()
                .tripId(trip.getId())
                .vehicleId(vehicle.getId())
                .vehicleNumber(vehicle.getVehicleNumber())
                .fuelType(vehicle.getFuelType())
                .distance(trip.getDistance())
                .durationMinutes(trip.getDurationMinutes())
                .efficiencyScore(trip.getEfficiencyScore())
                .averageSpeed(trip.getAverageSpeed())
                .harshAccelerationCount(trip.getHarshAccelerationCount())
                .harshBrakingCount(trip.getHarshBrakingCount())
                .overspeedingCount(trip.getOverspeedingCount())
                .idleTimeMinutes(trip.getIdleTimeMinutes())
                .driverId(trip.getDriverId())
                .status(trip.getStatus() != null ? trip.getStatus().name() : null);

        // Add timestamps
        if (trip.getStartTime() != null) {
            builder.startTime(trip.getStartTime().format(DATE_TIME_FORMATTER));
        }
        if (trip.getEndTime() != null) {
            builder.endTime(trip.getEndTime().format(DATE_TIME_FORMATTER));
        }

        // Calculate costs based on fuel type
        switch (vehicle.getFuelType()) {
            case EV:
                calculateEVMetrics(builder, trip, vehicle);
                break;
            case ICE:
                calculateICEMetrics(builder, trip, vehicle);
                break;
            case HYBRID:
                calculateHybridMetrics(builder, trip, vehicle);
                break;
        }

        return builder.build();
    }

    /**
     * Calculate metrics for EV vehicles
     */
    private void calculateEVMetrics(TripCostAnalysisDTO.TripCostAnalysisDTOBuilder builder, 
                                    Trip trip, Vehicle vehicle) {
        double energyCost = evCostCalculator.calculateEnergyCost(trip, vehicle);
        double energyEfficiency = evCostCalculator.calculateEnergyEfficiency(trip);
        double carbonFootprint = evCostCalculator.calculateCarbonFootprint(trip);
        double costPerKm = evCostCalculator.calculateCostPerKm(trip, vehicle);

        builder.energyCost(round(energyCost))
               .totalCost(round(energyCost))
               .costPerKm(round(costPerKm))
               .energyConsumed(trip.getEnergyConsumed())
               .energyEfficiency(round(energyEfficiency))
               .carbonFootprint(round(carbonFootprint))
               .startBatterySoc(trip.getStartBatterySoc())
               .endBatterySoc(trip.getEndBatterySoc());

        if (trip.getStartBatterySoc() != null && trip.getEndBatterySoc() != null) {
            double batteryConsumed = trip.getStartBatterySoc() - trip.getEndBatterySoc();
            builder.batteryConsumed(round(batteryConsumed));
        }

        if (trip.getDistance() != null && trip.getDistance() > 0 && carbonFootprint > 0) {
            double carbonPerKm = (carbonFootprint / trip.getDistance()) * 1000.0; // Convert to g CO2/km
            builder.carbonPerKm(round(carbonPerKm));
        }
    }

    /**
     * Calculate metrics for ICE vehicles
     */
    private void calculateICEMetrics(TripCostAnalysisDTO.TripCostAnalysisDTOBuilder builder, 
                                    Trip trip, Vehicle vehicle) {
        double fuelCost = iceCostCalculator.calculateFuelCost(trip, vehicle);
        double fuelEfficiency = iceCostCalculator.calculateFuelEfficiency(trip, vehicle);
        double mileage = iceCostCalculator.calculateMileage(trip, vehicle);
        double carbonFootprint = iceCostCalculator.calculateCarbonFootprint(trip, vehicle);
        double costPerKm = iceCostCalculator.calculateCostPerKm(trip, vehicle);

        // Estimate fuel consumed (would be better to get from FuelConsumption records)
        double fuelConsumed = 0.0;
        if (trip.getDistance() != null && fuelEfficiency > 0) {
            fuelConsumed = (trip.getDistance() / 100.0) * fuelEfficiency;
        }

        builder.fuelCost(round(fuelCost))
               .totalCost(round(fuelCost))
               .costPerKm(round(costPerKm))
               .fuelConsumed(round(fuelConsumed))
               .fuelEfficiency(round(fuelEfficiency))
               .mileage(round(mileage))
               .carbonFootprint(round(carbonFootprint));

        if (trip.getDistance() != null && trip.getDistance() > 0 && carbonFootprint > 0) {
            double carbonPerKm = (carbonFootprint / trip.getDistance()) * 1000.0; // Convert to g CO2/km
            builder.carbonPerKm(round(carbonPerKm));
        }
    }

    /**
     * Calculate metrics for HYBRID vehicles
     * Combines both EV and ICE metrics
     */
    private void calculateHybridMetrics(TripCostAnalysisDTO.TripCostAnalysisDTOBuilder builder, 
                                       Trip trip, Vehicle vehicle) {
        // For hybrid, we calculate both sets of metrics
        // In a real system, telemetry would tell us the split between electric and fuel usage
        
        double energyCost = evCostCalculator.calculateEnergyCost(trip, vehicle);
        double fuelCost = iceCostCalculator.calculateFuelCost(trip, vehicle);
        double totalCost = energyCost + fuelCost;
        
        double evCarbonFootprint = evCostCalculator.calculateCarbonFootprint(trip);
        double iceCarbonFootprint = iceCostCalculator.calculateCarbonFootprint(trip, vehicle);
        double totalCarbonFootprint = evCarbonFootprint + iceCarbonFootprint;

        double costPerKm = trip.getDistance() != null && trip.getDistance() > 0 ? 
                          totalCost / trip.getDistance() : 0.0;

        builder.energyCost(round(energyCost))
               .fuelCost(round(fuelCost))
               .totalCost(round(totalCost))
               .costPerKm(round(costPerKm))
               .energyConsumed(trip.getEnergyConsumed())
               .carbonFootprint(round(totalCarbonFootprint))
               .startBatterySoc(trip.getStartBatterySoc())
               .endBatterySoc(trip.getEndBatterySoc());

        if (trip.getStartBatterySoc() != null && trip.getEndBatterySoc() != null) {
            double batteryConsumed = trip.getStartBatterySoc() - trip.getEndBatterySoc();
            builder.batteryConsumed(round(batteryConsumed));
        }

        if (trip.getDistance() != null && trip.getDistance() > 0 && totalCarbonFootprint > 0) {
            double carbonPerKm = (totalCarbonFootprint / trip.getDistance()) * 1000.0; // Convert to g CO2/km
            builder.carbonPerKm(round(carbonPerKm));
        }
    }

    /**
     * Round to 2 decimal places
     */
    private Double round(double value) {
        return Math.round(value * 100.0) / 100.0;
    }
}
