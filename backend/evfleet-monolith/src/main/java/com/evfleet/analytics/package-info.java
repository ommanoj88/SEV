/**
 * Analytics Module
 *
 * @author SEV Platform Team
 * @version 1.0.0
 */
@org.springframework.modulith.ApplicationModule(
        displayName = "Analytics Module",
        allowedDependencies = {"common", "fleet::event", "charging::event"}
)
package com.evfleet.analytics;
