package com.evfleet.analytics.service;

import com.evfleet.analytics.dto.TCOAnalysisResponse;
import com.evfleet.analytics.dto.TCOProjectionResponse;
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
import io.micrometer.core.instrument.MeterRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;

/**
 * TCO Analysis Service
 * Handles Total Cost of Ownership calculations for vehicles
 * 
 * Enhanced with:
 * - Multi-fuel support (EV, ICE, HYBRID, CNG, LPG)
 * - Regional cost variations
 * - Carbon cost calculations for ESG
 * - 5-year projections
 * - Cross-fuel type comparison mode
 *
 * @author SEV Platform Team
 * @version 2.0.0
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
    private final MeterRegistry meterRegistry;

    // ==================== Multi-Fuel Cost Constants ====================
    
    // ICE (Petrol/Diesel) costs per km
    private static final BigDecimal PETROL_FUEL_COST_PER_KM = new BigDecimal("6.00"); // ₹6/km at 15km/l, ₹90/l
    private static final BigDecimal DIESEL_FUEL_COST_PER_KM = new BigDecimal("5.50"); // ₹5.5/km at 18km/l, ₹99/l
    private static final BigDecimal ICE_MAINTENANCE_COST_PER_KM = new BigDecimal("1.50"); // ₹1.5/km
    
    // EV costs
    private static final BigDecimal EV_ENERGY_COST_PER_KWH = new BigDecimal("8.00"); // ₹8/kWh
    private static final BigDecimal EV_EFFICIENCY_KWH_PER_KM = new BigDecimal("0.15"); // 150 Wh/km
    private static final BigDecimal EV_MAINTENANCE_COST_FACTOR = new BigDecimal("0.40"); // 40% of ICE
    private static final BigDecimal EV_DEPRECIATION_FACTOR = new BigDecimal("0.85"); // EVs depreciate less
    
    // Hybrid costs (30% more efficient than ICE)
    private static final BigDecimal HYBRID_FUEL_EFFICIENCY_FACTOR = new BigDecimal("0.70"); // 30% fuel savings
    private static final BigDecimal HYBRID_MAINTENANCE_COST_FACTOR = new BigDecimal("0.75"); // 75% of ICE
    
    // CNG costs
    private static final BigDecimal CNG_COST_PER_KG = new BigDecimal("80.00"); // ₹80/kg
    private static final BigDecimal CNG_EFFICIENCY_KM_PER_KG = new BigDecimal("22.00"); // 22 km/kg
    private static final BigDecimal CNG_MAINTENANCE_COST_FACTOR = new BigDecimal("0.85");
    
    // LPG costs
    private static final BigDecimal LPG_COST_PER_LITER = new BigDecimal("55.00"); // ₹55/liter
    private static final BigDecimal LPG_EFFICIENCY_KM_PER_L = new BigDecimal("12.00"); // 12 km/l
    private static final BigDecimal LPG_MAINTENANCE_COST_FACTOR = new BigDecimal("0.90");
    
    // Depreciation rates
    private static final BigDecimal FIRST_YEAR_DEPRECIATION = new BigDecimal("0.20");
    private static final BigDecimal SUBSEQUENT_YEAR_DEPRECIATION = new BigDecimal("0.15");
    
    // Carbon emission factors (kg CO2 per unit)
    private static final BigDecimal PETROL_CO2_PER_LITER = new BigDecimal("2.31");
    private static final BigDecimal DIESEL_CO2_PER_LITER = new BigDecimal("2.68");
    private static final BigDecimal CNG_CO2_PER_KG = new BigDecimal("2.75");
    private static final BigDecimal LPG_CO2_PER_LITER = new BigDecimal("1.51");
    private static final BigDecimal ELECTRICITY_CO2_PER_KWH = new BigDecimal("0.82"); // India grid average
    
    // Carbon cost (social cost of carbon)
    private static final BigDecimal CARBON_COST_PER_KG = new BigDecimal("5.00"); // ₹5/kg CO2
    
    // Regional cost adjustments (multiplier based on fuel prices)
    private static final Map<String, BigDecimal> REGIONAL_FUEL_ADJUSTMENTS = Map.of(
            "MUMBAI", new BigDecimal("1.15"),
            "DELHI", new BigDecimal("1.10"),
            "BANGALORE", new BigDecimal("1.12"),
            "CHENNAI", new BigDecimal("1.08"),
            "KOLKATA", new BigDecimal("1.05"),
            "HYDERABAD", new BigDecimal("1.07"),
            "PUNE", new BigDecimal("1.10"),
            "DEFAULT", new BigDecimal("1.00")
    );
    
    // Vehicle purchase price premiums by fuel type
    private static final Map<FuelType, BigDecimal> FUEL_TYPE_PREMIUMS = Map.of(
            FuelType.EV, new BigDecimal("500000"),      // ₹5L premium for EV
            FuelType.HYBRID, new BigDecimal("200000"),  // ₹2L premium for hybrid
            FuelType.CNG, new BigDecimal("80000"),      // ₹80K for CNG kit
            FuelType.LPG, new BigDecimal("50000")       // ₹50K for LPG kit
    );
    
    @Value("${analytics.tco.projection-years:5}")
    private int defaultProjectionYears;
    
    @Value("${analytics.tco.include-carbon-cost:true}")
    private boolean includeCarbonCost;

    // ==================== Core TCO Methods ====================

    /**
     * Calculate TCO for a vehicle with multi-fuel support
     */
    @Transactional
    public TCOAnalysisResponse calculateTCO(Long vehicleId, Integer analysisPeriodYears) {
        log.info("Calculating TCO for vehicle: {}, period: {} years", vehicleId, analysisPeriodYears);
        meterRegistry.counter("tco.analysis.calculated").increment();

        Vehicle vehicle = vehicleRepository.findById(vehicleId)
                .orElseThrow(() -> new ResourceNotFoundException("Vehicle not found with id: " + vehicleId));

        LocalDate today = LocalDate.now();
        int period = analysisPeriodYears != null ? analysisPeriodYears : defaultProjectionYears;
        
        // Get or create TCO analysis
        TCOAnalysis tco = tcoAnalysisRepository
                .findLatestByVehicleId(vehicleId)
                .orElse(TCOAnalysis.builder()
                        .companyId(vehicle.getCompanyId())
                        .vehicleId(vehicleId)
                        .analysisDate(today)
                        .analysisPeriodYears(period)
                        .fuelType(vehicle.getFuelType())
                        .build());

        // Update analysis metadata
        tco.setAnalysisDate(today);
        tco.setAnalysisPeriodYears(period);
        tco.setFuelType(vehicle.getFuelType());

        // Calculate costs
        calculateAcquisitionCosts(tco, vehicle);
        calculateOperatingCostsByFuelType(tco, vehicle);
        calculateCarbonCosts(tco, vehicle);
        calculateMetrics(tco, vehicle);

        // Calculate comparison with ICE for non-ICE vehicles
        if (vehicle.getFuelType() != null && vehicle.getFuelType() != FuelType.ICE) {
            calculateICEComparison(tco, vehicle);
        }

        // Calculate 5-year projection
        calculate5YearProjection(tco, vehicle);

        // Save the analysis
        tco = tcoAnalysisRepository.save(tco);
        log.info("TCO analysis saved for vehicle: {} with fuel type: {}", vehicleId, vehicle.getFuelType());

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
            return calculateTCO(vehicleId, defaultProjectionYears);
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
        int errors = 0;

        for (Vehicle vehicle : vehicles) {
            try {
                calculateTCO(vehicle.getId(), defaultProjectionYears);
                count++;
            } catch (Exception e) {
                log.error("Error calculating TCO for vehicle: {}", vehicle.getId(), e);
                errors++;
            }
        }

        log.info("Completed TCO recalculation: {} successful, {} errors", count, errors);
        meterRegistry.gauge("tco.analysis.batch.success", count);
        meterRegistry.gauge("tco.analysis.batch.errors", errors);
    }

    // ==================== Multi-Fuel Comparison Methods ====================

    /**
     * Compare TCO between two vehicles with different fuel types
     */
    @Transactional(readOnly = true)
    public TCOComparisonResponse compareTCO(Long vehicleId1, Long vehicleId2, Integer years) {
        log.info("Comparing TCO between vehicles {} and {}", vehicleId1, vehicleId2);
        
        TCOAnalysisResponse tco1 = getTCOAnalysis(vehicleId1);
        TCOAnalysisResponse tco2 = getTCOAnalysis(vehicleId2);
        
        BigDecimal costDifference = tco1.getTotalCost().subtract(tco2.getTotalCost());
        BigDecimal percentageDifference = BigDecimal.ZERO;
        
        if (tco1.getTotalCost().compareTo(BigDecimal.ZERO) > 0) {
            percentageDifference = costDifference
                    .divide(tco1.getTotalCost(), 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100));
        }
        
        return TCOComparisonResponse.builder()
                .vehicle1(tco1)
                .vehicle2(tco2)
                .costDifference(costDifference)
                .percentageDifference(percentageDifference)
                .cheaperVehicleId(costDifference.compareTo(BigDecimal.ZERO) > 0 ? vehicleId2 : vehicleId1)
                .annualSavings(costDifference.abs().divide(BigDecimal.valueOf(years != null ? years : 5), 2, RoundingMode.HALF_UP))
                .build();
    }

    /**
     * Compare TCO across fuel types for a hypothetical vehicle
     */
    @Transactional(readOnly = true)
    public Map<FuelType, TCOProjectionResponse> compareFuelTypes(
            BigDecimal purchasePrice,
            BigDecimal annualKm,
            Integer years,
            String region) {
        
        log.info("Comparing fuel types for {} km/year over {} years in {}", annualKm, years, region);
        
        Map<FuelType, TCOProjectionResponse> comparisons = new LinkedHashMap<>();
        BigDecimal regionalFactor = REGIONAL_FUEL_ADJUSTMENTS.getOrDefault(
                region != null ? region.toUpperCase() : "DEFAULT", 
                BigDecimal.ONE);
        
        for (FuelType fuelType : FuelType.values()) {
            TCOProjectionResponse projection = calculateFuelTypeProjection(
                    purchasePrice, annualKm, years, fuelType, regionalFactor);
            comparisons.put(fuelType, projection);
        }
        
        return comparisons;
    }

    /**
     * Get 5-year TCO projection for a vehicle
     */
    @Transactional(readOnly = true)
    public TCOProjectionResponse get5YearProjection(Long vehicleId) {
        log.info("Getting 5-year projection for vehicle: {}", vehicleId);
        
        Vehicle vehicle = vehicleRepository.findById(vehicleId)
                .orElseThrow(() -> new ResourceNotFoundException("Vehicle not found with id: " + vehicleId));
        
        Optional<TCOAnalysis> tcoOpt = tcoAnalysisRepository.findLatestByVehicleId(vehicleId);
        
        if (tcoOpt.isEmpty()) {
            calculateTCO(vehicleId, 5);
            tcoOpt = tcoAnalysisRepository.findLatestByVehicleId(vehicleId);
        }
        
        TCOAnalysis tco = tcoOpt.orElseThrow();
        
        BigDecimal costPerKm = BigDecimal.ZERO;
        if (tco.getTotalDistanceKm().compareTo(BigDecimal.ZERO) > 0) {
            costPerKm = tco.getProjected5YrTotalCost()
                    .divide(tco.getTotalDistanceKm().multiply(BigDecimal.valueOf(5)), 4, RoundingMode.HALF_UP);
        }
        
        return TCOProjectionResponse.builder()
                .vehicleId(vehicleId)
                .fuelType(vehicle.getFuelType())
                .projectionYears(5)
                .totalProjectedCost(tco.getProjected5YrTotalCost())
                .projectedEnergyCost(tco.getProjected5YrEnergyCost())
                .projectedMaintenanceCost(tco.getProjected5YrMaintenanceCost())
                .projectedCarbonCost(tco.getProjected5YrCarbonCost())
                .projectedDepreciation(calculateProjectedDepreciation(tco.getPurchasePrice(), 5, vehicle.getFuelType()))
                .costPerKmProjected(costPerKm)
                .build();
    }

    // ==================== Private Calculation Methods ====================

    /**
     * Calculate acquisition costs (purchase price and depreciation)
     */
    private void calculateAcquisitionCosts(TCOAnalysis tco, Vehicle vehicle) {
        if (tco.getPurchasePrice().compareTo(BigDecimal.ZERO) == 0) {
            tco.setPurchasePrice(new BigDecimal("1500000")); // ₹15L default
        }

        LocalDate purchaseDate = vehicle.getCreatedAt() != null 
                ? vehicle.getCreatedAt().toLocalDate() 
                : LocalDate.now().minusYears(1);
        
        long monthsOwned = ChronoUnit.MONTHS.between(purchaseDate, LocalDate.now());
        int yearsOwned = (int) (monthsOwned / 12);
        
        BigDecimal depreciation = BigDecimal.ZERO;
        BigDecimal currentValue = tco.getPurchasePrice();
        
        BigDecimal depreciationFactor = vehicle.getFuelType() == FuelType.EV 
                ? EV_DEPRECIATION_FACTOR : BigDecimal.ONE;
        
        if (yearsOwned >= 1) {
            depreciation = currentValue.multiply(FIRST_YEAR_DEPRECIATION).multiply(depreciationFactor);
            currentValue = currentValue.subtract(depreciation);
            yearsOwned--;
        }
        
        for (int i = 0; i < yearsOwned && i < 10; i++) {
            BigDecimal yearlyDep = currentValue.multiply(SUBSEQUENT_YEAR_DEPRECIATION).multiply(depreciationFactor);
            depreciation = depreciation.add(yearlyDep);
            currentValue = currentValue.subtract(yearlyDep);
        }
        
        tco.setDepreciationValue(depreciation);
        tco.setCurrentValue(currentValue);
    }

    /**
     * Calculate operating costs based on fuel type
     */
    private void calculateOperatingCostsByFuelType(TCOAnalysis tco, Vehicle vehicle) {
        FuelType fuelType = vehicle.getFuelType();
        if (fuelType == null) {
            fuelType = FuelType.ICE;
        }
        
        List<Trip> trips = tripRepository.findByVehicleId(vehicle.getId());
        double totalDistance = trips.stream()
                .mapToDouble(t -> t.getDistance() != null ? t.getDistance() : 0.0)
                .sum();
        
        tco.setTotalDistanceKm(BigDecimal.valueOf(totalDistance));
        
        BigDecimal energyCosts = calculateEnergyCostsByFuelType(fuelType, vehicle, totalDistance);
        tco.setEnergyCosts(energyCosts);

        BigDecimal baseMaintenanceCost = calculateBaseMaintenanceCosts(vehicle);
        BigDecimal maintenanceFactor = getMaintenanceFactor(fuelType);
        tco.setMaintenanceCosts(baseMaintenanceCost.multiply(maintenanceFactor).setScale(2, RoundingMode.HALF_UP));

        calculateInsuranceCosts(tco, vehicle);
        calculateTaxesAndFees(tco, vehicle);

        if (tco.getRegionCode() != null) {
            BigDecimal factor = REGIONAL_FUEL_ADJUSTMENTS.getOrDefault(
                    tco.getRegionCode().toUpperCase(), BigDecimal.ONE);
            tco.setRegionalAdjustmentFactor(factor);
            tco.applyRegionalAdjustment();
        }

        tco.calculateTotalCost();
    }

    /**
     * Calculate energy costs based on fuel type
     */
    private BigDecimal calculateEnergyCostsByFuelType(FuelType fuelType, Vehicle vehicle, double totalDistance) {
        return switch (fuelType) {
            case EV -> calculateEVEnergyCosts(vehicle, totalDistance);
            case HYBRID -> PETROL_FUEL_COST_PER_KM.multiply(HYBRID_FUEL_EFFICIENCY_FACTOR).multiply(BigDecimal.valueOf(totalDistance));
            case CNG -> BigDecimal.valueOf(totalDistance).divide(CNG_EFFICIENCY_KM_PER_KG, 2, RoundingMode.HALF_UP).multiply(CNG_COST_PER_KG);
            case LPG -> BigDecimal.valueOf(totalDistance).divide(LPG_EFFICIENCY_KM_PER_L, 2, RoundingMode.HALF_UP).multiply(LPG_COST_PER_LITER);
            case DIESEL -> DIESEL_FUEL_COST_PER_KM.multiply(BigDecimal.valueOf(totalDistance));
            default -> PETROL_FUEL_COST_PER_KM.multiply(BigDecimal.valueOf(totalDistance));
        };
    }

    /**
     * Calculate EV energy costs from charging sessions
     */
    private BigDecimal calculateEVEnergyCosts(Vehicle vehicle, double totalDistance) {
        List<ChargingSession> sessions = chargingSessionRepository.findByVehicleId(vehicle.getId());
        
        BigDecimal actualCost = sessions.stream()
                .filter(s -> s.getTotalCost() != null)
                .map(ChargingSession::getTotalCost)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        if (actualCost.compareTo(BigDecimal.ZERO) == 0 && totalDistance > 0) {
            actualCost = EV_EFFICIENCY_KWH_PER_KM
                    .multiply(BigDecimal.valueOf(totalDistance))
                    .multiply(EV_ENERGY_COST_PER_KWH);
        }
        
        return actualCost;
    }

    /**
     * Get maintenance cost factor by fuel type
     */
    private BigDecimal getMaintenanceFactor(FuelType fuelType) {
        return switch (fuelType) {
            case EV -> EV_MAINTENANCE_COST_FACTOR;
            case HYBRID -> HYBRID_MAINTENANCE_COST_FACTOR;
            case CNG -> CNG_MAINTENANCE_COST_FACTOR;
            case LPG -> LPG_MAINTENANCE_COST_FACTOR;
            default -> BigDecimal.ONE;
        };
    }

    /**
     * Calculate base maintenance costs from records
     */
    private BigDecimal calculateBaseMaintenanceCosts(Vehicle vehicle) {
        List<MaintenanceRecord> records = maintenanceRecordRepository.findByVehicleId(vehicle.getId());
        
        return records.stream()
                .filter(m -> m.getActualCost() != null)
                .map(MaintenanceRecord::getActualCost)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * Calculate carbon emissions and costs
     */
    private void calculateCarbonCosts(TCOAnalysis tco, Vehicle vehicle) {
        if (!includeCarbonCost) {
            return;
        }
        
        BigDecimal totalDistanceKm = tco.getTotalDistanceKm();
        if (totalDistanceKm.compareTo(BigDecimal.ZERO) == 0) {
            return;
        }
        
        FuelType fuelType = vehicle.getFuelType() != null ? vehicle.getFuelType() : FuelType.ICE;
        BigDecimal carbonEmissionsKg = calculateCarbonEmissions(fuelType, totalDistanceKm);
        
        tco.setCarbonEmissionsKg(carbonEmissionsKg);
        tco.setCarbonCost(carbonEmissionsKg.multiply(CARBON_COST_PER_KG).setScale(2, RoundingMode.HALF_UP));
        
        log.debug("Carbon emissions for vehicle {}: {} kg CO2, cost: {}", 
                vehicle.getId(), carbonEmissionsKg, tco.getCarbonCost());
    }

    /**
     * Calculate carbon emissions based on fuel type and distance
     */
    private BigDecimal calculateCarbonEmissions(FuelType fuelType, BigDecimal distanceKm) {
        return switch (fuelType) {
            case EV -> {
                BigDecimal kwhUsed = distanceKm.multiply(EV_EFFICIENCY_KWH_PER_KM);
                yield kwhUsed.multiply(ELECTRICITY_CO2_PER_KWH);
            }
            case HYBRID -> {
                BigDecimal litersUsed = distanceKm.divide(new BigDecimal("15"), 2, RoundingMode.HALF_UP);
                yield litersUsed.multiply(PETROL_CO2_PER_LITER).multiply(HYBRID_FUEL_EFFICIENCY_FACTOR);
            }
            case CNG -> {
                BigDecimal kgUsed = distanceKm.divide(CNG_EFFICIENCY_KM_PER_KG, 2, RoundingMode.HALF_UP);
                yield kgUsed.multiply(CNG_CO2_PER_KG);
            }
            case LPG -> {
                BigDecimal litersUsed = distanceKm.divide(LPG_EFFICIENCY_KM_PER_L, 2, RoundingMode.HALF_UP);
                yield litersUsed.multiply(LPG_CO2_PER_LITER);
            }
            case DIESEL -> {
                BigDecimal litersUsed = distanceKm.divide(new BigDecimal("18"), 2, RoundingMode.HALF_UP);
                yield litersUsed.multiply(DIESEL_CO2_PER_LITER);
            }
            default -> {
                BigDecimal litersUsed = distanceKm.divide(new BigDecimal("15"), 2, RoundingMode.HALF_UP);
                yield litersUsed.multiply(PETROL_CO2_PER_LITER);
            }
        };
    }

    /**
     * Calculate insurance costs
     */
    private void calculateInsuranceCosts(TCOAnalysis tco, Vehicle vehicle) {
        if (tco.getInsuranceCosts().compareTo(BigDecimal.ZERO) == 0) {
            LocalDate purchaseDate = vehicle.getCreatedAt() != null 
                    ? vehicle.getCreatedAt().toLocalDate() 
                    : LocalDate.now().minusYears(1);
            long monthsOwned = ChronoUnit.MONTHS.between(purchaseDate, LocalDate.now());
            
            BigDecimal annualInsurance = vehicle.getFuelType() == FuelType.EV 
                    ? new BigDecimal("45000")
                    : new BigDecimal("35000");
            
            BigDecimal totalInsurance = annualInsurance
                    .multiply(BigDecimal.valueOf(monthsOwned))
                    .divide(BigDecimal.valueOf(12), 2, RoundingMode.HALF_UP);
            tco.setInsuranceCosts(totalInsurance);
        }
    }

    /**
     * Calculate taxes and fees
     */
    private void calculateTaxesAndFees(TCOAnalysis tco, Vehicle vehicle) {
        if (tco.getTaxesFees().compareTo(BigDecimal.ZERO) == 0) {
            BigDecimal annualTax = vehicle.getFuelType() == FuelType.EV 
                    ? new BigDecimal("5000")
                    : new BigDecimal("15000");
            tco.setTaxesFees(annualTax);
        }
    }

    /**
     * Calculate metrics (cost per km, cost per year)
     */
    private void calculateMetrics(TCOAnalysis tco, Vehicle vehicle) {
        tco.calculateCostPerKm();
        tco.calculateCostPerYear();
    }

    /**
     * Calculate ICE comparison for non-ICE vehicles
     */
    private void calculateICEComparison(TCOAnalysis tco, Vehicle vehicle) {
        BigDecimal totalDistance = tco.getTotalDistanceKm();
        
        if (totalDistance.compareTo(BigDecimal.ZERO) == 0) {
            return;
        }

        BigDecimal iceFuelCost = PETROL_FUEL_COST_PER_KM.multiply(totalDistance);
        BigDecimal iceMaintenanceCost = ICE_MAINTENANCE_COST_PER_KM.multiply(totalDistance);

        BigDecimal fuelSavings = iceFuelCost.subtract(tco.getEnergyCosts());
        tco.setIceFuelSavings(fuelSavings);

        BigDecimal maintenanceSavings = iceMaintenanceCost.subtract(tco.getMaintenanceCosts());
        tco.setIceMaintenanceSavings(maintenanceSavings);

        tco.calculateIceTotalSavings();

        BigDecimal evPremium = FUEL_TYPE_PREMIUMS.getOrDefault(vehicle.getFuelType(), BigDecimal.ZERO);
        
        if (tco.getIceTotalSavings().compareTo(BigDecimal.ZERO) > 0 && tco.getAnalysisPeriodYears() > 0) {
            BigDecimal annualSavings = tco.getIceTotalSavings()
                    .divide(BigDecimal.valueOf(tco.getAnalysisPeriodYears()), 2, RoundingMode.HALF_UP);
            
            if (annualSavings.compareTo(BigDecimal.ZERO) > 0) {
                BigDecimal paybackYears = evPremium.divide(annualSavings, 2, RoundingMode.HALF_UP);
                int paybackMonths = paybackYears.multiply(BigDecimal.valueOf(12)).intValue();
                tco.setIcePaybackPeriodMonths(paybackMonths);
            }
        }
    }

    /**
     * Calculate 5-year TCO projection
     */
    private void calculate5YearProjection(TCOAnalysis tco, Vehicle vehicle) {
        int years = 5;
        BigDecimal annualDistance = tco.getTotalDistanceKm().compareTo(BigDecimal.ZERO) > 0
                ? tco.getTotalDistanceKm().divide(BigDecimal.valueOf(tco.getAnalysisPeriodYears() > 0 ? tco.getAnalysisPeriodYears() : 1), 2, RoundingMode.HALF_UP)
                : new BigDecimal("15000");
        
        BigDecimal projectedEnergyCost = BigDecimal.ZERO;
        BigDecimal annualEnergyCost = tco.getEnergyCosts().divide(
                BigDecimal.valueOf(tco.getAnalysisPeriodYears() > 0 ? tco.getAnalysisPeriodYears() : 1), 2, RoundingMode.HALF_UP);
        
        for (int i = 0; i < years; i++) {
            BigDecimal inflationFactor = BigDecimal.ONE.add(new BigDecimal("0.05")).pow(i);
            projectedEnergyCost = projectedEnergyCost.add(annualEnergyCost.multiply(inflationFactor));
        }
        tco.setProjected5YrEnergyCost(projectedEnergyCost.setScale(2, RoundingMode.HALF_UP));
        
        BigDecimal projectedMaintenanceCost = BigDecimal.ZERO;
        BigDecimal annualMaintenanceCost = tco.getMaintenanceCosts().divide(
                BigDecimal.valueOf(tco.getAnalysisPeriodYears() > 0 ? tco.getAnalysisPeriodYears() : 1), 2, RoundingMode.HALF_UP);
        
        for (int i = 0; i < years; i++) {
            BigDecimal ageFactor = BigDecimal.ONE.add(new BigDecimal("0.03").multiply(BigDecimal.valueOf(i)));
            projectedMaintenanceCost = projectedMaintenanceCost.add(annualMaintenanceCost.multiply(ageFactor));
        }
        tco.setProjected5YrMaintenanceCost(projectedMaintenanceCost.setScale(2, RoundingMode.HALF_UP));
        
        BigDecimal projectedCarbonCost = tco.getCarbonCost().multiply(BigDecimal.valueOf(years));
        tco.setProjected5YrCarbonCost(projectedCarbonCost.setScale(2, RoundingMode.HALF_UP));
        
        BigDecimal projectedDepreciation = calculateProjectedDepreciation(tco.getPurchasePrice(), years, vehicle.getFuelType());
        BigDecimal projectedInsurance = tco.getInsuranceCosts().multiply(BigDecimal.valueOf(years));
        BigDecimal projectedTaxes = tco.getTaxesFees().multiply(BigDecimal.valueOf(years));
        
        BigDecimal total5YrCost = tco.getPurchasePrice()
                .subtract(projectedDepreciation)
                .add(projectedEnergyCost)
                .add(projectedMaintenanceCost)
                .add(projectedInsurance)
                .add(projectedTaxes)
                .add(projectedCarbonCost);
        
        tco.setProjected5YrTotalCost(total5YrCost.setScale(2, RoundingMode.HALF_UP));
    }

    /**
     * Calculate projected depreciation over years
     */
    private BigDecimal calculateProjectedDepreciation(BigDecimal purchasePrice, int years, FuelType fuelType) {
        BigDecimal currentValue = purchasePrice;
        BigDecimal totalDepreciation = BigDecimal.ZERO;
        
        BigDecimal depreciationFactor = fuelType == FuelType.EV ? EV_DEPRECIATION_FACTOR : BigDecimal.ONE;
        
        for (int i = 0; i < years; i++) {
            BigDecimal rate = i == 0 ? FIRST_YEAR_DEPRECIATION : SUBSEQUENT_YEAR_DEPRECIATION;
            BigDecimal yearlyDep = currentValue.multiply(rate).multiply(depreciationFactor);
            totalDepreciation = totalDepreciation.add(yearlyDep);
            currentValue = currentValue.subtract(yearlyDep);
        }
        
        return totalDepreciation.setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * Calculate fuel type projection for comparison
     */
    private TCOProjectionResponse calculateFuelTypeProjection(
            BigDecimal purchasePrice,
            BigDecimal annualKm,
            Integer years,
            FuelType fuelType,
            BigDecimal regionalFactor) {
        
        int projectionYears = years != null ? years : 5;
        BigDecimal totalDistance = annualKm.multiply(BigDecimal.valueOf(projectionYears));
        
        BigDecimal adjustedPrice = purchasePrice.add(
                FUEL_TYPE_PREMIUMS.getOrDefault(fuelType, BigDecimal.ZERO));
        
        BigDecimal energyCost = calculateEnergyCostsByFuelType(fuelType, null, totalDistance.doubleValue());
        energyCost = energyCost.multiply(regionalFactor);
        
        BigDecimal maintenanceCost = ICE_MAINTENANCE_COST_PER_KM
                .multiply(totalDistance)
                .multiply(getMaintenanceFactor(fuelType));
        
        BigDecimal depreciation = calculateProjectedDepreciation(adjustedPrice, projectionYears, fuelType);
        
        BigDecimal carbonEmissions = calculateCarbonEmissions(fuelType, totalDistance);
        BigDecimal carbonCost = carbonEmissions.multiply(CARBON_COST_PER_KG);
        
        BigDecimal totalCost = adjustedPrice
                .subtract(adjustedPrice.subtract(depreciation))
                .add(energyCost)
                .add(maintenanceCost)
                .add(carbonCost);
        
        return TCOProjectionResponse.builder()
                .fuelType(fuelType)
                .projectionYears(projectionYears)
                .totalProjectedCost(totalCost.setScale(2, RoundingMode.HALF_UP))
                .projectedEnergyCost(energyCost.setScale(2, RoundingMode.HALF_UP))
                .projectedMaintenanceCost(maintenanceCost.setScale(2, RoundingMode.HALF_UP))
                .projectedCarbonCost(carbonCost.setScale(2, RoundingMode.HALF_UP))
                .projectedDepreciation(depreciation)
                .carbonEmissionsKg(carbonEmissions.setScale(2, RoundingMode.HALF_UP))
                .costPerKmProjected(totalCost.divide(totalDistance, 4, RoundingMode.HALF_UP))
                .build();
    }

    /**
     * Convert TCOAnalysis entity to response DTO
     */
    private TCOAnalysisResponse convertToResponse(TCOAnalysis tco, Vehicle vehicle) {
        TCOAnalysisResponse.ComparisonWithICE comparison = null;
        
        if (vehicle.getFuelType() != FuelType.ICE && 
            tco.getIceTotalSavings() != null && 
            tco.getIceTotalSavings().compareTo(BigDecimal.ZERO) > 0) {
            
            BigDecimal iceTotalCost = tco.getTotalCost().add(tco.getIceTotalSavings());
            Double savingsPercentage = BigDecimal.ZERO.compareTo(iceTotalCost) == 0 ? 0.0 :
                    tco.getIceTotalSavings()
                            .divide(iceTotalCost, 4, RoundingMode.HALF_UP)
                            .multiply(BigDecimal.valueOf(100))
                            .doubleValue();
            
            comparison = TCOAnalysisResponse.ComparisonWithICE.builder()
                    .fuelSavings(tco.getIceFuelSavings())
                    .maintenanceSavings(tco.getIceMaintenanceSavings())
                    .totalSavings(tco.getIceTotalSavings())
                    .paybackPeriodMonths(tco.getIcePaybackPeriodMonths())
                    .savingsPercentage(savingsPercentage)
                    .build();
        }

        String vehicleName = vehicle.getMake() + " " + vehicle.getModel();
        
        int ageMonths = 0;
        if (vehicle.getCreatedAt() != null) {
            ageMonths = (int) ChronoUnit.MONTHS.between(vehicle.getCreatedAt().toLocalDate(), LocalDate.now());
        }

        return TCOAnalysisResponse.builder()
                .id(tco.getId())
                .vehicleId(vehicle.getId())
                .vehicleName(vehicleName)
                .vehicleNumber(vehicle.getVehicleNumber())
                .fuelType(vehicle.getFuelType())
                .analysisDate(tco.getAnalysisDate())
                .purchasePrice(tco.getPurchasePrice())
                .depreciation(tco.getDepreciationValue())
                .currentValue(tco.getCurrentValue())
                .ageMonths(ageMonths)
                .energyCosts(tco.getEnergyCosts())
                .maintenanceCosts(tco.getMaintenanceCosts())
                .insuranceCosts(tco.getInsuranceCosts())
                .taxesFees(tco.getTaxesFees())
                .otherCosts(tco.getOtherCosts())
                .carbonEmissionsKg(tco.getCarbonEmissionsKg())
                .carbonCost(tco.getCarbonCost())
                .totalCost(tco.getTotalCost())
                .costPerKm(tco.getCostPerKm())
                .costPerYear(tco.getCostPerYear())
                .analysisPeriodYears(tco.getAnalysisPeriodYears())
                .totalDistanceKm(tco.getTotalDistanceKm())
                .projected5YrTotalCost(tco.getProjected5YrTotalCost())
                .projected5YrEnergyCost(tco.getProjected5YrEnergyCost())
                .projected5YrMaintenanceCost(tco.getProjected5YrMaintenanceCost())
                .projected5YrCarbonCost(tco.getProjected5YrCarbonCost())
                .comparisonWithICE(comparison)
                .build();
    }

    // ==================== Inner Classes ====================

    /**
     * TCO Comparison Response
     */
    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class TCOComparisonResponse {
        private TCOAnalysisResponse vehicle1;
        private TCOAnalysisResponse vehicle2;
        private BigDecimal costDifference;
        private BigDecimal percentageDifference;
        private Long cheaperVehicleId;
        private BigDecimal annualSavings;
    }
}
