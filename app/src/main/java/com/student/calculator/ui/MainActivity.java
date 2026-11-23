package com.student.calculator.ui;

import android.os.Bundle;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.widget.GridLayout;
import android.widget.TextView;
import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.annotation.StringRes;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.os.LocaleListCompat;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.student.calculator.R;
import com.student.calculator.engine.AngleMode;
import com.student.calculator.engine.CalculationException;
import com.student.calculator.engine.ExpressionEvaluator;
import com.student.calculator.engine.ResultFormatter;
import com.student.calculator.ui.KeySpec.Action;
import com.student.calculator.ui.KeySpec.Style;
import timber.log.Timber;

/**
 * The only screen of the app: a scientific calculator with a keypad, a live
 * preview of the result and an in-app language switch.
 */
public class MainActivity extends AppCompatActivity {

    private static final String STATE_EXPRESSION = "expression";
    private static final String STATE_ANGLE_MODE = "angleMode";
    private static final String STATE_SHOWING_RESULT = "showingResult";

    private static final String LABEL_DEGREES = "DEG";
    private static final String LABEL_RADIANS = "RAD";
    private static final int COLUMNS = 5;
    private static final int KEY_MARGIN_DP = 3;

    // Unicode escapes keep the source independent of the file encoding.
    private static final String MULTIPLY = "\u00D7";
    private static final String DIVIDE = "\u00F7";
    private static final String MINUS = "\u2212";
    private static final String PI = "\u03C0";
    private static final String SQRT = "\u221A";
    private static final String BACKSPACE = "\u232B";

    private static final KeySpec[] KEYS = {
        KeySpec.command(LABEL_DEGREES, Action.TOGGLE_ANGLE, Style.FUNCTION,
                R.string.cd_angle_mode),
        KeySpec.insert("(", "(", Style.FUNCTION),
        KeySpec.insert(")", ")", Style.FUNCTION),
        KeySpec.command("C", Action.CLEAR, Style.OPERATOR,
                R.string.cd_clear),
        KeySpec.command(BACKSPACE, Action.BACKSPACE, Style.OPERATOR,
                R.string.cd_backspace),

        KeySpec.insert("sin", "sin(", Style.FUNCTION),
        KeySpec.insert("cos", "cos(", Style.FUNCTION),
        KeySpec.insert("tan", "tan(", Style.FUNCTION),
        KeySpec.insert("ln", "ln(", Style.FUNCTION),
        KeySpec.insert("log", "log(", Style.FUNCTION),

        KeySpec.insert("asin", "asin(", Style.FUNCTION),
        KeySpec.insert("acos", "acos(", Style.FUNCTION),
        KeySpec.insert("atan", "atan(", Style.FUNCTION),
        KeySpec.insert(SQRT, SQRT + "(", Style.FUNCTION),
        KeySpec.insert("x^y", "^", Style.OPERATOR),

        KeySpec.insert("7", "7", Style.DIGIT),
        KeySpec.insert("8", "8", Style.DIGIT),
        KeySpec.insert("9", "9", Style.DIGIT),
        KeySpec.insert(DIVIDE, DIVIDE, Style.OPERATOR),
        KeySpec.insert(PI, PI, Style.DIGIT),

        KeySpec.insert("4", "4", Style.DIGIT),
        KeySpec.insert("5", "5", Style.DIGIT),
        KeySpec.insert("6", "6", Style.DIGIT),
        KeySpec.insert(MULTIPLY, MULTIPLY, Style.OPERATOR),
        KeySpec.insert("e", "e", Style.DIGIT),

        KeySpec.insert("1", "1", Style.DIGIT),
        KeySpec.insert("2", "2", Style.DIGIT),
        KeySpec.insert("3", "3", Style.DIGIT),
        KeySpec.insert(MINUS, MINUS, Style.OPERATOR),
        KeySpec.insert("!", "!", Style.OPERATOR),

        KeySpec.insert("0", "0", Style.DIGIT),
        KeySpec.insert(".", ".", Style.DIGIT),
        KeySpec.command("=", Action.EQUALS, Style.OPERATOR,
                R.string.cd_equals),
        KeySpec.insert("+", "+", Style.OPERATOR),
        KeySpec.insert("%", "%", Style.OPERATOR),
    };

