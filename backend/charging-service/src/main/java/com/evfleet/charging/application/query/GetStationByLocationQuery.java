package com.evfleet.charging.application.query;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GetStationByLocationQuery {
    private String city;
    private String state;
    private String provider;
}
