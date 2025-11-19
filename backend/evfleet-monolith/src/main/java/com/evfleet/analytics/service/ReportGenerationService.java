package com.evfleet.analytics.service;

import com.evfleet.analytics.dto.VehicleReportRequest;
import com.evfleet.charging.model.ChargingSession;
import com.evfleet.charging.repository.ChargingSessionRepository;
import com.evfleet.common.exception.ResourceNotFoundException;
import com.evfleet.fleet.model.BatteryHealth;
import com.evfleet.fleet.model.Trip;
import com.evfleet.fleet.model.Vehicle;
import com.evfleet.fleet.repository.BatteryHealthRepository;
import com.evfleet.fleet.repository.TripRepository;
import com.evfleet.fleet.repository.VehicleRepository;
import com.evfleet.maintenance.model.MaintenanceRecord;
import com.evfleet.maintenance.repository.MaintenanceRecordRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Report Generation Service
 * Handles PDF report generation for vehicles
 *
 * @author SEV Platform Team
 * @version 1.0.0
 */
@Service
@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReportGenerationService {

    private final VehicleRepository vehicleRepository;
    private final TripRepository tripRepository;
    private final MaintenanceRecordRepository maintenanceRecordRepository;
    private final ChargingSessionRepository chargingSessionRepository;
    private final BatteryHealthRepository batteryHealthRepository;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    private static final DateTimeFormatter DATE_ONLY_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    /**
     * Generate comprehensive vehicle report
     */
    public byte[] generateVehicleReport(VehicleReportRequest request) throws IOException {
        log.info("Generating vehicle report for vehicle: {}", request.getVehicleId());

        Vehicle vehicle = vehicleRepository.findById(request.getVehicleId())
                .orElseThrow(() -> new ResourceNotFoundException("Vehicle not found with id: " + request.getVehicleId()));

        try (PDDocument document = new PDDocument()) {
            // Add title page
            addTitlePage(document, vehicle, request);

            // Add sections based on request
            if (request.isIncludeVehicleInfo()) {
                addVehicleInfoSection(document, vehicle);
            }

            if (request.isIncludeTripHistory()) {
                List<Trip> trips = tripRepository.findByVehicleIdAndStartTimeBetween(
                        vehicle.getId(), request.getStartDate(), request.getEndDate());
                addTripHistorySection(document, trips);
            }

            if (request.isIncludeMaintenanceHistory()) {
                List<MaintenanceRecord> maintenanceRecords = maintenanceRecordRepository
                        .findByVehicleIdAndScheduledDateBetween(
                                vehicle.getId(), 
                                request.getStartDate().toLocalDate(), 
                                request.getEndDate().toLocalDate());
                addMaintenanceHistorySection(document, maintenanceRecords);
            }

            if (request.isIncludeChargingHistory()) {
                List<ChargingSession> chargingSessions = chargingSessionRepository
                        .findByVehicleIdAndStartTimeBetween(
                                vehicle.getId(), request.getStartDate(), request.getEndDate());
                addChargingHistorySection(document, chargingSessions);
            }

            if (request.isIncludePerformanceMetrics()) {
                addPerformanceMetricsSection(document, vehicle, request);
            }

            if (request.isIncludeCostAnalysis()) {
                addCostAnalysisSection(document, vehicle, request);
            }

            // Convert to byte array
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            document.save(baos);
            log.info("Vehicle report generated successfully for vehicle: {}", request.getVehicleId());
            return baos.toByteArray();
        }
    }

    /**
     * Generate genealogy report (event timeline)
     */
    public byte[] generateGenealogyReport(Long vehicleId, LocalDateTime startDate, LocalDateTime endDate) throws IOException {
        log.info("Generating genealogy report for vehicle: {}", vehicleId);

        Vehicle vehicle = vehicleRepository.findById(vehicleId)
                .orElseThrow(() -> new ResourceNotFoundException("Vehicle not found with id: " + vehicleId));

        try (PDDocument document = new PDDocument()) {
            // Title page
            PDPage titlePage = new PDPage(PDRectangle.A4);
            document.addPage(titlePage);

            try (PDPageContentStream contentStream = new PDPageContentStream(document, titlePage)) {
                contentStream.setFont(PDType1Font.HELVETICA_BOLD, 20);
                contentStream.beginText();
                contentStream.newLineAtOffset(50, 750);
                contentStream.showText("Vehicle Genealogy Report");
                contentStream.endText();

                contentStream.setFont(PDType1Font.HELVETICA, 12);
                contentStream.beginText();
                contentStream.newLineAtOffset(50, 720);
                contentStream.showText("Vehicle: " + vehicle.getMake() + " " + vehicle.getModel());
                contentStream.endText();

                contentStream.beginText();
                contentStream.newLineAtOffset(50, 700);
                contentStream.showText("VIN: " + (vehicle.getVehicleNumber() != null ? vehicle.getVehicleNumber() : "N/A"));
                contentStream.endText();

                contentStream.beginText();
                contentStream.newLineAtOffset(50, 680);
                contentStream.showText("Period: " + startDate.format(DATE_FORMATTER) + " to " + endDate.format(DATE_FORMATTER));
                contentStream.endText();
            }

            // Event timeline
            addEventTimelineSection(document, vehicle, startDate, endDate);

            // Convert to byte array
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            document.save(baos);
            log.info("Genealogy report generated successfully for vehicle: {}", vehicleId);
            return baos.toByteArray();
        }
    }

    /**
     * Add title page to the report
     */
    private void addTitlePage(PDDocument document, Vehicle vehicle, VehicleReportRequest request) throws IOException {
        PDPage page = new PDPage(PDRectangle.A4);
        document.addPage(page);

        try (PDPageContentStream contentStream = new PDPageContentStream(document, page)) {
            contentStream.setFont(PDType1Font.HELVETICA_BOLD, 24);
            contentStream.beginText();
            contentStream.newLineAtOffset(50, 750);
            contentStream.showText("Vehicle Report");
            contentStream.endText();

            contentStream.setFont(PDType1Font.HELVETICA, 14);
            contentStream.beginText();
            contentStream.newLineAtOffset(50, 710);
            contentStream.showText("Vehicle: " + vehicle.getMake() + " " + vehicle.getModel());
            contentStream.endText();

            contentStream.beginText();
            contentStream.newLineAtOffset(50, 685);
            contentStream.showText("VIN: " + (vehicle.getVehicleNumber() != null ? vehicle.getVehicleNumber() : "N/A"));
            contentStream.endText();

            contentStream.beginText();
            contentStream.newLineAtOffset(50, 660);
            contentStream.showText("Report Period: " + 
                    request.getStartDate().format(DATE_FORMATTER) + " to " + 
                    request.getEndDate().format(DATE_FORMATTER));
            contentStream.endText();

            contentStream.beginText();
            contentStream.newLineAtOffset(50, 635);
            contentStream.showText("Generated: " + LocalDateTime.now().format(DATE_FORMATTER));
            contentStream.endText();
        }
    }

    /**
     * Add vehicle information section
     */
    private void addVehicleInfoSection(PDDocument document, Vehicle vehicle) throws IOException {
        PDPage page = new PDPage(PDRectangle.A4);
        document.addPage(page);

        try (PDPageContentStream contentStream = new PDPageContentStream(document, page)) {
            contentStream.setFont(PDType1Font.HELVETICA_BOLD, 16);
            contentStream.beginText();
            contentStream.newLineAtOffset(50, 750);
            contentStream.showText("Vehicle Information");
            contentStream.endText();

            contentStream.setFont(PDType1Font.HELVETICA, 12);
            int yPosition = 720;

            addTextField(contentStream, "Make:", vehicle.getMake(), yPosition);
            yPosition -= 25;
            addTextField(contentStream, "Model:", vehicle.getModel(), yPosition);
            yPosition -= 25;
            addTextField(contentStream, "Year:", String.valueOf(vehicle.getYear()), yPosition);
            yPosition -= 25;
            addTextField(contentStream, "VIN:", vehicle.getVehicleNumber(), yPosition);
            yPosition -= 25;
            addTextField(contentStream, "Fuel Type:", vehicle.getFuelType().toString(), yPosition);
            yPosition -= 25;
            addTextField(contentStream, "Status:", vehicle.getStatus().toString(), yPosition);
            yPosition -= 25;

            if (vehicle.getBatteryCapacity() != null) {
                addTextField(contentStream, "Battery Capacity:", vehicle.getBatteryCapacity() + " kWh", yPosition);
                yPosition -= 25;
            }
        }
    }

    /**
     * Add trip history section
     */
    private void addTripHistorySection(PDDocument document, List<Trip> trips) throws IOException {
        PDPage page = new PDPage(PDRectangle.A4);
        document.addPage(page);

        try (PDPageContentStream contentStream = new PDPageContentStream(document, page)) {
            contentStream.setFont(PDType1Font.HELVETICA_BOLD, 16);
            contentStream.beginText();
            contentStream.newLineAtOffset(50, 750);
            contentStream.showText("Trip History");
            contentStream.endText();

            contentStream.setFont(PDType1Font.HELVETICA, 10);
            int yPosition = 720;

            addTextField(contentStream, "Total Trips:", String.valueOf(trips.size()), yPosition);
            yPosition -= 20;

            double totalDistance = trips.stream()
                    .mapToDouble(t -> t.getDistance() != null ? t.getDistance() : 0.0)
                    .sum();
            addTextField(contentStream, "Total Distance:", String.format("%.2f km", totalDistance), yPosition);
            yPosition -= 20;

            BigDecimal totalEnergy = trips.stream()
                    .map(t -> t.getEnergyConsumed() != null ? t.getEnergyConsumed() : BigDecimal.ZERO)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            addTextField(contentStream, "Total Energy:", String.format("%.2f kWh", totalEnergy), yPosition);
            yPosition -= 30;

            // List recent trips (max 15)
            contentStream.setFont(PDType1Font.HELVETICA_BOLD, 12);
            contentStream.beginText();
            contentStream.newLineAtOffset(50, yPosition);
            contentStream.showText("Recent Trips:");
            contentStream.endText();
            yPosition -= 20;

            contentStream.setFont(PDType1Font.HELVETICA, 9);
            int count = 0;
            for (Trip trip : trips) {
                if (count >= 15 || yPosition < 100) break;

                String tripInfo = String.format("%s | %.1f km | %.2f kWh", 
                        trip.getStartTime().format(DATE_FORMATTER),
                        trip.getDistance() != null ? trip.getDistance() : 0.0,
                        trip.getEnergyConsumed() != null ? trip.getEnergyConsumed() : BigDecimal.ZERO);

                contentStream.beginText();
                contentStream.newLineAtOffset(50, yPosition);
                contentStream.showText(tripInfo);
                contentStream.endText();
                yPosition -= 15;
                count++;
            }
        }
    }

    /**
     * Add maintenance history section
     */
    private void addMaintenanceHistorySection(PDDocument document, List<MaintenanceRecord> records) throws IOException {
        PDPage page = new PDPage(PDRectangle.A4);
        document.addPage(page);

        try (PDPageContentStream contentStream = new PDPageContentStream(document, page)) {
            contentStream.setFont(PDType1Font.HELVETICA_BOLD, 16);
            contentStream.beginText();
            contentStream.newLineAtOffset(50, 750);
            contentStream.showText("Maintenance History");
            contentStream.endText();

            contentStream.setFont(PDType1Font.HELVETICA, 10);
            int yPosition = 720;

            addTextField(contentStream, "Total Records:", String.valueOf(records.size()), yPosition);
            yPosition -= 20;

            BigDecimal totalCost = records.stream()
                    .map(m -> m.getCost() != null ? m.getCost() : BigDecimal.ZERO)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            addTextField(contentStream, "Total Cost:", String.format("$%.2f", totalCost), yPosition);
            yPosition -= 30;

            // List maintenance records
            contentStream.setFont(PDType1Font.HELVETICA_BOLD, 12);
            contentStream.beginText();
            contentStream.newLineAtOffset(50, yPosition);
            contentStream.showText("Maintenance Records:");
            contentStream.endText();
            yPosition -= 20;

            contentStream.setFont(PDType1Font.HELVETICA, 9);
            for (MaintenanceRecord record : records) {
                if (yPosition < 100) break;

                String recordInfo = String.format("%s | %s | $%.2f", 
                        record.getScheduledDate().format(DATE_ONLY_FORMATTER),
                        record.getDescription() != null ? record.getDescription() : "N/A",
                        record.getCost() != null ? record.getCost() : BigDecimal.ZERO);

                contentStream.beginText();
                contentStream.newLineAtOffset(50, yPosition);
                contentStream.showText(recordInfo);
                contentStream.endText();
                yPosition -= 15;
            }
        }
    }

    /**
     * Add charging history section
     */
    private void addChargingHistorySection(PDDocument document, List<ChargingSession> sessions) throws IOException {
        PDPage page = new PDPage(PDRectangle.A4);
        document.addPage(page);

        try (PDPageContentStream contentStream = new PDPageContentStream(document, page)) {
            contentStream.setFont(PDType1Font.HELVETICA_BOLD, 16);
            contentStream.beginText();
            contentStream.newLineAtOffset(50, 750);
            contentStream.showText("Charging History");
            contentStream.endText();

            contentStream.setFont(PDType1Font.HELVETICA, 10);
            int yPosition = 720;

            addTextField(contentStream, "Total Sessions:", String.valueOf(sessions.size()), yPosition);
            yPosition -= 20;

            BigDecimal totalEnergy = sessions.stream()
                    .map(s -> s.getEnergyConsumed() != null ? s.getEnergyConsumed() : BigDecimal.ZERO)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            addTextField(contentStream, "Total Energy:", String.format("%.2f kWh", totalEnergy), yPosition);
            yPosition -= 20;

            BigDecimal totalCost = sessions.stream()
                    .map(s -> s.getCost() != null ? s.getCost() : BigDecimal.ZERO)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            addTextField(contentStream, "Total Cost:", String.format("$%.2f", totalCost), yPosition);
            yPosition -= 30;

            // List charging sessions
            contentStream.setFont(PDType1Font.HELVETICA_BOLD, 12);
            contentStream.beginText();
            contentStream.newLineAtOffset(50, yPosition);
            contentStream.showText("Charging Sessions:");
            contentStream.endText();
            yPosition -= 20;

            contentStream.setFont(PDType1Font.HELVETICA, 9);
            int count = 0;
            for (ChargingSession session : sessions) {
                if (count >= 20 || yPosition < 100) break;

                String sessionInfo = String.format("%s | %.2f kWh | $%.2f", 
                        session.getStartTime().format(DATE_FORMATTER),
                        session.getEnergyConsumed() != null ? session.getEnergyConsumed() : BigDecimal.ZERO,
                        session.getCost() != null ? session.getCost() : BigDecimal.ZERO);

                contentStream.beginText();
                contentStream.newLineAtOffset(50, yPosition);
                contentStream.showText(sessionInfo);
                contentStream.endText();
                yPosition -= 15;
                count++;
            }
        }
    }

    /**
     * Add performance metrics section
     */
    private void addPerformanceMetricsSection(PDDocument document, Vehicle vehicle, VehicleReportRequest request) throws IOException {
        PDPage page = new PDPage(PDRectangle.A4);
        document.addPage(page);

        try (PDPageContentStream contentStream = new PDPageContentStream(document, page)) {
            contentStream.setFont(PDType1Font.HELVETICA_BOLD, 16);
            contentStream.beginText();
            contentStream.newLineAtOffset(50, 750);
            contentStream.showText("Performance Metrics");
            contentStream.endText();

            contentStream.setFont(PDType1Font.HELVETICA, 12);
            int yPosition = 720;

            // Get battery health
            BatteryHealth latestBatteryHealth = batteryHealthRepository
                    .findFirstByVehicleIdOrderByRecordedAtDesc(vehicle.getId())
                    .orElse(null);

            if (latestBatteryHealth != null) {
                addTextField(contentStream, "Battery SOC:", String.format("%.1f%%", latestBatteryHealth.getCurrentSoc()), yPosition);
                yPosition -= 25;
                addTextField(contentStream, "Battery SOH:", String.format("%.1f%%", latestBatteryHealth.getSoh()), yPosition);
                yPosition -= 25;
            }


            // Calculate average efficiency
            List<Trip> trips = tripRepository.findByVehicleIdAndStartTimeBetween(
                    vehicle.getId(), request.getStartDate(), request.getEndDate());

            if (!trips.isEmpty()) {
                double totalDistance = trips.stream()
                        .mapToDouble(t -> t.getDistance() != null ? t.getDistance() : 0.0)
                        .sum();

                BigDecimal totalEnergy = trips.stream()
                        .map(t -> t.getEnergyConsumed() != null ? t.getEnergyConsumed() : BigDecimal.ZERO)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);

                if (totalDistance > 0) {
                    double efficiency = totalEnergy.doubleValue() * 100 / totalDistance;
                    addTextField(contentStream, "Avg Efficiency:", String.format("%.2f kWh/100km", efficiency), yPosition);
                }
            }
        }
    }

    /**
     * Add cost analysis section
     */
    private void addCostAnalysisSection(PDDocument document, Vehicle vehicle, VehicleReportRequest request) throws IOException {
        PDPage page = new PDPage(PDRectangle.A4);
        document.addPage(page);

        try (PDPageContentStream contentStream = new PDPageContentStream(document, page)) {
            contentStream.setFont(PDType1Font.HELVETICA_BOLD, 16);
            contentStream.beginText();
            contentStream.newLineAtOffset(50, 750);
            contentStream.showText("Cost Analysis");
            contentStream.endText();

            contentStream.setFont(PDType1Font.HELVETICA, 12);
            int yPosition = 720;

            // Get charging costs
            List<ChargingSession> sessions = chargingSessionRepository
                    .findByVehicleIdAndStartTimeBetween(vehicle.getId(), request.getStartDate(), request.getEndDate());

            BigDecimal energyCost = sessions.stream()
                    .map(s -> s.getCost() != null ? s.getCost() : BigDecimal.ZERO)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            addTextField(contentStream, "Energy Costs:", String.format("$%.2f", energyCost), yPosition);
            yPosition -= 25;

            // Get maintenance costs
            List<MaintenanceRecord> records = maintenanceRecordRepository
                    .findByVehicleIdAndScheduledDateBetween(
                            vehicle.getId(), 
                            request.getStartDate().toLocalDate(), 
                            request.getEndDate().toLocalDate());

            BigDecimal maintenanceCost = records.stream()
                    .map(m -> m.getCost() != null ? m.getCost() : BigDecimal.ZERO)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            addTextField(contentStream, "Maintenance Costs:", String.format("$%.2f", maintenanceCost), yPosition);
            yPosition -= 25;

            BigDecimal totalCost = energyCost.add(maintenanceCost);
            addTextField(contentStream, "Total Costs:", String.format("$%.2f", totalCost), yPosition);
            yPosition -= 25;

            // Calculate cost per km
            List<Trip> trips = tripRepository.findByVehicleIdAndStartTimeBetween(
                    vehicle.getId(), request.getStartDate(), request.getEndDate());

            double totalDistance = trips.stream()
                    .mapToDouble(t -> t.getDistance() != null ? t.getDistance() : 0.0)
                    .sum();

            if (totalDistance > 0) {
                double costPerKm = totalCost.doubleValue() / totalDistance;
                addTextField(contentStream, "Cost per km:", String.format("$%.4f", costPerKm), yPosition);
            }
        }
    }

    /**
     * Add event timeline section for genealogy report
     */
    private void addEventTimelineSection(PDDocument document, Vehicle vehicle, 
                                         LocalDateTime startDate, LocalDateTime endDate) throws IOException {
        PDPage page = new PDPage(PDRectangle.A4);
        document.addPage(page);

        try (PDPageContentStream contentStream = new PDPageContentStream(document, page)) {
            contentStream.setFont(PDType1Font.HELVETICA_BOLD, 14);
            contentStream.beginText();
            contentStream.newLineAtOffset(50, 750);
            contentStream.showText("Event Timeline");
            contentStream.endText();

            contentStream.setFont(PDType1Font.HELVETICA, 10);
            int yPosition = 720;

            // This would include all events chronologically
            // For now, showing a simplified version
            contentStream.beginText();
            contentStream.newLineAtOffset(50, yPosition);
            contentStream.showText("Timeline of all vehicle events including trips, maintenance, and charging...");
            contentStream.endText();
        }
    }

    /**
     * Helper method to add text field
     */
    private void addTextField(PDPageContentStream contentStream, String label, String value, int yPosition) throws IOException {
        contentStream.beginText();
        contentStream.newLineAtOffset(50, yPosition);
        contentStream.showText(label + " " + (value != null ? value : "N/A"));
        contentStream.endText();
    }
}
