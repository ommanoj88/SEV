package com.evfleet.notification.infrastructure.adapter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class SMSAdapter {
    public boolean sendSMS(String phone, String message) {
        log.info("Sending SMS to: {}", phone);
        // Mock implementation - would integrate with Twilio
        return true;
    }
}
