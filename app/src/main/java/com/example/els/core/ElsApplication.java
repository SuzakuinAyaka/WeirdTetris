package com.example.els.core;

import android.app.Application;

import com.google.android.material.color.DynamicColors;
import com.google.android.material.color.DynamicColorsOptions;

public class ElsApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        AppSettingsManager.applyNightMode(this);
        AppSettingsManager.applyLocale(this);
        DynamicColorsOptions options = new DynamicColorsOptions.Builder()
                .setPrecondition((activity, theme) -> AppSettingsManager.isDynamicColorEnabled(activity))
                .build();
        DynamicColors.applyToActivitiesIfAvailable(this, options);
    }
}


