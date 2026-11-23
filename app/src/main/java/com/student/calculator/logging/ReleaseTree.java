package com.student.calculator.logging;

import android.util.Log;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import timber.log.Timber;

/**
 * Logging tree for release builds: only warnings and errors are written, so
 * that debug and info messages do not reach the device log of end users.
 */
public class ReleaseTree extends Timber.Tree {

    private static final String DEFAULT_TAG = "Calculator";

    @Override
    protected void log(int priority, @Nullable String tag,
            @NonNull String message, @Nullable Throwable throwable) {
        if (priority < Log.WARN) {
            return;
        }
        Log.println(priority, tag == null ? DEFAULT_TAG : tag, message);
    }
}
