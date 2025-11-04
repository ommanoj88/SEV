package com.evfleet.charging.domain.model.valueobject;

import lombok.Value;
import java.io.Serializable;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Currency;

/**
 * Price Value Object - Immutable representation of money
 * Domain-Driven Design Value Object with business validation
 */
@Value
public class Price implements Serializable, Comparable<Price> {
    private static final long serialVersionUID = 1L;
    private static final int SCALE = 2;

    BigDecimal amount;
    Currency currency;

    public Price(BigDecimal amount, String currencyCode) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Amount cannot be null or negative");
        }
        if (currencyCode == null || currencyCode.trim().isEmpty()) {
            throw new IllegalArgumentException("Currency code cannot be null or empty");
        }

        this.amount = amount.setScale(SCALE, RoundingMode.HALF_UP);
        this.currency = Currency.getInstance(currencyCode);
    }

    public Price(double amount, String currencyCode) {
        this(BigDecimal.valueOf(amount), currencyCode);
    }

    public static Price zero(String currencyCode) {
        return new Price(BigDecimal.ZERO, currencyCode);
    }

    public static Price inr(BigDecimal amount) {
        return new Price(amount, "INR");
    }

    public static Price inr(double amount) {
        return new Price(amount, "INR");
    }

    public Price add(Price other) {
        validateSameCurrency(other);
        return new Price(this.amount.add(other.amount), this.currency.getCurrencyCode());
    }

    public Price subtract(Price other) {
        validateSameCurrency(other);
        BigDecimal result = this.amount.subtract(other.amount);
        if (result.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Subtraction would result in negative price");
        }
        return new Price(result, this.currency.getCurrencyCode());
    }

    public Price multiply(BigDecimal factor) {
        if (factor.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Multiplication factor cannot be negative");
        }
        return new Price(this.amount.multiply(factor), this.currency.getCurrencyCode());
    }

    public Price multiply(double factor) {
        return multiply(BigDecimal.valueOf(factor));
    }

    private void validateSameCurrency(Price other) {
        if (!this.currency.equals(other.currency)) {
            throw new IllegalArgumentException(
                String.format("Cannot operate on different currencies: %s and %s",
                    this.currency.getCurrencyCode(),
                    other.currency.getCurrencyCode())
            );
        }
    }

    public boolean isGreaterThan(Price other) {
        validateSameCurrency(other);
        return this.amount.compareTo(other.amount) > 0;
    }

    public boolean isLessThan(Price other) {
        validateSameCurrency(other);
        return this.amount.compareTo(other.amount) < 0;
    }

    public boolean isZero() {
        return this.amount.compareTo(BigDecimal.ZERO) == 0;
    }

    @Override
    public int compareTo(Price other) {
        validateSameCurrency(other);
        return this.amount.compareTo(other.amount);
    }

    @Override
    public String toString() {
        return String.format("%s %.2f", currency.getSymbol(), amount);
    }

    public String toFormattedString() {
        return String.format("%s %s", currency.getCurrencyCode(), amount.toPlainString());
    }
}
