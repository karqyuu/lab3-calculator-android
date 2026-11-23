package com.student.calculator.engine;

import com.student.calculator.engine.CalculationException.ErrorType;

/**
 * Evaluates scientific expressions such as {@code 2*(3+sin(30))^2}.
 *
 * <p>Supported: {@code + - * / ^ ! %}, parentheses, unary minus, implicit
 * multiplication ({@code 2pi}, {@code 3(4+5)}), the constants {@code pi} and
 * {@code e}, and the functions {@code sin cos tan asin acos atan ln log sqrt
 * abs}. The display symbols {@code \u00D7 \u00F7 \u2212 \u03C0 \u221A} are
 * accepted as well. Missing closing parentheses are added automatically.
 *
 * <p>The class does not depend on Android and can be unit tested on the JVM.
 */
public class ExpressionEvaluator {

    /**
     * Evaluates an expression.
     *
     * @param expression the text to evaluate
     * @param angleMode  unit of angles for the trigonometric functions
     * @return the value of the expression
     * @throws CalculationException if the expression is malformed or has no
     *                              defined value
     */
    public double evaluate(String expression, AngleMode angleMode)
            throws CalculationException {
        String source = normalize(expression);
        if (source.isEmpty()) {
            throw new CalculationException(ErrorType.SYNTAX);
        }
        return new Parser(source, angleMode).parse();
    }

