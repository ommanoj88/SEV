package com.evfleet.fleet.service;

import com.evfleet.fleet.model.FuelConsumption;
import com.evfleet.fleet.model.Trip;
import com.evfleet.fleet.model.Vehicle;
import com.evfleet.fleet.repository.FuelConsumptionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Cost Calculator for Internal Combustion Engine (ICE) Vehicles
 * Calculates fuel costs, efficiency metrics, and carbon footprint for ICE trips
 * 
 * @since 2.0.0 (Multi-fuel support - PR 7)
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class ICECostCalculator {

    private final FuelConsumptionRepository fuelConsumptionRepository;

    // Default fuel costs per liter in INR (can be configured)
    private static final double DEFAULT_PETROL_COST_PER_LITER = 105.0; // ₹105 per liter
    private static final double DEFAULT_DIESEL_COST_PER_LITER = 95.0;  // ₹95 per liter
    private static final double DEFAULT_CNG_COST_PER_KG = 80.0;        // ₹80 per kg
    
    // Carbon emissions factors (kg CO2 per liter)
    // Source: EPA/IPCC emission factors
    private static final double PETROL_CARBON_FACTOR = 2.31;  // kg CO2 per liter
    private static final double DIESEL_CARBON_FACTOR = 2.68;  // kg CO2 per liter
    private static final double CNG_CARBON_FACTOR = 1.88;     // kg CO2 per kg

    /**
     * Calculate the fuel cost for an ICE trip
     * Uses fuel consumption records if available, otherwise estimates from distance
     * 
     * @param trip The completed trip
     * @param vehicle The ICE vehicle
     * @return Fuel cost in INR
     */
    public double calculateFuelCost(Trip trip, Vehicle vehicle) {
        double fuelConsumed = getFuelConsumedForTrip(trip, vehicle);
        
        if (fuelConsumed <= 0) {
            log.warn("Trip {} has no fuel consumption data", trip.getId());
            return 0.0;
        }
        
        double fuelCostPerUnit = getFuelCostPerUnit(vehicle.getEngineType());
        double fuelCost = fuelConsumed * fuelCostPerUnit;
        
        log.debug("ICE Trip {} fuel cost: ₹{} ({} liters @ ₹{}/liter)", 
                  trip.getId(), fuelCost, fuelConsumed, fuelCostPerUnit);
        
        return fuelCost;
    }

    /**
     * Calculate the fuel cost with custom fuel price
     * 
     * @param trip The completed trip
     * @param vehicle The ICE vehicle
     * @param fuelCostPerUnit Custom fuel cost per liter/kg
     * @return Fuel cost in INR
     */
    public double calculateFuelCost(Trip trip, Vehicle vehicle, double fuelCostPerUnit) {
        double fuelConsumed = getFuelConsumedForTrip(trip, vehicle);
        
        if (fuelConsumed <= 0) {
            log.warn("Trip {} has no fuel consumption data", trip.getId());
            return 0.0;
        }
        
        double fuelCost = fuelConsumed * fuelCostPerUnit;
        
        log.debug("ICE Trip {} fuel cost: ₹{} ({} liters @ ₹{}/liter)", 
                  trip.getId(), fuelCost, fuelConsumed, fuelCostPerUnit);
        
        return fuelCost;
    }

    /**
     * Calculate fuel efficiency (liters per 100 km or km per liter)
     * 
     * @param trip The completed trip
     * @param vehicle The ICE vehicle
     * @return Fuel efficiency in liters/100km, or 0 if data is insufficient
     */
    public double calculateFuelEfficiency(Trip trip, Vehicle vehicle) {
        if (trip.getDistance() == null || trip.getDistance() <= 0) {
            log.warn("Trip {} has no distance data", trip.getId());
            return 0.0;
        }
        
        double fuelConsumed = getFuelConsumedForTrip(trip, vehicle);
        if (fuelConsumed <= 0) {
            log.warn("Trip {} has no fuel consumption data", trip.getId());
            return 0.0;
        }
        
        // Calculate liters per 100 km
        double efficiency = (fuelConsumed / trip.getDistance()) * 100.0;
        log.debug("ICE Trip {} efficiency: {} L/100km", trip.getId(), efficiency);
        
        return efficiency;
    }

    /**
     * Calculate fuel efficiency in km per liter (mileage)
     * 
     * @param trip The completed trip
     * @param vehicle The ICE vehicle
     * @return Fuel efficiency in km/liter, or 0 if data is insufficient
     */
    public double calculateMileage(Trip trip, Vehicle vehicle) {
        if (trip.getDistance() == null || trip.getDistance() <= 0) {
            log.warn("Trip {} has no distance data", trip.getId());
            return 0.0;
        }
        
        double fuelConsumed = getFuelConsumedForTrip(trip, vehicle);
        if (fuelConsumed <= 0) {
            log.warn("Trip {} has no fuel consumption data", trip.getId());
            return 0.0;
        }
        
        // Calculate km per liter
        double mileage = trip.getDistance() / fuelConsumed;
        log.debug("ICE Trip {} mileage: {} km/L", trip.getId(), mileage);
        
        return mileage;
    }

    /**
     * Calculate carbon footprint for an ICE trip
     * Based on fuel type and consumption
     * 
     * @param trip The completed trip
     * @param vehicle The ICE vehicle
     * @return Carbon emissions in kg CO2
     */
    public double calculateCarbonFootprint(Trip trip, Vehicle vehicle) {
        double fuelConsumed = getFuelConsumedForTrip(trip, vehicle);
        
        if (fuelConsumed <= 0) {
            log.warn("Trip {} has no fuel consumption data for carbon calculation", trip.getId());
            return 0.0;
        }
        
        double carbonFactor = getCarbonEmissionFactor(vehicle.getEngineType());
        double carbonEmissions = fuelConsumed * carbonFactor;
        
        log.debug("ICE Trip {} carbon footprint: {} kg CO2 ({} liters @ {} kg CO2/liter)", 
                  trip.getId(), carbonEmissions, fuelConsumed, carbonFactor);
        
        return carbonEmissions;
    }

    /**
     * Calculate cost per kilometer for an ICE trip
     * 
     * @param trip The completed trip
     * @param vehicle The ICE vehicle
     * @return Cost per kilometer in INR, or 0 if data is insufficient
     */
    public double calculateCostPerKm(Trip trip, Vehicle vehicle) {
        if (trip.getDistance() == null || trip.getDistance() <= 0) {
            log.warn("Trip {} has no distance data", trip.getId());
            return 0.0;
        }
        
        double totalCost = calculateFuelCost(trip, vehicle);
        if (totalCost <= 0) {
            return 0.0;
        }
        
        double costPerKm = totalCost / trip.getDistance();
        log.debug("ICE Trip {} cost per km: ₹{}/km", trip.getId(), costPerKm);
        
        return costPerKm;
    }

    /**
     * Calculate estimated range remaining based on current fuel level
     * 
     * @param vehicle The ICE vehicle
     * @param averageMileage Average mileage in km/liter
     * @return Estimated range in kilometers
     */
    public double calculateEstimatedRange(Vehicle vehicle, double averageMileage) {
        if (vehicle.getFuelLevel() == null || averageMileage <= 0) {
            log.warn("Insufficient data for range estimation for vehicle {}", vehicle.getId());
            return 0.0;
        }
        
        double estimatedRange = vehicle.getFuelLevel() * averageMileage;
        
        log.debug("Vehicle {} estimated range: {} km (fuel level: {} L, mileage: {} km/L)",
                  vehicle.getId(), estimatedRange, vehicle.getFuelLevel(), averageMileage);
        
        return estimatedRange;
    }

    /**
     * Get fuel consumed for a trip from fuel_consumption records
     * If not available, returns 0 (should be calculated from odometer or telemetry)
     * 
     * @param trip The trip
     * @param vehicle The vehicle
     * @return Fuel consumed in liters
     */
    private double getFuelConsumedForTrip(Trip trip, Vehicle vehicle) {
        // Try to get from fuel consumption records
        List<FuelConsumption> consumptions = fuelConsumptionRepository
                .findByVehicleIdAndTimestampBetweenOrderByTimestampDesc(
                        vehicle.getId(),
                        trip.getStartTime(),
                        trip.getEndTime() != null ? trip.getEndTime() : LocalDateTime.now()
                );
        
        if (!consumptions.isEmpty()) {
            double totalConsumption = consumptions.stream()
                    .mapToDouble(FuelConsumption::getFuelConsumedLiters)
                    .sum();
            log.debug("Found fuel consumption records for trip {}: {} liters", trip.getId(), totalConsumption);
            return totalConsumption;
        }
        
        // Fallback: estimate from distance using average consumption
        // Assuming average consumption of 8 liters per 100 km for LCV
        if (trip.getDistance() != null && trip.getDistance() > 0) {
            double estimatedConsumption = (trip.getDistance() / 100.0) * 8.0;
            log.debug("Estimated fuel consumption for trip {}: {} liters (from {} km)", 
                      trip.getId(), estimatedConsumption, trip.getDistance());
            return estimatedConsumption;
        }
        
        return 0.0;
    }

    /**
     * Get fuel cost per unit based on engine type
     * 
     * @param engineType The engine/fuel type (Petrol, Diesel, CNG, etc.)
     * @return Fuel cost per liter/kg in INR
     */
    private double getFuelCostPerUnit(String engineType) {
        if (engineType == null) {
            return DEFAULT_DIESEL_COST_PER_LITER; // Default to diesel
        }
        
        return switch (engineType.toUpperCase()) {
            case "PETROL", "GASOLINE" -> DEFAULT_PETROL_COST_PER_LITER;
            case "DIESEL" -> DEFAULT_DIESEL_COST_PER_LITER;
            case "CNG", "COMPRESSED_NATURAL_GAS" -> DEFAULT_CNG_COST_PER_KG;
            default -> DEFAULT_DIESEL_COST_PER_LITER;
        };
    }

    /**
     * Get carbon emission factor based on engine type
     * 
     * @param engineType The engine/fuel type
     * @return Carbon emissions in kg CO2 per liter/kg
     */
    private double getCarbonEmissionFactor(String engineType) {
        if (engineType == null) {
            return DIESEL_CARBON_FACTOR; // Default to diesel
        }
        
        return switch (engineType.toUpperCase()) {
            case "PETROL", "GASOLINE" -> PETROL_CARBON_FACTOR;
            case "DIESEL" -> DIESEL_CARBON_FACTOR;
            case "CNG", "COMPRESSED_NATURAL_GAS" -> CNG_CARBON_FACTOR;
            default -> DIESEL_CARBON_FACTOR;
        };
    }

    /**
     * Get default fuel cost per liter for petrol
     * 
     * @return Default petrol cost in INR per liter
     */
    public double getDefaultPetrolCost() {
        return DEFAULT_PETROL_COST_PER_LITER;
    }

    /**
     * Get default fuel cost per liter for diesel
     * 
     * @return Default diesel cost in INR per liter
     */
    public double getDefaultDieselCost() {
        return DEFAULT_DIESEL_COST_PER_LITER;
    }

    /**
     * Get default CNG cost per kg
     * 
     * @return Default CNG cost in INR per kg
     */
    public double getDefaultCngCost() {
        return DEFAULT_CNG_COST_PER_KG;
    }
}
