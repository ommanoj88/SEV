package com.evfleet.notification.infrastructure.adapter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class EmailAdapter {
    public boolean sendEmail(String to, String subject, String body) {
        log.info("Sending email to: {}, subject: {}", to, subject);
        // Mock implementation - would integrate with SendGrid/SES
        return true;
    }
}
