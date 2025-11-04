package com.evfleet.charging.application.query;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GetSessionHistoryQuery {
    private String vehicleId;
    private LocalDateTime fromDate;
    private LocalDateTime toDate;
    private Integer page = 0;
    private Integer size = 20;
}
