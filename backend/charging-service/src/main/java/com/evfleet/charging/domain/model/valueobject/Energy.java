package com.evfleet.charging.domain.model.valueobject;

import lombok.Value;
import java.io.Serializable;
import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Energy Value Object - Represents energy in kilowatt-hours (kWh)
 * Domain-Driven Design Value Object for energy measurements
 */
@Value
public class Energy implements Serializable, Comparable<Energy> {
    private static final long serialVersionUID = 1L;
    private static final int SCALE = 3;

    BigDecimal kwh;

    public Energy(BigDecimal kwh) {
        if (kwh == null || kwh.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Energy cannot be null or negative");
        }
        this.kwh = kwh.setScale(SCALE, RoundingMode.HALF_UP);
    }

    public Energy(double kwh) {
        this(BigDecimal.valueOf(kwh));
    }

    public static Energy zero() {
        return new Energy(BigDecimal.ZERO);
    }

    public static Energy fromWh(BigDecimal wh) {
        return new Energy(wh.divide(BigDecimal.valueOf(1000), SCALE, RoundingMode.HALF_UP));
    }

    public Energy add(Energy other) {
        return new Energy(this.kwh.add(other.kwh));
    }

    public Energy subtract(Energy other) {
        BigDecimal result = this.kwh.subtract(other.kwh);
        if (result.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Subtraction would result in negative energy");
        }
        return new Energy(result);
    }

    public Energy multiply(BigDecimal factor) {
        if (factor.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Multiplication factor cannot be negative");
        }
        return new Energy(this.kwh.multiply(factor));
    }

    public BigDecimal toWh() {
        return kwh.multiply(BigDecimal.valueOf(1000));
    }

    public boolean isGreaterThan(Energy other) {
        return this.kwh.compareTo(other.kwh) > 0;
    }

    public boolean isZero() {
        return this.kwh.compareTo(BigDecimal.ZERO) == 0;
    }

    @Override
    public int compareTo(Energy other) {
        return this.kwh.compareTo(other.kwh);
    }

    @Override
    public String toString() {
        return String.format("%.3f kWh", kwh);
    }
}
