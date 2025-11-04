package com.evfleet.notification.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AlertResponse {

    private String id;
    private String companyId;
    private String ruleName;
    private String triggerType;
    private String conditions;
    private String actions;
    private Boolean enabled;
    private String schedule;
    private String createdBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
