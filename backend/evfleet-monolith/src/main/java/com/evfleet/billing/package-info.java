/**
 * Billing Module
 *
 * @author SEV Platform Team
 * @version 1.0.0
 */
@org.springframework.modulith.ApplicationModule(
        displayName = "Billing Module",
        allowedDependencies = {"common", "charging::event"}
)
package com.evfleet.billing;
