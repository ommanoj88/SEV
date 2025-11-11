package com.evfleet.fleet.model;

public enum DocumentStatus {
    ACTIVE("Active - Valid"),
    EXPIRED("Expired"),
    EXPIRING_SOON("Expiring Soon"),
    PENDING_RENEWAL("Pending Renewal"),
    INVALID("Invalid/Rejected");

    private final String displayName;

    DocumentStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public boolean requiresAction() {
        return this == EXPIRED || this == EXPIRING_SOON || this == PENDING_RENEWAL;
    }
}
