/**
 * Driver Management Module
 *
 * @author SEV Platform Team
 * @version 1.0.0
 */
@org.springframework.modulith.ApplicationModule(
        displayName = "Driver Module",
        allowedDependencies = {"common", "fleet::event"}
)
package com.evfleet.driver;
