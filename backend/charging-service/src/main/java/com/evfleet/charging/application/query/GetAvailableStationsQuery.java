package com.evfleet.charging.application.query;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GetAvailableStationsQuery {
    private Double latitude;
    private Double longitude;
    private Double radiusKm = 10.0;
    private Boolean onlyAvailable = true;
}
