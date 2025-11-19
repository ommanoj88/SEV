package com.evfleet.analytics.service;

import com.evfleet.analytics.dto.TCOAnalysisResponse;
import com.evfleet.analytics.model.TCOAnalysis;
import com.evfleet.analytics.repository.TCOAnalysisRepository;
import com.evfleet.charging.model.ChargingSession;
import com.evfleet.charging.repository.ChargingSessionRepository;
import com.evfleet.common.exception.ResourceNotFoundException;
import com.evfleet.fleet.model.FuelType;
import com.evfleet.fleet.model.Trip;
import com.evfleet.fleet.model.Vehicle;
import com.evfleet.fleet.repository.TripRepository;
import com.evfleet.fleet.repository.VehicleRepository;
import com.evfleet.maintenance.model.MaintenanceRecord;
import com.evfleet.maintenance.repository.MaintenanceRecordRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

/**
 * TCO Analysis Service
 * Handles Total Cost of Ownership calculations for vehicles
 *
 * @author SEV Platform Team
 * @version 1.0.0
 */
@Service
@Slf4j
@RequiredArgsConstructor
@Transactional
public class TCOAnalysisService {

    private final TCOAnalysisRepository tcoAnalysisRepository;
    private final VehicleRepository vehicleRepository;
    private final TripRepository tripRepository;
    private final ChargingSessionRepository chargingSessionRepository;
    private final MaintenanceRecordRepository maintenanceRecordRepository;

    // Industry benchmarks for ICE comparison
    private static final BigDecimal ICE_FUEL_COST_PER_KM = new BigDecimal("0.12"); // $0.12/km
    private static final BigDecimal ICE_MAINTENANCE_COST_PER_KM = new BigDecimal("0.08"); // $0.08/km
    private static final BigDecimal EV_MAINTENANCE_COST_FACTOR = new BigDecimal("0.6"); // EVs cost 60% of ICE
    private static final BigDecimal FIRST_YEAR_DEPRECIATION = new BigDecimal("0.20"); // 20% first year
    private static final BigDecimal SUBSEQUENT_YEAR_DEPRECIATION = new BigDecimal("0.15"); // 15% per year after

    /**
     * Calculate TCO for a vehicle
     */
    @Transactional(readOnly = true)
    public TCOAnalysisResponse calculateTCO(Long vehicleId, Integer analysisPeriodYears) {
        log.info("Calculating TCO for vehicle: {}, period: {} years", vehicleId, analysisPeriodYears);

        Vehicle vehicle = vehicleRepository.findById(vehicleId)
                .orElseThrow(() -> new ResourceNotFoundException("Vehicle not found with id: " + vehicleId));

        LocalDate today = LocalDate.now();
        
        // Get or create TCO analysis
        TCOAnalysis tco = tcoAnalysisRepository
                .findLatestByVehicleId(vehicleId)
                .orElse(TCOAnalysis.builder()
                        .companyId(vehicle.getCompanyId())
                        .vehicleId(vehicleId)
                        .analysisDate(today)
                        .analysisPeriodYears(analysisPeriodYears)
                        .build());

        // Update analysis
        tco.setAnalysisDate(today);
        tco.setAnalysisPeriodYears(analysisPeriodYears);

        // Calculate costs
        calculateAcquisitionCosts(tco, vehicle);
        calculateOperatingCosts(tco, vehicle);
        calculateMetrics(tco, vehicle);

        // Calculate ICE comparison for EVs
        if (vehicle.getFuelType() == FuelType.EV) {
            calculateICEComparison(tco, vehicle);
        }

        // Save the analysis
        tco = tcoAnalysisRepository.save(tco);
        log.info("TCO analysis saved for vehicle: {}", vehicleId);

        return convertToResponse(tco, vehicle);
    }

    /**
     * Get latest TCO analysis for a vehicle
     */
    @Transactional(readOnly = true)
    public TCOAnalysisResponse getTCOAnalysis(Long vehicleId) {
        log.info("Getting TCO analysis for vehicle: {}", vehicleId);

        Vehicle vehicle = vehicleRepository.findById(vehicleId)
                .orElseThrow(() -> new ResourceNotFoundException("Vehicle not found with id: " + vehicleId));

        Optional<TCOAnalysis> tcoOpt = tcoAnalysisRepository.findLatestByVehicleId(vehicleId);
        
        if (tcoOpt.isEmpty()) {
            // Calculate fresh TCO if not found
            return calculateTCO(vehicleId, 5);
        }

        return convertToResponse(tcoOpt.get(), vehicle);
    }

    /**
     * Get TCO trend over time
     */
    @Transactional(readOnly = true)
    public List<TCOAnalysisResponse> getTCOTrend(Long vehicleId, LocalDate startDate, LocalDate endDate) {
        log.info("Getting TCO trend for vehicle: {} from {} to {}", vehicleId, startDate, endDate);

        Vehicle vehicle = vehicleRepository.findById(vehicleId)
                .orElseThrow(() -> new ResourceNotFoundException("Vehicle not found with id: " + vehicleId));

        List<TCOAnalysis> tcoList = tcoAnalysisRepository
                .findByVehicleIdAndAnalysisDateBetween(vehicleId, startDate, endDate);

        return tcoList.stream()
                .map(tco -> convertToResponse(tco, vehicle))
                .toList();
    }

