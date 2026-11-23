package com.student.calculator.engine;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;

/**
 * Converts calculation results to text. The text of a very big or very small
 * number uses the form {@code 1.5\u00D710^20}, which the
 * {@link ExpressionEvaluator} can read back.
 */
public final class ResultFormatter {

    private static final int SIGNIFICANT_DIGITS = 12;
    private static final double MAX_PLAIN = 1e12;
    private static final double MIN_PLAIN = 1e-6;

    private ResultFormatter() {
    }

    /**
     * Formats a finite number with at most twelve significant digits.
     *
     * @param value the number to format
     * @return the text, for example {@code 0.5} or {@code 1\u00D710^15}
     * @throws IllegalArgumentException if the value is NaN or infinite
     */
    public static String format(double value) {
        if (Double.isNaN(value) || Double.isInfinite(value)) {
            throw new IllegalArgumentException("Value must be finite");
        }
        if (value == 0) {
            return "0";
        }
        BigDecimal rounded = new BigDecimal(value)
                .round(new MathContext(SIGNIFICANT_DIGITS,
                        RoundingMode.HALF_UP))
                .stripTrailingZeros();
        double abs = Math.abs(value);
        if (abs >= MAX_PLAIN || abs < MIN_PLAIN) {
            int exponent = rounded.precision() - rounded.scale() - 1;
            BigDecimal mantissa = rounded.movePointLeft(exponent);
            return mantissa.toPlainString() + "\u00D710^" + exponent;
        }
        return rounded.toPlainString();
    }
}
