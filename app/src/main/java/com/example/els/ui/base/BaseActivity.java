package com.example.els.ui.base;

import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.els.core.AppSettingsManager;
import com.example.els.core.UiUtils;

public abstract class BaseActivity extends AppCompatActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        AppSettingsManager.applyNightMode(this);
        AppSettingsManager.applyLocale(this);
        setTheme(AppSettingsManager.resolveThemeRes(this));
        super.onCreate(savedInstanceState);
        UiUtils.enforceOrientation(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        UiUtils.enforceOrientation(this);
    }

    @Override
    public void setContentView(int layoutResID) {
        super.setContentView(layoutResID);
        UiUtils.applyStatusBarInsets(this);
    }

    @Override
    public void setContentView(View view) {
        super.setContentView(view);
        UiUtils.applyStatusBarInsets(this);
    }

    @Override
    public void setContentView(View view, ViewGroup.LayoutParams params) {
        super.setContentView(view, params);
        UiUtils.applyStatusBarInsets(this);
    }
}