    private static String normalize(String expression) {
        String text = expression.replaceAll("\\s+", "")
                .toLowerCase(java.util.Locale.ROOT)
                .replace('\u00D7', '*')
                .replace('\u00F7', '/')
                .replace('\u2212', '-')
                .replace("\u03C0", "pi")
                .replace("\u221A", "sqrt");
        int open = 0;
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if (c == '(') {
                open++;
            } else if (c == ')' && open > 0) {
                open--;
            }
        }
        StringBuilder balanced = new StringBuilder(text);
        for (int i = 0; i < open; i++) {
            balanced.append(')');
        }
        return balanced.toString();
    }

    /** Recursive descent parser that evaluates while it parses. */
    private static final class Parser {

        private static final int MAX_FACTORIAL = 170;
        private static final double ZERO_THRESHOLD = 1e-15;
        private static final String[] FUNCTIONS = {
            "asin", "acos", "atan", "sqrt", "sin", "cos", "tan", "abs",
            "log", "ln"
        };

        private final String source;
        private final AngleMode angleMode;
        private int position;

        Parser(String source, AngleMode angleMode) {
            this.source = source;
            this.angleMode = angleMode;
        }

        double parse() throws CalculationException {
            double value = parseExpression();
            if (position < source.length()) {
                throw syntax();
            }
            return checkResult(value);
        }

        // expression := term (('+' | '-') term)*
        private double parseExpression() throws CalculationException {
            double value = parseTerm();
            while (true) {
                if (consume('+')) {
                    value += parseTerm();
                } else if (consume('-')) {
                    value -= parseTerm();
                } else {
                    return value;
                }
            }
        }

        // term := unary (('*' | '/' | implicit) unary)*
        private double parseTerm() throws CalculationException {
            double value = parseUnary();
            while (true) {
                if (consume('*')) {
                    value *= parseUnary();
                } else if (consume('/')) {
                    double divisor = parseUnary();
                    if (divisor == 0) {
                        throw new CalculationException(
                                ErrorType.DIVISION_BY_ZERO);
                    }
                    value /= divisor;
                } else if (startsFactor()) {
                    value *= parseUnary();
                } else {
                    return value;
                }
            }
        }

        // unary := ('-' | '+') unary | power
        private double parseUnary() throws CalculationException {
            if (consume('-')) {
                return -parseUnary();
            }
            if (consume('+')) {
                return parseUnary();
            }
            return parsePower();
        }

        // power := postfix ('^' unary)?   (right associative)
        private double parsePower() throws CalculationException {
            double base = parsePostfix();
            if (consume('^')) {
                return power(base, parseUnary());
            }
            return base;
        }

        // postfix := primary ('!' | '%')*
        private double parsePostfix() throws CalculationException {
            double value = parsePrimary();
            while (true) {
                if (consume('!')) {
                    value = factorial(value);
                } else if (consume('%')) {
                    value /= 100.0;
                } else {
                    return value;
                }
            }
        }

        private double parsePrimary() throws CalculationException {
            if (consume('(')) {
                double value = parseExpression();
                expect(')');
                return value;
            }
            if (position < source.length()) {
                char c = source.charAt(position);
                if (Character.isDigit(c) || c == '.') {
                    return parseNumber();
                }
                if (Character.isLetter(c)) {
                    return parseName();
                }
            }
            throw syntax();
        }

        private double parseNumber() throws CalculationException {
            int start = position;
            while (position < source.length()
                    && (Character.isDigit(source.charAt(position))
                    || source.charAt(position) == '.')) {
                position++;
            }
            try {
                return Double.parseDouble(source.substring(start, position));
            } catch (NumberFormatException e) {
                throw syntax();
            }
        }

        private double parseName() throws CalculationException {
            if (source.startsWith("pi", position)) {
                position += 2;
                return Math.PI;
            }
            for (String function : FUNCTIONS) {
                if (source.startsWith(function, position)) {
                    position += function.length();
                    expect('(');
                    double argument = parseExpression();
                    expect(')');
                    return apply(function, argument);
                }
            }
            if (consume('e')) {
                return Math.E;
            }
            throw syntax();
        }

        private double apply(String function, double x)
                throws CalculationException {
            switch (function) {
                case "sin":
                    return clean(Math.sin(toRadians(x)));
                case "cos":
                    return clean(Math.cos(toRadians(x)));
                case "tan":
                    return tan(x);
                case "asin":
                    requireInRange(x, -1, 1);
                    return fromRadians(Math.asin(x));
                case "acos":
                    requireInRange(x, -1, 1);
                    return fromRadians(Math.acos(x));
                case "atan":
                    return fromRadians(Math.atan(x));
                case "ln":
                    requirePositive(x);
                    return Math.log(x);
                case "log":
                    requirePositive(x);
                    return Math.log10(x);
                case "sqrt":
                    if (x < 0) {
                        throw new CalculationException(ErrorType.DOMAIN);
                    }
                    return Math.sqrt(x);
                case "abs":
                    return Math.abs(x);
                default:
                    throw syntax();
            }
        }

        private double tan(double x) throws CalculationException {
            if (angleMode == AngleMode.DEGREES && Math.abs(x % 180) == 90) {
                throw new CalculationException(ErrorType.DOMAIN);
            }
            return clean(Math.tan(toRadians(x)));
        }

        private double toRadians(double x) {
            return angleMode == AngleMode.DEGREES ? Math.toRadians(x) : x;
        }

        private double fromRadians(double x) {
            return angleMode == AngleMode.DEGREES ? Math.toDegrees(x) : x;
        }

        private static double clean(double x) {
            return Math.abs(x) < ZERO_THRESHOLD ? 0.0 : x;
        }

        private static void requireInRange(double x, double min, double max)
                throws CalculationException {
            if (x < min || x > max) {
                throw new CalculationException(ErrorType.DOMAIN);
            }
        }

        private static void requirePositive(double x)
                throws CalculationException {
            if (x <= 0) {
                throw new CalculationException(ErrorType.DOMAIN);
            }
        }

        private static double power(double base, double exponent)
                throws CalculationException {
            if (base == 0 && exponent < 0) {
                throw new CalculationException(ErrorType.DIVISION_BY_ZERO);
            }
            return Math.pow(base, exponent);
        }

        private static double factorial(double x)
                throws CalculationException {
            if (x < 0 || x != Math.floor(x)) {
                throw new CalculationException(ErrorType.DOMAIN);
            }
            if (x > MAX_FACTORIAL) {
                throw new CalculationException(ErrorType.OVERFLOW);
            }
            double result = 1;
            for (int i = 2; i <= (int) x; i++) {
                result *= i;
            }
            return result;
        }

        private static double checkResult(double value)
                throws CalculationException {
            if (Double.isNaN(value)) {
                throw new CalculationException(ErrorType.DOMAIN);
            }
            if (Double.isInfinite(value)) {
                throw new CalculationException(ErrorType.OVERFLOW);
            }
            return value;
        }

        private boolean startsFactor() {
            if (position >= source.length()) {
                return false;
            }
            char c = source.charAt(position);
            return Character.isDigit(c) || c == '.' || c == '('
                    || Character.isLetter(c);
        }

        private boolean consume(char expected) {
            if (position < source.length()
                    && source.charAt(position) == expected) {
                position++;
                return true;
            }
            return false;
        }

        private void expect(char expected) throws CalculationException {
            if (!consume(expected)) {
                throw syntax();
            }
        }

        private static CalculationException syntax() {
            return new CalculationException(ErrorType.SYNTAX);
        }
    }
}