    /**
     * Recalculate TCO for all vehicles (scheduled job)
     */
    @Transactional
    public void recalculateTCOForAllVehicles() {
        log.info("Starting TCO recalculation for all vehicles");

        List<Vehicle> vehicles = vehicleRepository.findAll();
        int count = 0;

        for (Vehicle vehicle : vehicles) {
            try {
                calculateTCO(vehicle.getId(), 5);
                count++;
            } catch (Exception e) {
                log.error("Error calculating TCO for vehicle: {}", vehicle.getId(), e);
            }
        }

        log.info("Completed TCO recalculation for {} vehicles", count);
    }

    /**
     * Calculate acquisition costs (purchase price and depreciation)
     */
    private void calculateAcquisitionCosts(TCOAnalysis tco, Vehicle vehicle) {
        // Purchase price - would come from vehicle entity if tracked
        // For now, using placeholder or existing value
        if (tco.getPurchasePrice().compareTo(BigDecimal.ZERO) == 0) {
            // Estimate based on vehicle type if not available
            tco.setPurchasePrice(new BigDecimal("35000")); // Default placeholder
        }

        // Calculate depreciation based on vehicle age
        LocalDate purchaseDate = vehicle.getCreatedAt() != null 
                ? vehicle.getCreatedAt().toLocalDate() 
                : LocalDate.now().minusYears(1);
        
        long monthsOwned = ChronoUnit.MONTHS.between(purchaseDate, LocalDate.now());
        int yearsOwned = (int) (monthsOwned / 12);
        
        BigDecimal depreciation = BigDecimal.ZERO;
        BigDecimal currentValue = tco.getPurchasePrice();
        
        // First year depreciation
        if (yearsOwned >= 1) {
            depreciation = currentValue.multiply(FIRST_YEAR_DEPRECIATION);
            currentValue = currentValue.subtract(depreciation);
            yearsOwned--;
        }
        
        // Subsequent years
        for (int i = 0; i < yearsOwned; i++) {
            BigDecimal yearlyDep = currentValue.multiply(SUBSEQUENT_YEAR_DEPRECIATION);
            depreciation = depreciation.add(yearlyDep);
            currentValue = currentValue.subtract(yearlyDep);
        }
        
        tco.setDepreciationValue(depreciation);
    }

