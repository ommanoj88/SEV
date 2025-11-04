package com.evfleet.notification.repository;

import com.evfleet.notification.entity.AlertRule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AlertRuleRepository extends JpaRepository<AlertRule, String> {

    List<AlertRule> findByCompanyId(String companyId);

    List<AlertRule> findByEnabledTrue();

    List<AlertRule> findByCompanyIdAndEnabledTrue(String companyId);

    List<AlertRule> findByTriggerType(String triggerType);
}
