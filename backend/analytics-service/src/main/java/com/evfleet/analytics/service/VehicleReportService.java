package com.evfleet.analytics.service;

import com.evfleet.analytics.dto.VehicleReportRequest;

/**
 * Service interface for generating vehicle reports
 */
public interface VehicleReportService {

    /**
     * Generate a comprehensive vehicle report in PDF format
     * This includes genealogy and historical data for the vehicle
     * 
     * @param request The report request parameters
     * @return PDF report as byte array
     */
    byte[] generateVehicleReport(VehicleReportRequest request);

    /**
     * Generate a vehicle genealogy report
     * Focuses on complete event history and timeline
     * 
     * @param vehicleId The vehicle ID
     * @param startDate Start date for the report
     * @param endDate End date for the report
     * @return PDF report as byte array
     */
    byte[] generateGenealogyReport(Long vehicleId, java.time.LocalDateTime startDate, java.time.LocalDateTime endDate);
}
