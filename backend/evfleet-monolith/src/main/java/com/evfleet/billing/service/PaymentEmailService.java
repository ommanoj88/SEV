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
}
