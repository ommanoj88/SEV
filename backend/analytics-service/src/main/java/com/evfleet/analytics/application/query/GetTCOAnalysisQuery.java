package com.evfleet.analytics.application.query;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GetTCOAnalysisQuery {
    private String vehicleId;
    private LocalDate fromDate;
    private LocalDate toDate;
}