    /**
     * Calculate operating costs (energy, maintenance, insurance, etc.)
     */
    private void calculateOperatingCosts(TCOAnalysis tco, Vehicle vehicle) {
        // 1. Energy/Fuel costs
        BigDecimal energyCosts = BigDecimal.ZERO;
        
        if (vehicle.getFuelType() == FuelType.EV) {
            // Get all charging sessions
            List<ChargingSession> sessions = chargingSessionRepository.findByVehicleId(vehicle.getId());
            energyCosts = sessions.stream()
                    .map(s -> s.getCost() != null ? s.getCost() : BigDecimal.ZERO)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
        } else {
            // For ICE/Hybrid, estimate from trips
            List<Trip> trips = tripRepository.findByVehicleId(vehicle.getId());
            double totalDistance = trips.stream()
                    .mapToDouble(t -> t.getDistance() != null ? t.getDistance() : 0.0)
                    .sum();
            energyCosts = ICE_FUEL_COST_PER_KM.multiply(BigDecimal.valueOf(totalDistance));
        }
        
        tco.setEnergyCosts(energyCosts);

        // 2. Maintenance costs
        List<MaintenanceRecord> maintenanceRecords = maintenanceRecordRepository.findByVehicleId(vehicle.getId());
        BigDecimal maintenanceCosts = maintenanceRecords.stream()
                .map(m -> m.getCost() != null ? m.getCost() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        tco.setMaintenanceCosts(maintenanceCosts);

        // 3. Insurance costs (placeholder - would come from billing records)
        if (tco.getInsuranceCosts().compareTo(BigDecimal.ZERO) == 0) {
            // Estimate annual insurance
            LocalDate purchaseDate = vehicle.getCreatedAt() != null 
                    ? vehicle.getCreatedAt().toLocalDate() 
                    : LocalDate.now().minusYears(1);
            long monthsOwned = ChronoUnit.MONTHS.between(purchaseDate, LocalDate.now());
            BigDecimal estimatedAnnualInsurance = new BigDecimal("1200"); // $1200/year
            BigDecimal totalInsurance = estimatedAnnualInsurance
                    .multiply(BigDecimal.valueOf(monthsOwned))
                    .divide(BigDecimal.valueOf(12), 2, RoundingMode.HALF_UP);
            tco.setInsuranceCosts(totalInsurance);
        }

        // 4. Taxes and fees (placeholder)
        if (tco.getTaxesFees().compareTo(BigDecimal.ZERO) == 0) {
            tco.setTaxesFees(new BigDecimal("500")); // Annual registration/taxes
        }

        // 5. Other costs
        if (tco.getOtherCosts().compareTo(BigDecimal.ZERO) == 0) {
            tco.setOtherCosts(BigDecimal.ZERO);
        }

        // Calculate total cost
        tco.calculateTotalCost();
    }

    /**
     * Calculate metrics (cost per km, cost per year)
     */
    private void calculateMetrics(TCOAnalysis tco, Vehicle vehicle) {
        // Get total distance from trips
        List<Trip> trips = tripRepository.findByVehicleId(vehicle.getId());
        double totalDistance = trips.stream()
                .mapToDouble(t -> t.getDistance() != null ? t.getDistance() : 0.0)
                .sum();
        
        tco.setTotalDistanceKm(BigDecimal.valueOf(totalDistance));
        
        // Calculate cost per km
        tco.calculateCostPerKm();
        
        // Calculate cost per year
        tco.calculateCostPerYear();
    }

    /**
     * Calculate ICE comparison for electric vehicles
     */
    private void calculateICEComparison(TCOAnalysis tco, Vehicle vehicle) {
        BigDecimal totalDistance = tco.getTotalDistanceKm();
        
        if (totalDistance.compareTo(BigDecimal.ZERO) == 0) {
            return;
        }

        // 1. Fuel savings (ICE fuel cost - EV electricity cost)
        BigDecimal iceFuelCost = ICE_FUEL_COST_PER_KM.multiply(totalDistance);
        BigDecimal fuelSavings = iceFuelCost.subtract(tco.getEnergyCosts());
        tco.setIceFuelSavings(fuelSavings);

        // 2. Maintenance savings (ICE maintenance is typically higher)
        BigDecimal iceMaintenanceCost = ICE_MAINTENANCE_COST_PER_KM.multiply(totalDistance);
        BigDecimal maintenanceSavings = iceMaintenanceCost.subtract(tco.getMaintenanceCosts());
        tco.setIceMaintenanceSavings(maintenanceSavings);

        // 3. Calculate total savings
        tco.calculateIceTotalSavings();

        // 4. Calculate payback period
        // Assuming EV has $5000 premium over ICE
        BigDecimal evPremium = new BigDecimal("5000");
        BigDecimal annualSavings = tco.getIceTotalSavings()
                .divide(BigDecimal.valueOf(tco.getAnalysisPeriodYears()), 2, RoundingMode.HALF_UP);
        
        if (annualSavings.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal paybackYears = evPremium.divide(annualSavings, 2, RoundingMode.HALF_UP);
            int paybackMonths = paybackYears.multiply(BigDecimal.valueOf(12)).intValue();
            tco.setIcePaybackPeriodMonths(paybackMonths);
        }
    }

    /**
     * Convert TCOAnalysis entity to response DTO
     */
    private TCOAnalysisResponse convertToResponse(TCOAnalysis tco, Vehicle vehicle) {
        TCOAnalysisResponse.ComparisonWithICE comparison = null;
        
        if (vehicle.getFuelType() == FuelType.EV && 
            tco.getIceTotalSavings() != null && 
            tco.getIceTotalSavings().compareTo(BigDecimal.ZERO) > 0) {
            
            // Calculate savings percentage
            BigDecimal iceTotalCost = tco.getTotalCost().add(tco.getIceTotalSavings());
            Double savingsPercentage = tco.getIceTotalSavings()
                    .divide(iceTotalCost, 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100))
                    .doubleValue();
            
            comparison = TCOAnalysisResponse.ComparisonWithICE.builder()
                    .fuelSavings(tco.getIceFuelSavings())
                    .maintenanceSavings(tco.getIceMaintenanceSavings())
                    .totalSavings(tco.getIceTotalSavings())
                    .paybackPeriod(tco.getIcePaybackPeriodMonths())
                    .savingsPercentage(savingsPercentage)
                    .build();
        }

        String vehicleName = vehicle.getMake() + " " + vehicle.getModel();

        return TCOAnalysisResponse.builder()
                .id(tco.getId())
                .vehicleId(vehicle.getId())
                .vehicleName(vehicleName)
                .vehicleNumber(vehicle.getVehicleNumber())
                .fuelType(vehicle.getFuelType())
                .analysisDate(tco.getAnalysisDate())
                .purchasePrice(tco.getPurchasePrice())
                .depreciation(tco.getDepreciationValue())
                .ageMonths((int) ChronoUnit.MONTHS.between(
                        vehicle.getCreatedAt().toLocalDate(), 
                        LocalDate.now()))
                .energyCosts(tco.getEnergyCosts())
                .maintenanceCosts(tco.getMaintenanceCosts())
                .insuranceCosts(tco.getInsuranceCosts())
                .taxesFees(tco.getTaxesFees())
                .otherCosts(tco.getOtherCosts())
                .totalCost(tco.getTotalCost())
                .costPerKm(tco.getCostPerKm())
                .costPerYear(tco.getCostPerYear())
                .analysisPeriodYears(tco.getAnalysisPeriodYears())
                .totalDistanceKm(tco.getTotalDistanceKm())
                .comparisonWithICE(comparison)
                .build();
    }
}
