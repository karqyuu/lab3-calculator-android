package com.student.calculator.ui;

import androidx.annotation.StringRes;

/**
 * Describes one key of the calculator keypad.
 */
final class KeySpec {

    /** What happens when the key is pressed. */
    enum Action {
        /** Appends text to the expression. */
        INSERT,
        /** Clears the expression. */
        CLEAR,
        /** Deletes the last symbol or function name. */
        BACKSPACE,
        /** Evaluates the expression. */
        EQUALS,
        /** Switches between degrees and radians. */
        TOGGLE_ANGLE
    }

    /** Visual style of the key. */
    enum Style {
        /** Digits and constants. */
        DIGIT,
        /** Functions and brackets. */
        FUNCTION,
        /** Operators and commands. */
        OPERATOR
    }

    final String label;
    final String text;
    final Action action;
    final Style style;
    @StringRes
    final int contentDescription;

    private KeySpec(String label, String text, Action action, Style style,
            int contentDescription) {
        this.label = label;
        this.text = text;
        this.action = action;
        this.style = style;
        this.contentDescription = contentDescription;
    }

    static KeySpec insert(String label, String text, Style style) {
        return new KeySpec(label, text, Action.INSERT, style, 0);
    }

    static KeySpec command(String label, Action action, Style style,
            @StringRes int contentDescription) {
        return new KeySpec(label, "", action, style, contentDescription);
    }

    /**
     * Tells whether pressing this key after a result continues the
     * calculation with that result (operators) or starts a new expression
     * (digits, constants, functions).
     *
     * @return true for binary and postfix operators
     */
    boolean continuesResult() {
        return action == Action.INSERT && style == Style.OPERATOR;
    }
}
