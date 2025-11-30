package com.evfleet.billing.service;

import com.evfleet.billing.dto.PaymentReceiptResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import java.math.BigDecimal;

/**
 * Payment Email Notification Service
 * 
 * Sends email notifications for payment events:
 * - Payment success with receipt
 * - Payment failure notification
 * - Payment reminder
 * 
 * @author SEV Platform Team
 * @version 1.0.0
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class PaymentEmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username:noreply@evfleet.com}")
    private String fromEmail;

    @Value("${app.name:EV Fleet Management}")
    private String appName;

    @Value("${app.url:https://evfleet.com}")
    private String appUrl;

    /**
     * Send payment success email with receipt details
     * 
     * @param toEmail Recipient email
     * @param receipt Payment receipt details
     */
    @Async
    public void sendPaymentSuccessEmail(String toEmail, PaymentReceiptResponse receipt) {
        if (toEmail == null || toEmail.isBlank()) {
            log.warn("Cannot send payment success email - no recipient email provided");
            return;
        }

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(toEmail);
            helper.setSubject("Payment Successful - Receipt #" + receipt.getReceiptNumber());
            helper.setText(buildPaymentSuccessEmailHtml(receipt), true);

            mailSender.send(message);
            log.info("Payment success email sent to: {} for receipt: {}", toEmail, receipt.getReceiptNumber());

        } catch (MessagingException e) {
            log.error("Failed to send payment success email to: {}: {}", toEmail, e.getMessage());
        }
    }

    /**
     * Send payment failure notification email
     * 
     * @param toEmail Recipient email
     * @param invoiceNumber Invoice number
     * @param amount Failed payment amount
     * @param errorReason Reason for failure
     */
    @Async
    public void sendPaymentFailureEmail(String toEmail, String invoiceNumber, BigDecimal amount, String errorReason) {
        if (toEmail == null || toEmail.isBlank()) {
            log.warn("Cannot send payment failure email - no recipient email provided");
            return;
        }

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(toEmail);
            helper.setSubject("Payment Failed - Invoice " + invoiceNumber);
            helper.setText(buildPaymentFailureEmailHtml(invoiceNumber, amount, errorReason), true);

            mailSender.send(message);
            log.info("Payment failure email sent to: {} for invoice: {}", toEmail, invoiceNumber);

        } catch (MessagingException e) {
            log.error("Failed to send payment failure email to: {}: {}", toEmail, e.getMessage());
        }
    }

    /**
     * Send payment reminder email
     * 
     * @param toEmail Recipient email
     * @param invoiceNumber Invoice number
     * @param amount Due amount
     * @param dueDate Due date
     */
    @Async
    public void sendPaymentReminderEmail(String toEmail, String invoiceNumber, BigDecimal amount, String dueDate) {
        if (toEmail == null || toEmail.isBlank()) {
            log.warn("Cannot send payment reminder email - no recipient email provided");
            return;
        }

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(toEmail);
            helper.setSubject("Payment Reminder - Invoice " + invoiceNumber);
            helper.setText(buildPaymentReminderEmailHtml(invoiceNumber, amount, dueDate), true);

            mailSender.send(message);
            log.info("Payment reminder email sent to: {} for invoice: {}", toEmail, invoiceNumber);

        } catch (MessagingException e) {
            log.error("Failed to send payment reminder email to: {}: {}", toEmail, e.getMessage());
        }
    }

    /**
     * Helper method to send HTML emails
     */
    private void sendHtmlEmail(String toEmail, String subject, String htmlContent) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(toEmail);
            helper.setSubject(subject);
            helper.setText(htmlContent, true);

            mailSender.send(message);
            log.info("Email sent successfully to: {}, subject: {}", toEmail, subject);

        } catch (MessagingException e) {
            log.error("Failed to send email to: {}: {}", toEmail, e.getMessage());
        }
    }

    // ==================== EMAIL TEMPLATE BUILDERS ====================

    private String buildPaymentSuccessEmailHtml(PaymentReceiptResponse receipt) {
        return """
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <style>
                    body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
                    .container { max-width: 600px; margin: 0 auto; padding: 20px; }
                    .header { background: #28a745; color: white; padding: 20px; text-align: center; border-radius: 5px 5px 0 0; }
                    .content { background: #f9f9f9; padding: 20px; border: 1px solid #ddd; }
                    .receipt-box { background: white; padding: 20px; margin: 15px 0; border: 1px solid #e0e0e0; border-radius: 5px; }
                    .row { display: flex; justify-content: space-between; padding: 8px 0; border-bottom: 1px solid #eee; }
                    .row:last-child { border-bottom: none; }
                    .label { color: #666; }
                    .value { font-weight: bold; }
                    .amount { color: #28a745; font-size: 24px; font-weight: bold; }
                    .footer { text-align: center; padding: 15px; color: #666; font-size: 12px; }
                    .success-icon { font-size: 48px; margin-bottom: 10px; }
                    .button { display: inline-block; background: #007bff; color: white; padding: 12px 30px; 
                              text-decoration: none; border-radius: 5px; margin-top: 15px; }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <div class="success-icon">✓</div>
                        <h1>Payment Successful!</h1>
                    </div>
                    <div class="content">
                        <p>Thank you for your payment. Here's your receipt:</p>
                        
                        <div class="receipt-box">
                            <h3>Receipt Details</h3>
                            <div class="row">
                                <span class="label">Receipt Number:</span>
                                <span class="value">%s</span>
                            </div>
                            <div class="row">
                                <span class="label">Date:</span>
                                <span class="value">%s</span>
                            </div>
                            <div class="row">
                                <span class="label">Invoice Number:</span>
                                <span class="value">%s</span>
                            </div>
                            <div class="row">
                                <span class="label">Transaction ID:</span>
                                <span class="value">%s</span>
                            </div>
                            <div class="row">
                                <span class="label">Payment Method:</span>
                                <span class="value">%s</span>
                            </div>
                        </div>

                        <div class="receipt-box">
                            <h3>Payment Summary</h3>
                            <div class="row">
                                <span class="label">Invoice Total:</span>
                                <span class="value">₹%.2f</span>
                            </div>
                            <div class="row">
                                <span class="label">Previously Paid:</span>
                                <span class="value">₹%.2f</span>
                            </div>
                            <div class="row">
                                <span class="label">Amount Paid Now:</span>
                                <span class="amount">₹%.2f</span>
                            </div>
                            <div class="row">
                                <span class="label">Remaining Balance:</span>
                                <span class="value">₹%.2f</span>
                            </div>
                            <div class="row">
                                <span class="label">Status:</span>
                                <span class="value">%s</span>
                            </div>
                        </div>

                        <center>
                            <a href="%s/billing/invoices/%d" class="button">View Invoice</a>
                        </center>
                    </div>
                    <div class="footer">
                        <p>This is an automated email from %s</p>
                        <p>If you have any questions, please contact our support team.</p>
                    </div>
                </div>
            </body>
            </html>
            """.formatted(
                receipt.getReceiptNumber(),
                receipt.getFormattedReceiptDate(),
                receipt.getInvoiceNumber(),
                receipt.getTransactionId(),
                receipt.getFormattedPaymentMethod(),
                receipt.getInvoiceTotal(),
                receipt.getPreviouslyPaid() != null ? receipt.getPreviouslyPaid() : BigDecimal.ZERO,
                receipt.getAmountPaid(),
                receipt.getRemainingAmount() != null ? receipt.getRemainingAmount() : BigDecimal.ZERO,
                receipt.isFullyPaid() ? "FULLY PAID" : "PARTIALLY PAID",
                appUrl,
                receipt.getInvoiceId(),
                appName
            );
    }

    private String buildPaymentFailureEmailHtml(String invoiceNumber, BigDecimal amount, String errorReason) {
        return """
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <style>
                    body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
                    .container { max-width: 600px; margin: 0 auto; padding: 20px; }
                    .header { background: #dc3545; color: white; padding: 20px; text-align: center; border-radius: 5px 5px 0 0; }
                    .content { background: #f9f9f9; padding: 20px; border: 1px solid #ddd; }
                    .info-box { background: white; padding: 20px; margin: 15px 0; border: 1px solid #e0e0e0; border-radius: 5px; }
                    .row { display: flex; justify-content: space-between; padding: 8px 0; border-bottom: 1px solid #eee; }
                    .label { color: #666; }
                    .value { font-weight: bold; }
                    .error-reason { color: #dc3545; }
                    .footer { text-align: center; padding: 15px; color: #666; font-size: 12px; }
                    .error-icon { font-size: 48px; margin-bottom: 10px; }
                    .button { display: inline-block; background: #007bff; color: white; padding: 12px 30px; 
                              text-decoration: none; border-radius: 5px; margin-top: 15px; }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <div class="error-icon">✗</div>
                        <h1>Payment Failed</h1>
                    </div>
                    <div class="content">
                        <p>We were unable to process your payment. Please try again or use a different payment method.</p>
                        
                        <div class="info-box">
                            <h3>Payment Details</h3>
                            <div class="row">
                                <span class="label">Invoice Number:</span>
                                <span class="value">%s</span>
                            </div>
                            <div class="row">
                                <span class="label">Amount:</span>
                                <span class="value">₹%.2f</span>
                            </div>
                            <div class="row">
                                <span class="label">Reason:</span>
                                <span class="value error-reason">%s</span>
                            </div>
                        </div>

                        <p><strong>What you can do:</strong></p>
                        <ul>
                            <li>Check if your payment details are correct</li>
                            <li>Ensure sufficient balance in your account</li>
                            <li>Try a different payment method</li>
                            <li>Contact your bank if the issue persists</li>
                        </ul>

                        <center>
                            <a href="%s/billing/pay" class="button">Retry Payment</a>
                        </center>
                    </div>
                    <div class="footer">
                        <p>This is an automated email from %s</p>
                        <p>If you need assistance, please contact our support team.</p>
                    </div>
                </div>
            </body>
            </html>
            """.formatted(
                invoiceNumber,
                amount,
                errorReason,
                appUrl,
                appName
            );
    }

    private String buildPaymentReminderEmailHtml(String invoiceNumber, BigDecimal amount, String dueDate) {
        return """
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <style>
                    body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
                    .container { max-width: 600px; margin: 0 auto; padding: 20px; }
                    .header { background: #ffc107; color: #333; padding: 20px; text-align: center; border-radius: 5px 5px 0 0; }
                    .content { background: #f9f9f9; padding: 20px; border: 1px solid #ddd; }
                    .info-box { background: white; padding: 20px; margin: 15px 0; border: 1px solid #e0e0e0; border-radius: 5px; }
                    .row { display: flex; justify-content: space-between; padding: 8px 0; border-bottom: 1px solid #eee; }
                    .label { color: #666; }
                    .value { font-weight: bold; }
                    .amount { color: #007bff; font-size: 24px; font-weight: bold; }
                    .footer { text-align: center; padding: 15px; color: #666; font-size: 12px; }
                    .reminder-icon { font-size: 48px; margin-bottom: 10px; }
                    .button { display: inline-block; background: #28a745; color: white; padding: 12px 30px; 
                              text-decoration: none; border-radius: 5px; margin-top: 15px; }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <div class="reminder-icon">🔔</div>
                        <h1>Payment Reminder</h1>
                    </div>
                    <div class="content">
                        <p>This is a friendly reminder that your invoice payment is due soon.</p>
                        
                        <div class="info-box">
                            <h3>Invoice Details</h3>
                            <div class="row">
                                <span class="label">Invoice Number:</span>
                                <span class="value">%s</span>
                            </div>
                            <div class="row">
                                <span class="label">Amount Due:</span>
                                <span class="amount">₹%.2f</span>
                            </div>
                            <div class="row">
                                <span class="label">Due Date:</span>
                                <span class="value">%s</span>
                            </div>
                        </div>

                        <center>
                            <a href="%s/billing/pay" class="button">Pay Now</a>
                        </center>
                    </div>
                    <div class="footer">
                        <p>This is an automated email from %s</p>
                        <p>If you have already made this payment, please disregard this email.</p>
                    </div>
                </div>
            </body>
            </html>
            """.formatted(
                invoiceNumber,
                amount,
                dueDate,
                appUrl,
                appName
            );
    }

    // ==================== Subscription Email Methods ====================

    /**
     * Send subscription expiry reminder email
     */
    @Async
    public void sendSubscriptionExpiryReminder(String email, String companyName, String planType,
                                                java.time.LocalDate expiryDate, long daysRemaining,
                                                java.math.BigDecimal renewalAmount, boolean autoRenew) {
        if (email == null || email.isBlank()) {
            log.warn("Cannot send subscription expiry reminder - no recipient email provided");
            return;
        }
        
        log.info("Sending subscription expiry reminder to: {}", email);

        String subject = String.format("Your %s subscription expires in %d days", planType, daysRemaining);
        
        String htmlContent = String.format("""
            <html>
            <head>
                <meta charset="UTF-8">
                <style>
                    body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
                    .container { max-width: 600px; margin: 0 auto; padding: 20px; }
                    .header { background: #f0ad4e; color: white; padding: 20px; text-align: center; border-radius: 5px 5px 0 0; }
                    .content { background: #f9f9f9; padding: 20px; border: 1px solid #ddd; }
                    .info-box { background: white; padding: 20px; margin: 15px 0; border: 1px solid #e0e0e0; border-radius: 5px; }
                    .row { display: flex; justify-content: space-between; padding: 8px 0; border-bottom: 1px solid #eee; }
                    .label { color: #666; }
                    .value { font-weight: bold; }
                    .footer { text-align: center; padding: 15px; color: #666; font-size: 12px; }
                    .days-badge { font-size: 36px; font-weight: bold; color: #f0ad4e; margin: 10px 0; }
                    .button { display: inline-block; background: #28a745; color: white; padding: 12px 30px; 
                              text-decoration: none; border-radius: 5px; margin-top: 15px; }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <h1>⏰ Subscription Expiring Soon</h1>
                    </div>
                    <div class="content">
                        <p>Dear %s,</p>
                        <center>
                            <div class="days-badge">%d DAYS</div>
                            <p>until your subscription expires</p>
                        </center>
                        
                        <div class="info-box">
                            <h3>Subscription Details</h3>
                            <div class="row">
                                <span class="label">Plan:</span>
                                <span class="value">%s</span>
                            </div>
                            <div class="row">
                                <span class="label">Expiry Date:</span>
                                <span class="value">%s</span>
                            </div>
                            <div class="row">
                                <span class="label">Renewal Amount:</span>
                                <span class="value">₹%.2f</span>
                            </div>
                            <div class="row">
                                <span class="label">Auto-Renewal:</span>
                                <span class="value">%s</span>
                            </div>
                        </div>

                        %s

                        <center>
                            <a href="%s/billing" class="button">Manage Subscription</a>
                        </center>
                    </div>
                    <div class="footer">
                        <p>This is an automated email from %s</p>
                    </div>
                </div>
            </body>
            </html>
            """,
            companyName,
            daysRemaining,
            planType,
            expiryDate,
            renewalAmount,
            autoRenew ? "Enabled ✓" : "Disabled",
            autoRenew 
                ? "<p>✓ <strong>Auto-renewal is enabled.</strong> Your subscription will automatically renew and an invoice will be generated.</p>"
                : "<p>⚠️ <strong>Auto-renewal is disabled.</strong> Please renew manually to continue using our services.</p>",
            appUrl,
            appName
        );

        sendHtmlEmail(email, subject, htmlContent);
    }

    /**
     * Send grace period warning email
     */
    @Async
    public void sendGracePeriodWarning(String email, String companyName, 
                                        java.time.LocalDate expiryDate, int daysRemaining,
                                        java.math.BigDecimal amount) {
        if (email == null || email.isBlank()) {
            log.warn("Cannot send grace period warning - no recipient email provided");
            return;
        }
        log.info("Sending grace period warning to: {}", email);

        String subject = "⚠️ Your subscription has expired - " + daysRemaining + " days remaining in grace period";
        
        String htmlContent = String.format("""
            <html>
            <head>
                <meta charset="UTF-8">
                <style>
                    body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
                    .container { max-width: 600px; margin: 0 auto; padding: 20px; }
                    .header { background: #dc3545; color: white; padding: 20px; text-align: center; border-radius: 5px 5px 0 0; }
                    .content { background: #f9f9f9; padding: 20px; border: 1px solid #ddd; }
                    .warning-box { background: #fff3cd; border: 1px solid #ffc107; padding: 15px; border-radius: 5px; margin: 15px 0; }
                    .footer { text-align: center; padding: 15px; color: #666; font-size: 12px; }
                    .days-badge { font-size: 48px; font-weight: bold; color: #dc3545; margin: 10px 0; }
                    .button { display: inline-block; background: #28a745; color: white; padding: 12px 30px; 
                              text-decoration: none; border-radius: 5px; margin-top: 15px; }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <h1>⚠️ Subscription Expired</h1>
                    </div>
                    <div class="content">
                        <p>Dear %s,</p>
                        
                        <div class="warning-box">
                            <center>
                                <div class="days-badge">%d DAYS LEFT</div>
                                <p><strong>Grace period ending soon!</strong></p>
                            </center>
                            <p>Your subscription expired on %s. You have %d days remaining to make payment before service suspension.</p>
                        </div>

                        <p><strong>What happens next?</strong></p>
                        <ul>
                            <li>Pay now to restore full access immediately</li>
                            <li>If unpaid, your account will be suspended in %d days</li>
                            <li>Your data will be preserved for 30 days after suspension</li>
                        </ul>

                        <center>
                            <a href="%s/billing/pay" class="button">Pay ₹%.2f Now</a>
                        </center>
                    </div>
                    <div class="footer">
                        <p>Need help? Contact our support team.</p>
                        <p>This is an automated email from %s</p>
                    </div>
                </div>
            </body>
            </html>
            """,
            companyName,
            daysRemaining,
            expiryDate,
            daysRemaining,
            daysRemaining,
            appUrl,
            amount,
            appName
        );

        sendHtmlEmail(email, subject, htmlContent);
    }

    /**
     * Send subscription suspension notice
     */
    @Async
    public void sendSubscriptionSuspensionNotice(String email, String companyName, String planType) {
        if (email == null || email.isBlank()) {
            log.warn("Cannot send suspension notice - no recipient email provided");
            return;
        }
        log.info("Sending suspension notice to: {}", email);

        String subject = "🚫 Your " + planType + " subscription has been suspended";
        
        String htmlContent = String.format("""
            <html>
            <head>
                <meta charset="UTF-8">
                <style>
                    body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
                    .container { max-width: 600px; margin: 0 auto; padding: 20px; }
                    .header { background: #343a40; color: white; padding: 20px; text-align: center; border-radius: 5px 5px 0 0; }
                    .content { background: #f9f9f9; padding: 20px; border: 1px solid #ddd; }
                    .notice-box { background: #f8d7da; border: 1px solid #f5c6cb; padding: 15px; border-radius: 5px; margin: 15px 0; }
                    .footer { text-align: center; padding: 15px; color: #666; font-size: 12px; }
                    .button { display: inline-block; background: #28a745; color: white; padding: 12px 30px; 
                              text-decoration: none; border-radius: 5px; margin-top: 15px; }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <h1>🚫 Account Suspended</h1>
                    </div>
                    <div class="content">
                        <p>Dear %s,</p>
                        
                        <div class="notice-box">
                            <p><strong>Your %s subscription has been suspended due to non-payment.</strong></p>
                        </div>

                        <p><strong>What this means:</strong></p>
                        <ul>
                            <li>Access to the platform is restricted</li>
                            <li>Vehicle tracking and fleet management features are unavailable</li>
                            <li>Your data is preserved for 30 days</li>
                        </ul>

                        <p><strong>To restore access:</strong></p>
                        <p>Pay your outstanding invoice to immediately restore full access to all features.</p>

                        <center>
                            <a href="%s/billing" class="button">Pay Now & Restore Access</a>
                        </center>
                    </div>
                    <div class="footer">
                        <p>Questions? Contact support@evfleet.com</p>
                        <p>This is an automated email from %s</p>
                    </div>
                </div>
            </body>
            </html>
            """,
            companyName,
            planType,
            appUrl,
            appName
        );

        sendHtmlEmail(email, subject, htmlContent);
    }

    /**
     * Send renewal invoice notification
     */
    @Async
    public void sendRenewalInvoiceNotification(String email, String companyName, 
                                                String invoiceNumber, java.math.BigDecimal amount,
                                                java.time.LocalDate dueDate) {
        log.info("Sending renewal invoice notification to: {}", email);

        String subject = "New renewal invoice #" + invoiceNumber + " - ₹" + amount + " due";
        
        String htmlContent = String.format("""
            <html>
            <head>
                <meta charset="UTF-8">
                <style>
                    body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
                    .container { max-width: 600px; margin: 0 auto; padding: 20px; }
                    .header { background: #007bff; color: white; padding: 20px; text-align: center; border-radius: 5px 5px 0 0; }
                    .content { background: #f9f9f9; padding: 20px; border: 1px solid #ddd; }
                    .info-box { background: white; padding: 20px; margin: 15px 0; border: 1px solid #e0e0e0; border-radius: 5px; }
                    .row { display: flex; justify-content: space-between; padding: 8px 0; border-bottom: 1px solid #eee; }
                    .label { color: #666; }
                    .value { font-weight: bold; }
                    .amount { font-size: 24px; color: #28a745; font-weight: bold; }
                    .footer { text-align: center; padding: 15px; color: #666; font-size: 12px; }
                    .button { display: inline-block; background: #28a745; color: white; padding: 12px 30px; 
                              text-decoration: none; border-radius: 5px; margin-top: 15px; }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <h1>📄 Renewal Invoice</h1>
                    </div>
                    <div class="content">
                        <p>Dear %s,</p>
                        <p>A new invoice has been generated for your subscription renewal.</p>
                        
                        <div class="info-box">
                            <h3>Invoice Details</h3>
                            <div class="row">
                                <span class="label">Invoice Number:</span>
                                <span class="value">%s</span>
                            </div>
                            <div class="row">
                                <span class="label">Amount:</span>
                                <span class="amount">₹%.2f</span>
                            </div>
                            <div class="row">
                                <span class="label">Due Date:</span>
                                <span class="value">%s</span>
                            </div>
                        </div>

                        <center>
                            <a href="%s/billing/pay" class="button">Pay Now</a>
                        </center>
                    </div>
                    <div class="footer">
                        <p>This is an automated email from %s</p>
                    </div>
                </div>
            </body>
            </html>
            """,
            companyName,
            invoiceNumber,
            amount,
            dueDate,
            appUrl,
            appName
        );

        sendHtmlEmail(email, subject, htmlContent);
    }
}
