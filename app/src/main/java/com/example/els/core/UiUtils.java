package com.example.els.core;

import android.app.Activity;
import android.content.res.Configuration;
import android.content.pm.ActivityInfo;
import android.view.View;
import android.view.ViewGroup;

import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsControllerCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.els.R;

public final class UiUtils {
    private UiUtils() {
    }

    public static boolean isTablet(Activity activity) {
        return activity.getResources().getBoolean(R.bool.is_tablet);
    }

    public static void enforceOrientation(Activity activity) {
        if (isTablet(activity)) {
            activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
        } else {
            activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }
    }

    public static void applyStatusBarInsets(Activity activity) {
        WindowCompat.setDecorFitsSystemWindows(activity.getWindow(), false);

        ViewGroup content = activity.findViewById(android.R.id.content);
        if (content == null || content.getChildCount() == 0) {
            return;
        }

        View root = content.getChildAt(0);
        final int initialLeft = root.getPaddingLeft();
        final int initialTop = root.getPaddingTop();
        final int initialRight = root.getPaddingRight();
        final int initialBottom = root.getPaddingBottom();
        boolean isNight = (activity.getResources().getConfiguration().uiMode
                & Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES;
        WindowInsetsControllerCompat controller =
                WindowCompat.getInsetsController(activity.getWindow(), root);
        if (controller != null) {
            controller.setAppearanceLightStatusBars(!isNight);
            controller.setAppearanceLightNavigationBars(!isNight);
        }

        ViewCompat.setOnApplyWindowInsetsListener(root, (view, insets) -> {
            Insets systemInsets = insets.getInsets(
                    WindowInsetsCompat.Type.systemBars() | WindowInsetsCompat.Type.displayCutout()
            );
            view.setPadding(
                    initialLeft + systemInsets.left,
                    initialTop + systemInsets.top,
                    initialRight + systemInsets.right,
                    initialBottom + systemInsets.bottom
            );
            return insets;
        });
        ViewCompat.requestApplyInsets(root);
    }
}


