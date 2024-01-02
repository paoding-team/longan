package dev.paoding.longan.util;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class FinanceUtils {

    public static double subtract(double minuend, double subtrahend) {
        BigDecimal minuendBigDecimal = new BigDecimal(Double.toString(minuend));
        BigDecimal subtrahendBigDecimal = new BigDecimal(Double.toString(subtrahend));
        return minuendBigDecimal.subtract(subtrahendBigDecimal).doubleValue();
    }

    public static double add(double addend, double augend) {
        BigDecimal addendBigDecimal = new BigDecimal(Double.toString(addend));
        BigDecimal augendBigDecimal = new BigDecimal(Double.toString(augend));
        return addendBigDecimal.add(augendBigDecimal).doubleValue();
    }

    public static double multiply(double multiplier, int multiplicand) {
        BigDecimal multiplierBigDecimal = new BigDecimal(Double.toString(multiplier));
        BigDecimal multiplicandBigDecimal = new BigDecimal(Integer.toString(multiplicand));
        return multiplierBigDecimal.multiply(multiplicandBigDecimal).doubleValue();
    }

    public static double divide(double dividend, int divisor) {
        BigDecimal dividendBigDecimal = new BigDecimal(Double.toString(dividend));
        BigDecimal divisorBigDecimal = new BigDecimal(Integer.toString(divisor));
        return dividendBigDecimal.divide(divisorBigDecimal, 2, RoundingMode.HALF_UP).doubleValue();
    }

    public static double multiply(double multiplier, double multiplicand) {
        BigDecimal multiplierBigDecimal = new BigDecimal(Double.toString(multiplier));
        BigDecimal multiplicandBigDecimal = new BigDecimal(Double.toString(multiplicand));
        return multiplierBigDecimal.multiply(multiplicandBigDecimal).doubleValue();
    }

    public static double divide(double dividend, double divisor) {
        BigDecimal dividendBigDecimal = new BigDecimal(Double.toString(dividend));
        BigDecimal divisorBigDecimal = new BigDecimal(Double.toString(divisor));
        return dividendBigDecimal.divide(divisorBigDecimal, 2, RoundingMode.HALF_UP).doubleValue();
    }
}
