package com.evfleet.analytics.application.query;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GetFleetSummaryQuery {
    private String companyId;
}
