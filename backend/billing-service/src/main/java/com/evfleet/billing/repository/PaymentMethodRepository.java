package com.evfleet.billing.repository;

import com.evfleet.billing.entity.PaymentMethod;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PaymentMethodRepository extends JpaRepository<PaymentMethod, String> {
    List<PaymentMethod> findByCompanyId(String companyId);
    List<PaymentMethod> findByCompanyIdAndIsActiveTrue(String companyId);
}