    private final ExpressionEvaluator evaluator = new ExpressionEvaluator();
    private final StringBuilder expression = new StringBuilder();
    private AngleMode angleMode = AngleMode.DEGREES;
    private boolean showingResult;

    private TextView expressionView;
    private TextView resultView;
    private MaterialButton angleModeButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Timber.d("onCreate, restoring state: %s", savedInstanceState != null);
        setContentView(R.layout.activity_main);

        expressionView = findViewById(R.id.expressionView);
        resultView = findViewById(R.id.resultView);
        findViewById(R.id.languageButton)
                .setOnClickListener(view -> showLanguageDialog());

        if (savedInstanceState != null) {
            restoreState(savedInstanceState);
        }
        buildKeypad();
        updateAngleModeButton();
        refreshDisplay();
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(STATE_EXPRESSION, expression.toString());
        outState.putString(STATE_ANGLE_MODE, angleMode.name());
        outState.putBoolean(STATE_SHOWING_RESULT, showingResult);
    }

    private void restoreState(Bundle state) {
        expression.append(state.getString(STATE_EXPRESSION, ""));
        angleMode = AngleMode.valueOf(state.getString(STATE_ANGLE_MODE,
                AngleMode.DEGREES.name()));
        showingResult = state.getBoolean(STATE_SHOWING_RESULT, false);
    }

    // ---------------------------------------------------------- keypad

    private void buildKeypad() {
        GridLayout keypad = findViewById(R.id.keypad);
        keypad.setColumnCount(COLUMNS);
        keypad.setRowCount(KEYS.length / COLUMNS);
        LayoutInflater inflater = LayoutInflater.from(this);
        int margin = dpToPx(KEY_MARGIN_DP);

        for (KeySpec key : KEYS) {
            MaterialButton button = (MaterialButton) inflater.inflate(
                    layoutFor(key.style), keypad, false);
            button.setText(key.label);
            if (key.contentDescription != 0) {
                button.setContentDescription(
                        getString(key.contentDescription));
            }
            button.setOnClickListener(view -> onKeyPressed(key));

            GridLayout.LayoutParams params = new GridLayout.LayoutParams(
                    GridLayout.spec(GridLayout.UNDEFINED, 1f),
                    GridLayout.spec(GridLayout.UNDEFINED, 1f));
            params.width = 0;
            params.height = 0;
            params.setMargins(margin, margin, margin, margin);
            keypad.addView(button, params);

            if (key.action == Action.TOGGLE_ANGLE) {
                angleModeButton = button;
            }
        }
    }

    @LayoutRes
    private static int layoutFor(Style style) {
        switch (style) {
            case DIGIT:
                return R.layout.key_digit;
            case FUNCTION:
                return R.layout.key_function;
            default:
                return R.layout.key_operator;
        }
    }

    private int dpToPx(int dp) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                dp, getResources().getDisplayMetrics());
    }

    // ---------------------------------------------------------- actions

    private void onKeyPressed(KeySpec key) {
        Timber.d("Key pressed: %s", key.label);
        switch (key.action) {
            case INSERT:
                insert(key);
                break;
            case CLEAR:
                expression.setLength(0);
                showingResult = false;
                break;
            case BACKSPACE:
                deleteLastToken();
                showingResult = false;
                break;
            case TOGGLE_ANGLE:
                angleMode = angleMode == AngleMode.DEGREES
                        ? AngleMode.RADIANS : AngleMode.DEGREES;
                Timber.i("Angle mode changed to %s", angleMode);
                updateAngleModeButton();
                break;
            case EQUALS:
                evaluate();
                return;
            default:
                Timber.w("Unhandled key action: %s", key.action);
                break;
        }
        refreshDisplay();
    }

    private void insert(KeySpec key) {
        if (showingResult && !key.continuesResult()) {
            expression.setLength(0);
        }
        showingResult = false;
        expression.append(key.text);
    }

    /** Deletes the last symbol; a function name is deleted as a whole. */
    private void deleteLastToken() {
        int end = expression.length();
        if (end == 0) {
            return;
        }
        int start = end - 1;
        if (expression.charAt(start) == '(') {
            while (start > 0 && isFunctionChar(expression.charAt(start - 1))) {
                start--;
            }
        }
        expression.delete(start, end);
    }

    private static boolean isFunctionChar(char c) {
        return Character.isLetter(c) || c == '\u221A';
    }

    private void evaluate() {
        if (expression.length() == 0) {
            return;
        }
        String source = expression.toString();
        try {
            double value = evaluator.evaluate(source, angleMode);
            String formatted = ResultFormatter.format(value);
            Timber.i("Evaluated '%s' = %s", source, formatted);
            expression.setLength(0);
            expression.append(formatted);
            showingResult = true;
            expressionView.setText(source + " =");
            resultView.setText(formatted);
        } catch (CalculationException e) {
            Timber.w("Cannot evaluate '%s': %s", source, e.getType());
            resultView.setText(errorMessage(e.getType()));
        }
    }

    @StringRes
    private static int errorMessage(CalculationException.ErrorType type) {
        switch (type) {
            case DIVISION_BY_ZERO:
                return R.string.error_division_by_zero;
            case DOMAIN:
                return R.string.error_domain;
            case OVERFLOW:
                return R.string.error_overflow;
            default:
                return R.string.error_syntax;
        }
    }

    // ---------------------------------------------------------- display

    private void refreshDisplay() {
        expressionView.setText(expression);
        resultView.setText(preview());
    }

    /** Result shown while typing; empty if the expression is incomplete. */
    private String preview() {
        if (expression.length() == 0 || showingResult) {
            return "";
        }
        try {
            String text = ResultFormatter.format(
                    evaluator.evaluate(expression.toString(), angleMode));
            return text.equals(expression.toString()) ? "" : text;
        } catch (CalculationException e) {
            return "";
        }
    }

    private void updateAngleModeButton() {
        if (angleModeButton != null) {
            angleModeButton.setText(angleMode == AngleMode.DEGREES
                    ? LABEL_DEGREES : LABEL_RADIANS);
        }
    }

    // ---------------------------------------------------------- language

    private void showLanguageDialog() {
        String[] tags = getResources().getStringArray(R.array.language_tags);
        String[] names = getResources().getStringArray(R.array.language_names);
        names[0] = getString(R.string.language_system);

        new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.language_dialog_title)
                .setSingleChoiceItems(names, indexOfCurrentLanguage(tags),
                        (dialog, which) -> {
                            dialog.dismiss();
                            changeLanguage(tags[which]);
                        })
                .setNegativeButton(android.R.string.cancel, null)
                .show();
    }

    private int indexOfCurrentLanguage(String[] tags) {
        LocaleListCompat locales = AppCompatDelegate.getApplicationLocales();
        if (locales.isEmpty() || locales.get(0) == null) {
            return 0;
        }
        String current = locales.get(0).getLanguage();
        for (int i = 0; i < tags.length; i++) {
            if (tags[i].equals(current)) {
                return i;
            }
        }
        return 0;
    }

    /**
     * Switches the language of the app. The activity is recreated by the
     * system; the expression survives through the saved instance state.
     */
    private void changeLanguage(String languageTag) {
        Timber.i("Language changed to '%s'", languageTag);
        AppCompatDelegate.setApplicationLocales(
                LocaleListCompat.forLanguageTags(languageTag));
    }
}
