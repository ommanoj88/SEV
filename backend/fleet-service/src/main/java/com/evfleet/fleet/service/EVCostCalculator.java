package com.evfleet.fleet.service;

import com.evfleet.fleet.model.Trip;
import com.evfleet.fleet.model.Vehicle;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Cost Calculator for Electric Vehicles (EV)
 * Calculates energy costs, efficiency metrics, and carbon footprint for EV trips
 * 
 * @since 2.0.0 (Multi-fuel support - PR 7)
 */
@Component
@Slf4j
public class EVCostCalculator {

    // Default electricity cost per kWh in INR (can be configured)
    private static final double DEFAULT_ELECTRICITY_COST_PER_KWH = 8.0; // ₹8 per kWh
    
    // Carbon emissions factor for electricity in India (kg CO2 per kWh)
    // Based on grid mix - approximately 0.82 kg CO2 per kWh
    private static final double CARBON_EMISSION_FACTOR_KWH = 0.82;

    /**
     * Calculate the energy cost for an EV trip
     * 
     * @param trip The completed trip
     * @param vehicle The EV vehicle
     * @return Energy cost in INR
     */
    public double calculateEnergyCost(Trip trip, Vehicle vehicle) {
        if (trip.getEnergyConsumed() == null || trip.getEnergyConsumed() <= 0) {
            log.warn("Trip {} has no energy consumption data", trip.getId());
            return 0.0;
        }
        
        double energyCost = trip.getEnergyConsumed() * DEFAULT_ELECTRICITY_COST_PER_KWH;
        log.debug("EV Trip {} energy cost: ₹{} ({} kWh @ ₹{}/kWh)", 
                  trip.getId(), energyCost, trip.getEnergyConsumed(), DEFAULT_ELECTRICITY_COST_PER_KWH);
        
        return energyCost;
    }

    /**
     * Calculate the energy cost with custom electricity rate
     * 
     * @param trip The completed trip
     * @param vehicle The EV vehicle
     * @param electricityCostPerKwh Custom electricity cost per kWh
     * @return Energy cost in INR
     */
    public double calculateEnergyCost(Trip trip, Vehicle vehicle, double electricityCostPerKwh) {
        if (trip.getEnergyConsumed() == null || trip.getEnergyConsumed() <= 0) {
            log.warn("Trip {} has no energy consumption data", trip.getId());
            return 0.0;
        }
        
        double energyCost = trip.getEnergyConsumed() * electricityCostPerKwh;
        log.debug("EV Trip {} energy cost: ₹{} ({} kWh @ ₹{}/kWh)", 
                  trip.getId(), energyCost, trip.getEnergyConsumed(), electricityCostPerKwh);
        
        return energyCost;
    }

    /**
     * Calculate energy efficiency (kWh per 100 km)
     * 
     * @param trip The completed trip
     * @return Energy efficiency in kWh/100km, or 0 if data is insufficient
     */
    public double calculateEnergyEfficiency(Trip trip) {
        if (trip.getDistance() == null || trip.getDistance() <= 0 ||
            trip.getEnergyConsumed() == null || trip.getEnergyConsumed() <= 0) {
            log.warn("Trip {} has insufficient data for efficiency calculation", trip.getId());
            return 0.0;
        }
        
        // Calculate kWh per 100 km
        double efficiency = (trip.getEnergyConsumed() / trip.getDistance()) * 100.0;
        log.debug("EV Trip {} efficiency: {} kWh/100km", trip.getId(), efficiency);
        
        return efficiency;
    }

    /**
     * Calculate carbon footprint for an EV trip
     * Based on electricity generation carbon intensity
     * 
     * @param trip The completed trip
     * @return Carbon emissions in kg CO2
     */
    public double calculateCarbonFootprint(Trip trip) {
        if (trip.getEnergyConsumed() == null || trip.getEnergyConsumed() <= 0) {
            log.warn("Trip {} has no energy consumption data for carbon calculation", trip.getId());
            return 0.0;
        }
        
        double carbonEmissions = trip.getEnergyConsumed() * CARBON_EMISSION_FACTOR_KWH;
        log.debug("EV Trip {} carbon footprint: {} kg CO2 ({} kWh @ {} kg CO2/kWh)", 
                  trip.getId(), carbonEmissions, trip.getEnergyConsumed(), CARBON_EMISSION_FACTOR_KWH);
        
        return carbonEmissions;
    }

    /**
     * Calculate cost per kilometer for an EV trip
     * 
     * @param trip The completed trip
     * @param vehicle The EV vehicle
     * @return Cost per kilometer in INR, or 0 if data is insufficient
     */
    public double calculateCostPerKm(Trip trip, Vehicle vehicle) {
        if (trip.getDistance() == null || trip.getDistance() <= 0) {
            log.warn("Trip {} has no distance data", trip.getId());
            return 0.0;
        }
        
        double totalCost = calculateEnergyCost(trip, vehicle);
        if (totalCost <= 0) {
            return 0.0;
        }
        
        double costPerKm = totalCost / trip.getDistance();
        log.debug("EV Trip {} cost per km: ₹{}/km", trip.getId(), costPerKm);
        
        return costPerKm;
    }

    /**
     * Calculate estimated range remaining based on current battery SOC
     * 
     * @param vehicle The EV vehicle
     * @param averageEfficiency Average energy efficiency in kWh/100km
     * @return Estimated range in kilometers
     */
    public double calculateEstimatedRange(Vehicle vehicle, double averageEfficiency) {
        if (vehicle.getBatteryCapacity() == null || vehicle.getCurrentBatterySoc() == null ||
            averageEfficiency <= 0) {
            log.warn("Insufficient data for range estimation for vehicle {}", vehicle.getId());
            return 0.0;
        }
        
        double availableEnergy = vehicle.getBatteryCapacity() * (vehicle.getCurrentBatterySoc() / 100.0);
        double estimatedRange = (availableEnergy / averageEfficiency) * 100.0;
        
        log.debug("Vehicle {} estimated range: {} km (available energy: {} kWh, efficiency: {} kWh/100km)",
                  vehicle.getId(), estimatedRange, availableEnergy, averageEfficiency);
        
        return estimatedRange;
    }

    /**
     * Get default electricity cost per kWh
     * 
     * @return Default electricity cost in INR per kWh
     */
    public double getDefaultElectricityCost() {
        return DEFAULT_ELECTRICITY_COST_PER_KWH;
    }

    /**
     * Get carbon emission factor for electricity
     * 
     * @return Carbon emissions in kg CO2 per kWh
     */
    public double getCarbonEmissionFactor() {
        return CARBON_EMISSION_FACTOR_KWH;
    }
}
