package dev.paoding.longan.math;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class Money {
    private BigDecimal amount;

    public static Money of(Double quantity) {
        return new Money(quantity);
    }

    private Money(Double quantity) {
        this.amount = wrap(quantity);
    }

    public static Money of(Integer quantity) {
        return new Money(quantity);
    }

    private Money(Integer quantity) {
        this.amount = wrap(quantity);
    }

    public Money add(Double quantity) {
        amount = amount.add(wrap(quantity));
        return this;
    }

    public Money add(Integer quantity) {
        amount = amount.add(wrap(quantity));
        return this;
    }

    public Money subtract(Double quantity) {
        amount = amount.subtract(wrap(quantity));
        return this;
    }

    public Money subtract(Integer quantity) {
        amount = amount.subtract(wrap(quantity));
        return this;
    }

    public Money multiply(Double quantity) {
        amount = amount.multiply(wrap(quantity));
        return this;
    }

    public Money multiply(Integer quantity) {
        amount = amount.multiply(wrap(quantity));
        return this;
    }

    public Money divide(Double quantity) {
        amount = amount.divide(wrap(quantity), 2, RoundingMode.HALF_UP);
        return this;
    }

    public Money divide(Integer quantity) {
        amount = amount.divide(wrap(quantity), 2, RoundingMode.HALF_UP);
        return this;
    }

    private BigDecimal wrap(Double quantity) {
        return new BigDecimal(Double.toString(quantity));
    }

    private BigDecimal wrap(Integer quantity) {
        return new BigDecimal(Integer.toString(quantity));
    }

    public Double doubleValue() {
        return amount.doubleValue();
    }
}
