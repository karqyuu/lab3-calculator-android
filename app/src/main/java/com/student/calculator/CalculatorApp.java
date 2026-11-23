package com.student.calculator;

import android.app.Application;
import com.student.calculator.logging.ReleaseTree;
import timber.log.Timber;

/**
 * Application class. Configures logging once for the whole app:
 * debug builds log everything with automatic tags, release builds log only
 * warnings and errors.
 */
public class CalculatorApp extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        if (BuildConfig.DEBUG) {
            Timber.plant(new Timber.DebugTree());
        } else {
            Timber.plant(new ReleaseTree());
        }
        Timber.i("Application started, version %s", BuildConfig.VERSION_NAME);
    }
}
