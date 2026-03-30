package com.example.els.core;

import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.view.View;
import android.view.ViewGroup;

import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;
import androidx.drawerlayout.widget.DrawerLayout;

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
        boolean isNight = (activity.getResources().getConfiguration().uiMode
                & Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES;
        WindowInsetsControllerCompat controller =
                WindowCompat.getInsetsController(activity.getWindow(), root);
        if (controller != null) {
            controller.setAppearanceLightStatusBars(!isNight);
            controller.setAppearanceLightNavigationBars(!isNight);
        }

        if (root instanceof DrawerLayout) {
            DrawerLayout drawerLayout = (DrawerLayout) root;
            if (drawerLayout.getChildCount() > 0) {
                applySystemBarInsetsToView(drawerLayout.getChildAt(0));
            }
            if (drawerLayout.getChildCount() > 1) {
                applySystemBarInsetsToView(drawerLayout.getChildAt(1));
            }
            ViewCompat.requestApplyInsets(root);
            return;
        }

        applySystemBarInsetsToView(root);
        ViewCompat.requestApplyInsets(root);
    }

    private static void applySystemBarInsetsToView(View target) {
        final int initialLeft = target.getPaddingLeft();
        final int initialTop = target.getPaddingTop();
        final int initialRight = target.getPaddingRight();
        final int initialBottom = target.getPaddingBottom();

        ViewCompat.setOnApplyWindowInsetsListener(target, (view, insets) -> {
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
    }
}
