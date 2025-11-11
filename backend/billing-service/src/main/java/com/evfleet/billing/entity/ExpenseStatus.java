package com.evfleet.billing.entity;

public enum ExpenseStatus {
    DRAFT("Draft"),
    PENDING_APPROVAL("Pending Approval"),
    APPROVED("Approved"),
    REJECTED("Rejected"),
    PAID("Paid"),
    CANCELLED("Cancelled");

    private final String displayName;

    ExpenseStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public boolean requiresAction() {
        return this == PENDING_APPROVAL;
    }

    public boolean isFinal() {
        return this == PAID || this == CANCELLED || this == REJECTED;
    }
}
