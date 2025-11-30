package com.evfleet.billing.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.constraints.NotBlank;

/**
 * Razorpay Configuration Properties
 * 
 * Configure via environment variables or application.yml:
 * - RAZORPAY_KEY_ID: Your Razorpay Key ID
 * - RAZORPAY_KEY_SECRET: Your Razorpay Key Secret
 * - RAZORPAY_WEBHOOK_SECRET: Secret for webhook signature verification
 * 
 * For test mode, use test credentials from Razorpay Dashboard.
 * 
 * @author SEV Platform Team
 * @version 1.0.0
 */
@Configuration
@ConfigurationProperties(prefix = "razorpay")
@Data
@Validated
public class RazorpayConfig {
    
    /**
     * Razorpay API Key ID
     * Get from: https://dashboard.razorpay.com/app/keys
     */
    @NotBlank(message = "Razorpay Key ID is required")
    private String keyId;
    
    /**
     * Razorpay API Key Secret
     * Get from: https://dashboard.razorpay.com/app/keys
     */
    @NotBlank(message = "Razorpay Key Secret is required")
    private String keySecret;
    
    /**
     * Webhook Secret for signature verification
     * Get from: https://dashboard.razorpay.com/app/webhooks
     */
    private String webhookSecret;
    
    /**
     * Whether to run in test mode (sandbox)
     * Default: true for development safety
     */
    private boolean testMode = true;
    
    /**
     * Default currency for transactions
     */
    private String currency = "INR";
    
    /**
     * Payment link expiry in minutes
     */
    private int paymentLinkExpiryMinutes = 60;
    
    /**
     * Maximum retry attempts for API calls
     */
    private int maxRetries = 3;
    
    /**
     * Timeout for API calls in milliseconds
     */
    private int apiTimeoutMs = 30000;
    
    /**
     * Callback URL for successful payments
     */
    private String callbackUrl;
    
    /**
     * Webhook URL for payment events
     */
    private String webhookUrl;
    
    /**
     * Whether Razorpay integration is enabled
     */
    private boolean enabled = false;
    
    /**
     * Company name to display on Razorpay checkout
     */
    private String companyName = "SEV Fleet Management";
    
    /**
     * Description prefix for orders
     */
    private String orderDescriptionPrefix = "SEV Invoice Payment - ";
    
    /**
     * Check if configuration is valid for making API calls
     */
    public boolean isConfigured() {
        return enabled && keyId != null && !keyId.isBlank() 
            && keySecret != null && !keySecret.isBlank();
    }
    
    /**
     * Get masked key for logging (shows first 8 chars only)
     */
    public String getMaskedKeyId() {
        if (keyId == null || keyId.length() < 8) {
            return "***";
        }
        return keyId.substring(0, 8) + "***";
    }
}
