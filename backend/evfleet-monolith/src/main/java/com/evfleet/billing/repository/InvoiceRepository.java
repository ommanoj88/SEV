package com.evfleet.billing.repository;

import com.evfleet.billing.model.Invoice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface InvoiceRepository extends JpaRepository<Invoice, Long> {
    List<Invoice> findByCompanyId(Long companyId);
    List<Invoice> findByStatus(Invoice.InvoiceStatus status);
    Optional<Invoice> findByInvoiceNumber(String invoiceNumber);
    
    /**
     * Find invoices by company and status
     */
    List<Invoice> findByCompanyIdAndStatus(Long companyId, Invoice.InvoiceStatus status);
    
    /**
     * Find invoices created after a specific date for a company
     */
    List<Invoice> findByCompanyIdAndInvoiceDateAfter(Long companyId, LocalDate date);
}
