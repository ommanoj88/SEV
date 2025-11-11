package com.evfleet.billing.entity;

public enum ExpenseCategory {
    FUEL("Fuel"),
    CHARGING("Charging"),
    MAINTENANCE("Maintenance"),
    REPAIRS("Repairs"),
    PARTS("Parts"),
    TOLLS("Tolls"),
    PARKING("Parking"),
    INSURANCE("Insurance"),
    TAXES("Taxes"),
    DRIVER_WAGES("Driver Wages"),
    DRIVER_ALLOWANCE("Driver Allowance"),
    FINES("Fines"),
    PERMITS("Permits"),
    CLEANING("Cleaning"),
    OTHER("Other");

    private final String displayName;

    ExpenseCategory(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
