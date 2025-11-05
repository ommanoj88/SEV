package com.evfleet.billing.repository;

import com.evfleet.billing.entity.Invoice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface InvoiceRepository extends JpaRepository<Invoice, String> {
    List<Invoice> findByCompanyId(String companyId);
    List<Invoice> findByCompanyIdOrderByCreatedAtDesc(String companyId);
}
