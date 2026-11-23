package com.student.calculator.engine;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import com.student.calculator.engine.CalculationException.ErrorType;
import org.junit.Test;

public class ExpressionEvaluatorTest {

    private static final double DELTA = 1e-9;

    private final ExpressionEvaluator evaluator = new ExpressionEvaluator();

    private double deg(String expression) throws CalculationException {
        return evaluator.evaluate(expression, AngleMode.DEGREES);
    }

    private double rad(String expression) throws CalculationException {
        return evaluator.evaluate(expression, AngleMode.RADIANS);
    }

    private void assertError(ErrorType expected, String expression) {
        try {
            deg(expression);
            fail("Expected " + expected + " for: " + expression);
        } catch (CalculationException e) {
            assertEquals(expression, expected, e.getType());
        }
    }

    @Test
    public void basicArithmeticFollowsPrecedence() throws Exception {
        assertEquals(14, deg("2+3*4"), DELTA);
        assertEquals(20, deg("(2+3)*4"), DELTA);
        assertEquals(2.5, deg("10/4"), DELTA);
        assertEquals(2, deg("5-3"), DELTA);
    }

    @Test
    public void displaySymbolsAreAccepted() throws Exception {
        assertEquals(6, deg("2\u00D73"), DELTA);
        assertEquals(0.5, deg("2\u00F74"), DELTA);
        assertEquals(2, deg("5\u22123"), DELTA);
        assertEquals(Math.PI, deg("\u03C0"), DELTA);
        assertEquals(3, deg("\u221A(9)"), DELTA);
    }

    @Test
    public void whitespaceIsIgnored() throws Exception {
        assertEquals(7, deg(" 3 + 4 "), DELTA);
    }

    @Test
    public void powerIsRightAssociativeAndBindsTighterThanUnaryMinus()
            throws Exception {
        assertEquals(512, deg("2^3^2"), DELTA);
        assertEquals(-4, deg("-2^2"), DELTA);
        assertEquals(0.5, deg("2^-1"), DELTA);
    }

    @Test
    public void unaryOperatorsWork() throws Exception {
        assertEquals(-5, deg("-5"), DELTA);
        assertEquals(5, deg("--5"), DELTA);
        assertEquals(5, deg("+5"), DELTA);
        assertEquals(-6, deg("2*-3"), DELTA);
    }

    @Test
    public void factorialAndPercentWork() throws Exception {
        assertEquals(120, deg("5!"), DELTA);
        assertEquals(1, deg("0!"), DELTA);
        assertEquals(0.5, deg("50%"), DELTA);
        assertEquals(20, deg("200*10%"), DELTA);
    }

    @Test
    public void constantsAndImplicitMultiplicationWork() throws Exception {
        assertEquals(2 * Math.PI, deg("2pi"), DELTA);
        assertEquals(14, deg("2(3+4)"), DELTA);
        assertEquals(6, deg("3sqrt(4)"), DELTA);
        assertEquals(Math.E, deg("e"), DELTA);
        assertEquals(Math.PI * Math.E, deg("pie"), DELTA);
    }

    @Test
    public void trigonometryInDegrees() throws Exception {
        assertEquals(0.5, deg("sin(30)"), DELTA);
        assertEquals(0, deg("sin(180)"), 0);
        assertEquals(0, deg("cos(90)"), 0);
        assertEquals(1, deg("tan(45)"), DELTA);
        assertEquals(90, deg("asin(1)"), DELTA);
        assertEquals(0, deg("acos(1)"), DELTA);
        assertEquals(45, deg("atan(1)"), DELTA);
    }

    @Test
    public void trigonometryInRadians() throws Exception {
        assertEquals(1, rad("sin(pi/2)"), DELTA);
        assertEquals(-1, rad("cos(pi)"), DELTA);
        assertEquals(Math.PI / 2, rad("asin(1)"), DELTA);
    }

    @Test
    public void logarithmsRootsAndAbsoluteValue() throws Exception {
        assertEquals(1, deg("ln(e)"), DELTA);
        assertEquals(3, deg("log(1000)"), DELTA);
        assertEquals(4, deg("sqrt(16)"), DELTA);
        assertEquals(5, deg("abs(-5)"), DELTA);
    }

    @Test
    public void missingClosingParenthesesAreAdded() throws Exception {
        assertEquals(5, deg("(2+3"), DELTA);
        assertEquals(0.5, deg("sin(30"), DELTA);
    }

    @Test
    public void scientificOutputCanBeReadBack() throws Exception {
        assertEquals(1000, deg("1\u00D710^3"), DELTA);
        assertEquals(1.5e-7, deg("1.5\u00D710^-7"), 1e-20);
    }

    @Test
    public void malformedExpressionsAreSyntaxErrors() {
        assertError(ErrorType.SYNTAX, "");
        assertError(ErrorType.SYNTAX, "   ");
        assertError(ErrorType.SYNTAX, "2+");
        assertError(ErrorType.SYNTAX, "*3");
        assertError(ErrorType.SYNTAX, "2)");
        assertError(ErrorType.SYNTAX, "1+)");
        assertError(ErrorType.SYNTAX, "1.2.3");
        assertError(ErrorType.SYNTAX, ".");
        assertError(ErrorType.SYNTAX, "abc");
        assertError(ErrorType.SYNTAX, "sin30");
        assertError(ErrorType.SYNTAX, "()");
    }

    @Test
    public void divisionByZeroIsReported() {
        assertError(ErrorType.DIVISION_BY_ZERO, "1/0");
        assertError(ErrorType.DIVISION_BY_ZERO, "5/(3-3)");
        assertError(ErrorType.DIVISION_BY_ZERO, "0^-1");
    }

    @Test
    public void argumentsOutsideTheDomainAreReported() {
        assertError(ErrorType.DOMAIN, "sqrt(-1)");
        assertError(ErrorType.DOMAIN, "ln(0)");
        assertError(ErrorType.DOMAIN, "log(-5)");
        assertError(ErrorType.DOMAIN, "asin(2)");
        assertError(ErrorType.DOMAIN, "acos(-2)");
        assertError(ErrorType.DOMAIN, "tan(90)");
        assertError(ErrorType.DOMAIN, "(-8)^0.5");
        assertError(ErrorType.DOMAIN, "(-1)!");
        assertError(ErrorType.DOMAIN, "2.5!");
    }

    @Test
    public void tooBigResultsAreReported() {
        assertError(ErrorType.OVERFLOW, "171!");
        assertError(ErrorType.OVERFLOW, "10^400");
    }

    @Test
    public void exceptionExposesItsType() {
        CalculationException exception =
                new CalculationException(ErrorType.DOMAIN);

        assertEquals(ErrorType.DOMAIN, exception.getType());
        assertEquals("DOMAIN", exception.getMessage());
    }
}
