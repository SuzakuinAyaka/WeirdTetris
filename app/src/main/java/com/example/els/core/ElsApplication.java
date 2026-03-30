package com.example.els.core;

import android.app.Application;

import com.google.android.material.color.DynamicColors;

public class ElsApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        AppSettingsManager.applyNightMode(this);
        AppSettingsManager.applyLocale(this);
        if (AppSettingsManager.isDynamicColorEnabled(this)) {
            DynamicColors.applyToActivitiesIfAvailable(this);
        }
    }
}


