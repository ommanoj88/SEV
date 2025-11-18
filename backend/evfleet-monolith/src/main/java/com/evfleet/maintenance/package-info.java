/**
 * Maintenance Management Module
 *
 * @author SEV Platform Team
 * @version 1.0.0
 */
@org.springframework.modulith.ApplicationModule(
        displayName = "Maintenance Module",
        allowedDependencies = {"common", "fleet::event"}
)
package com.evfleet.maintenance;
