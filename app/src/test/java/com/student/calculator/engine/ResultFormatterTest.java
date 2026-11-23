package com.student.calculator.engine;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import org.junit.Test;

public class ResultFormatterTest {

    @Test
    public void integersHaveNoDecimalPart() {
        assertEquals("14", ResultFormatter.format(14.0));
        assertEquals("-3", ResultFormatter.format(-3.0));
        assertEquals("100", ResultFormatter.format(100.0));
        assertEquals("123456789012", ResultFormatter.format(123456789012.0));
    }

    @Test
    public void zeroIsPrintedWithoutSign() {
        assertEquals("0", ResultFormatter.format(0.0));
        assertEquals("0", ResultFormatter.format(-0.0));
    }

    @Test
    public void floatingPointNoiseIsRounded() {
        assertEquals("0.3", ResultFormatter.format(0.1 + 0.2));
        assertEquals("0.5", ResultFormatter.format(0.49999999999999994));
        assertEquals("0.333333333333", ResultFormatter.format(1.0 / 3));
    }

    @Test
    public void veryBigAndVerySmallNumbersUseScientificForm() {
        assertEquals("1\u00D710^12", ResultFormatter.format(1e12));
        assertEquals("1.5\u00D710^20", ResultFormatter.format(1.5e20));
        assertEquals("2.5\u00D710^-7", ResultFormatter.format(2.5e-7));
        assertEquals("-1.5\u00D710^20", ResultFormatter.format(-1.5e20));
    }

    @Test
    public void formattedResultCanBeEvaluatedAgain() throws Exception {
        ExpressionEvaluator evaluator = new ExpressionEvaluator();
        double original = 1.5e20;

        double again = evaluator.evaluate(ResultFormatter.format(original),
                AngleMode.DEGREES);

        assertEquals(original, again, 1e6);
    }

    @Test
    public void nonFiniteValuesAreRejected() {
        for (double bad : new double[] {Double.NaN, Double.POSITIVE_INFINITY,
            Double.NEGATIVE_INFINITY}) {
            try {
                ResultFormatter.format(bad);
                fail("Expected IllegalArgumentException for " + bad);
            } catch (IllegalArgumentException expected) {
                // expected
            }
        }
    }
}
