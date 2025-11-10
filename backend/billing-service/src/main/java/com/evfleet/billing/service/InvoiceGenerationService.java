package com.evfleet.billing.service;

import com.evfleet.billing.entity.Invoice;
import com.evfleet.billing.repository.InvoiceRepository;
import com.evfleet.billing.dto.InvoiceDTO;
import com.evfleet.billing.exception.BillingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.LocalDateTime;
import java.util.*;

/**
 * Service for generating monthly invoices based on vehicle pricing tiers.
 * Groups vehicles by tier and calculates billing based on fuel type usage.
 *
 * PR 18: Invoice Generation - Monthly billing automation
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class InvoiceGenerationService {

    private final InvoiceRepository invoiceRepository;

    /**
     * Generate invoices for all active fleets on the 1st of each month.
     * Runs at 00:00 on the first day of month.
     */
    @Scheduled(cron = "0 0 0 1 * *")
    @Transactional
    public void generateMonthlyInvoices() {
        log.info("Starting monthly invoice generation for all fleets");
        try {
            YearMonth previousMonth = YearMonth.now().minusMonths(1);

            // In production, fetch all fleet IDs from fleet-service
            // For now, log the scheduled execution
            log.info("Monthly invoice generation scheduled for month: {}", previousMonth);

        } catch (Exception e) {
            log.error("Error generating monthly invoices", e);
            throw new BillingException("Failed to generate monthly invoices", e);
        }
    }

    /**
     * Generate invoice for a specific fleet in a specific month.
     * Groups vehicles by pricing tier and calculates individual charges.
     */
    @Transactional
    public Invoice generateInvoiceForFleet(Long fleetId, YearMonth month,
                                          BigDecimal baseCharge, Map<String, BigDecimal> chargesByTier,
                                          Integer vehicleCount) {
        log.info("Generating invoice for fleet {} for month {}", fleetId, month);

        try {
            // Calculate total charge
            BigDecimal totalCharge = baseCharge;
            for (BigDecimal charge : chargesByTier.values()) {
                totalCharge = totalCharge.add(charge);
            }

            // Create invoice
            Invoice invoice = new Invoice();
            invoice.setId(UUID.randomUUID().toString());
            invoice.setCompanyId(fleetId.toString());
            invoice.setInvoiceNumber("INV-" + month + "-" + fleetId);
            invoice.setAmount(totalCharge);
            invoice.setTotalAmount(totalCharge);
            invoice.setTax(BigDecimal.ZERO);
            invoice.setDueDate(LocalDate.now().plusDays(15));
            invoice.setStatus("DRAFT");
            // Convert Map<String, BigDecimal> to Map<String, Object>
            Map<String, Object> chargesAsObjects = new HashMap<>();
            chargesByTier.forEach((key, value) -> chargesAsObjects.put(key, value));
            invoice.setChargesByTier(chargesAsObjects);
            invoice.setCreatedAt(LocalDateTime.now());
            invoice.setUpdatedAt(LocalDateTime.now());

            invoiceRepository.save(invoice);
            log.info("Invoice created for fleet {}: amount=₹{}, vehicles={}",
                    fleetId, totalCharge, vehicleCount);

            return invoice;
        } catch (Exception e) {
            log.error("Error generating invoice for fleet {}", fleetId, e);
            throw new BillingException("Failed to generate invoice", e);
        }
    }

    /**
     * Calculate total charge for vehicles in a specific tier for a month.
     * Includes base subscription fee + usage-based surcharges.
     */
    public BigDecimal calculateTierCharge(String tierName, BigDecimal monthlyPrice,
                                         Integer vehicleCount, BigDecimal usageSurcharge) {
        // Base: monthly subscription × vehicle count
        BigDecimal baseCharge = monthlyPrice.multiply(BigDecimal.valueOf(vehicleCount));

        // Add usage surcharge
        BigDecimal totalCharge = baseCharge.add(usageSurcharge);

        log.debug("Tier {} charge: base=₹{}, usage=₹{}, total=₹{}",
                tierName, baseCharge, usageSurcharge, totalCharge);

        return totalCharge;
    }

    /**
     * Calculate EV surcharge based on energy consumption.
     * Rate: ₹15/kWh for usage above free tier (50 kWh/month).
     */
    public BigDecimal calculateEVSurcharge(double energyConsumed) {
        double freeEnergyTier = 50.0; // kWh
        if (energyConsumed <= freeEnergyTier) {
            return BigDecimal.ZERO;
        }

        double chargeableEnergy = energyConsumed - freeEnergyTier;
        BigDecimal surcharge = BigDecimal.valueOf(chargeableEnergy)
                .multiply(BigDecimal.valueOf(15)); // ₹15/kWh

        log.debug("EV surcharge: consumed={} kWh, surcharge=₹{}", energyConsumed, surcharge);
        return surcharge;
    }

    /**
     * Calculate ICE surcharge based on fuel consumption.
     * Rate: ₹10/liter for usage above free tier (100 liters/month).
     */
    public BigDecimal calculateICESurcharge(double fuelConsumed) {
        double freeFuelTier = 100.0; // liters
        if (fuelConsumed <= freeFuelTier) {
            return BigDecimal.ZERO;
        }

        double chargeableFuel = fuelConsumed - freeFuelTier;
        BigDecimal surcharge = BigDecimal.valueOf(chargeableFuel)
                .multiply(BigDecimal.valueOf(10)); // ₹10/liter

        log.debug("ICE surcharge: consumed={} liters, surcharge=₹{}", fuelConsumed, surcharge);
        return surcharge;
    }

    /**
     * Calculate HYBRID surcharge: Combined EV + ICE surcharges with 10% discount.
     */
    public BigDecimal calculateHybridSurcharge(double energyConsumed, double fuelConsumed) {
        BigDecimal evSurcharge = calculateEVSurcharge(energyConsumed);
        BigDecimal iceSurcharge = calculateICESurcharge(fuelConsumed);

        // 10% discount for hybrid vehicles (more efficient)
        BigDecimal totalSurcharge = evSurcharge.add(iceSurcharge);
        BigDecimal discountedSurcharge = totalSurcharge.multiply(BigDecimal.valueOf(0.9));

        log.debug("HYBRID surcharge: ev=₹{}, ice=₹{}, discounted=₹{}",
                evSurcharge, iceSurcharge, discountedSurcharge);

        return discountedSurcharge;
    }

    /**
     * Finalize invoice and prepare for payment processing.
     */
    @Transactional
    public void finalizeInvoice(String invoiceId) {
        Invoice invoice = invoiceRepository.findById(invoiceId)
                .orElseThrow(() -> new BillingException("Invoice not found: " + invoiceId));

        if (!"DRAFT".equals(invoice.getStatus())) {
            throw new BillingException("Can only finalize DRAFT invoices");
        }

        invoice.setStatus("FINALIZED");
        invoiceRepository.save(invoice);
        log.info("Invoice {} finalized with amount ₹{}", invoiceId, invoice.getTotalAmount());
    }

    /**
     * Get invoice details
     */
    public InvoiceDTO getInvoiceDetails(String invoiceId) {
        Invoice invoice = invoiceRepository.findById(invoiceId)
                .orElseThrow(() -> new BillingException("Invoice not found: " + invoiceId));

        return InvoiceDTO.fromEntity(invoice);
    }

    /**
     * Apply late fee for overdue invoices (> 30 days past due date).
     * Applies 5% late fee on outstanding amount.
     */
    @Transactional
    public void handleOverdueInvoice(String invoiceId) {
        Invoice invoice = invoiceRepository.findById(invoiceId)
                .orElseThrow(() -> new BillingException("Invoice not found: " + invoiceId));

        if (!"FINALIZED".equals(invoice.getStatus())) {
            return; // Only apply to unpaid invoices
        }

        if (LocalDate.now().isAfter(invoice.getDueDate().plusDays(30))) {
            BigDecimal lateFee = invoice.getTotalAmount().multiply(BigDecimal.valueOf(0.05));
            invoice.setTotalAmount(invoice.getTotalAmount().add(lateFee));
            invoice.setStatus("OVERDUE");
            invoiceRepository.save(invoice);

            log.warn("Invoice {} marked overdue with late fee ₹{}", invoiceId, lateFee);
        }
    }
}
