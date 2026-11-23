package com.student.calculator.engine;

/**
 * Signals that an expression cannot be evaluated. It carries an
 * {@link ErrorType} instead of a text so that the user interface can show a
 * message in the language chosen by the user.
 */
public class CalculationException extends Exception {

    private static final long serialVersionUID = 1L;

    /** Reasons why an evaluation can fail. */
    public enum ErrorType {
        /** The expression is malformed. */
        SYNTAX,
        /** Division by zero. */
        DIVISION_BY_ZERO,
        /** An argument is outside the domain of a function. */
        DOMAIN,
        /** The result is too big to be represented. */
        OVERFLOW
    }

    private final ErrorType type;

    /**
     * Creates the exception.
     *
     * @param type the reason of the failure
     */
    public CalculationException(ErrorType type) {
        super(type.name());
        this.type = type;
    }

    public ErrorType getType() {
        return type;
    }
}
