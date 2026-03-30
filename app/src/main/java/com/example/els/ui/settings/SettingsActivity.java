package com.example.els.ui.settings;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.SystemClock;
import android.text.InputType;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatDelegate;

import com.google.android.material.color.MaterialColors;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.materialswitch.MaterialSwitch;

import com.example.els.R;
import com.example.els.core.AppSettingsManager;
import com.example.els.ui.base.BaseActivity;

import java.util.ArrayDeque;

public class SettingsActivity extends BaseActivity {
    private static final int SECRET_TAP_COUNT = 5;
    private static final long SECRET_TAP_WINDOW_MS = 2200L;
    private static final long COLOR_PRESET_ANIM_MS = 220L;

    private RadioGroup rgThemeMode;
    private RadioGroup rgColorPreset;
    private View layoutColorPresetSection;
    private MaterialSwitch switchDynamicColor;
    private MaterialSwitch switchItemModeDefault;
    private MaterialSwitch switchLineClearHaptics;
    private Spinner spLanguage;
    private View cardProjectFooter;
    private TextView tvProjectVersion;
    private TextView tvProjectBuildType;
    private TextView tvProjectPackage;
    private View btnProjectRepo;

    private final ArrayDeque<Long> footerTapTimes = new ArrayDeque<>();
    private boolean suppressRealtimeCallbacks;
    private ValueAnimator colorPresetAnimator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        rgThemeMode = findViewById(R.id.rgThemeMode);
        rgColorPreset = findViewById(R.id.rgColorPreset);
        layoutColorPresetSection = findViewById(R.id.layoutColorPresetSection);
        switchDynamicColor = findViewById(R.id.switchDynamicColor);
        switchItemModeDefault = findViewById(R.id.switchDefaultItemMode);
        switchLineClearHaptics = findViewById(R.id.switchLineClearHaptics);
        spLanguage = findViewById(R.id.spLanguage);
        cardProjectFooter = findViewById(R.id.cardProjectFooter);
        tvProjectVersion = findViewById(R.id.tvProjectVersion);
        tvProjectBuildType = findViewById(R.id.tvProjectBuildType);
        tvProjectPackage = findViewById(R.id.tvProjectPackage);
        btnProjectRepo = findViewById(R.id.btnProjectRepo);

        setupLanguageSpinner();
        bindProjectInfo();
        bindCurrentValues();
        setupRealtimeListeners();
        if (cardProjectFooter != null) {
            cardProjectFooter.setOnClickListener(v -> onProjectFooterTapped());
        }
        if (btnProjectRepo != null) {
            btnProjectRepo.setOnClickListener(v -> openRepositoryPlaceholder());
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        cancelColorPresetAnimator();
    }

    private void setupLanguageSpinner() {
        final int textColor = MaterialColors.getColor(
                this,
                com.google.android.material.R.attr.colorOnSurface,
                0xFF000000
        );

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(
                this,
                android.R.layout.simple_spinner_item,
                new String[]{
                        getString(R.string.language_system),
                        getString(R.string.language_zh),
                        getString(R.string.language_en)
                }
        ) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View view = super.getView(position, convertView, parent);
                if (view instanceof TextView) {
                    ((TextView) view).setTextColor(textColor);
                }
                return view;
            }

            @Override
            public View getDropDownView(int position, View convertView, ViewGroup parent) {
                View view = super.getDropDownView(position, convertView, parent);
                if (view instanceof TextView) {
                    ((TextView) view).setTextColor(textColor);
                }
                return view;
            }
        };
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spLanguage.setAdapter(adapter);
    }

    
    private void bindProjectInfo() {
        String versionName = "-";
        try {
            PackageInfo packageInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
            if (packageInfo.versionName != null && !packageInfo.versionName.trim().isEmpty()) {
                versionName = packageInfo.versionName;
            }
        } catch (PackageManager.NameNotFoundException ignored) {
            // Keep fallback version placeholders.
        }

        String buildType = "release";

        if (tvProjectVersion != null) {
            tvProjectVersion.setText(getString(
                    R.string.settings_project_version_value,
                    versionName
            ));
        }
        if (tvProjectBuildType != null) {
            tvProjectBuildType.setText(getString(
                    R.string.settings_project_build_type_value,
                    buildType
            ));
        }
        if (tvProjectPackage != null) {
            tvProjectPackage.setText(getString(
                    R.string.settings_project_package_value,
                    getPackageName()
            ));
        }
    }

    private void bindCurrentValues() {
        suppressRealtimeCallbacks = true;

        int mode = AppSettingsManager.getThemeMode(this);
        if (mode == AppCompatDelegate.MODE_NIGHT_YES) {
            rgThemeMode.check(R.id.rbThemeDark);
        } else if (mode == AppCompatDelegate.MODE_NIGHT_NO) {
            rgThemeMode.check(R.id.rbThemeLight);
        } else {
            rgThemeMode.check(R.id.rbThemeSystem);
        }

        String preset = AppSettingsManager.getColorPreset(this);
        if (AppSettingsManager.COLOR_PRESET_GREEN.equals(preset)) {
            rgColorPreset.check(R.id.rbPresetGreen);
        } else if (AppSettingsManager.COLOR_PRESET_ORANGE.equals(preset)) {
            rgColorPreset.check(R.id.rbPresetOrange);
        } else {
            rgColorPreset.check(R.id.rbPresetBlue);
        }

        boolean dynamicEnabled = AppSettingsManager.isDynamicColorEnabled(this);
        switchDynamicColor.setChecked(dynamicEnabled);
        setColorPresetEnabled(!dynamicEnabled, false);

        switchItemModeDefault.setChecked(AppSettingsManager.isItemModeEnabled(this));
        switchLineClearHaptics.setChecked(AppSettingsManager.isLineClearHapticsEnabled(this));

        String language = AppSettingsManager.getLanguage(this);
        int position = 0;
        if (AppSettingsManager.LANGUAGE_ZH.equals(language)) {
            position = 1;
        } else if (AppSettingsManager.LANGUAGE_EN.equals(language)) {
            position = 2;
        }
        spLanguage.setSelection(position);

        suppressRealtimeCallbacks = false;
    }

    private void setupRealtimeListeners() {
        rgThemeMode.setOnCheckedChangeListener((group, checkedId) -> {
            if (suppressRealtimeCallbacks) {
                return;
            }
            int mode = AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM;
            if (checkedId == R.id.rbThemeLight) {
                mode = AppCompatDelegate.MODE_NIGHT_NO;
            } else if (checkedId == R.id.rbThemeDark) {
                mode = AppCompatDelegate.MODE_NIGHT_YES;
            }
            AppSettingsManager.setThemeMode(this, mode);
            AppSettingsManager.applyNightMode(this);
        });

        switchDynamicColor.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (suppressRealtimeCallbacks) {
                return;
            }
            AppSettingsManager.setDynamicColorEnabled(this, isChecked);
            setColorPresetEnabled(!isChecked, true);
        });

        rgColorPreset.setOnCheckedChangeListener((group, checkedId) -> {
            if (suppressRealtimeCallbacks || switchDynamicColor.isChecked()) {
                return;
            }
            String preset = AppSettingsManager.COLOR_PRESET_BLUE;
            if (checkedId == R.id.rbPresetGreen) {
                preset = AppSettingsManager.COLOR_PRESET_GREEN;
            } else if (checkedId == R.id.rbPresetOrange) {
                preset = AppSettingsManager.COLOR_PRESET_ORANGE;
            }
            AppSettingsManager.setColorPreset(this, preset);
            recreate();
        });

        switchItemModeDefault.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (suppressRealtimeCallbacks) {
                return;
            }
            AppSettingsManager.setItemModeEnabled(this, isChecked);
        });

        switchLineClearHaptics.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (suppressRealtimeCallbacks) {
                return;
            }
            AppSettingsManager.setLineClearHapticsEnabled(this, isChecked);
        });

        spLanguage.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (suppressRealtimeCallbacks) {
                    return;
                }
                String nextLanguage = AppSettingsManager.LANGUAGE_SYSTEM;
                if (position == 1) {
                    nextLanguage = AppSettingsManager.LANGUAGE_ZH;
                } else if (position == 2) {
                    nextLanguage = AppSettingsManager.LANGUAGE_EN;
                }
                if (!nextLanguage.equals(AppSettingsManager.getLanguage(SettingsActivity.this))) {
                    AppSettingsManager.setLanguage(SettingsActivity.this, nextLanguage);
                    AppSettingsManager.applyLocale(SettingsActivity.this);
                    recreate();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // No-op
            }
        });
    }

    private void setColorPresetEnabled(boolean enabled, boolean animate) {
        rgColorPreset.setEnabled(enabled);
        for (int i = 0; i < rgColorPreset.getChildCount(); i++) {
            rgColorPreset.getChildAt(i).setEnabled(enabled);
        }

        if (layoutColorPresetSection == null) {
            return;
        }

        if (!animate) {
            cancelColorPresetAnimator();
            ViewGroup.LayoutParams params = layoutColorPresetSection.getLayoutParams();
            params.height = ViewGroup.LayoutParams.WRAP_CONTENT;
            layoutColorPresetSection.setLayoutParams(params);
            layoutColorPresetSection.setAlpha(1f);
            layoutColorPresetSection.setVisibility(enabled ? View.VISIBLE : View.GONE);
            return;
        }

        if (enabled) {
            animatePresetSectionExpand(layoutColorPresetSection);
        } else {
            animatePresetSectionCollapse(layoutColorPresetSection);
        }
    }

    private void animatePresetSectionExpand(View section) {
        cancelColorPresetAnimator();

        if (section.getVisibility() == View.VISIBLE && section.getHeight() > 0) {
            section.setAlpha(1f);
            return;
        }

        section.setVisibility(View.VISIBLE);
        section.setAlpha(0f);

        ViewGroup.LayoutParams params = section.getLayoutParams();
        params.height = ViewGroup.LayoutParams.WRAP_CONTENT;
        section.setLayoutParams(params);

        View parentView = (View) section.getParent();
        int parentWidth = parentView == null
                ? 0
                : parentView.getWidth() - parentView.getPaddingLeft() - parentView.getPaddingRight();
        if (parentWidth <= 0) {
            section.post(() -> animatePresetSectionExpand(section));
            return;
        }

        int widthSpec = View.MeasureSpec.makeMeasureSpec(parentWidth, View.MeasureSpec.EXACTLY);
        int heightSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
        section.measure(widthSpec, heightSpec);
        int targetHeight = section.getMeasuredHeight();

        if (targetHeight <= 0) {
            params.height = ViewGroup.LayoutParams.WRAP_CONTENT;
            section.setLayoutParams(params);
            section.setAlpha(1f);
            return;
        }

        params.height = 0;
        section.setLayoutParams(params);

        colorPresetAnimator = ValueAnimator.ofInt(0, targetHeight);
        colorPresetAnimator.setDuration(COLOR_PRESET_ANIM_MS);
        colorPresetAnimator.addUpdateListener(animation -> {
            int value = (int) animation.getAnimatedValue();
            ViewGroup.LayoutParams lp = section.getLayoutParams();
            lp.height = value;
            section.setLayoutParams(lp);
            section.setAlpha(Math.min(1f, Math.max(0f, value * 1f / targetHeight)));
        });
        colorPresetAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                ViewGroup.LayoutParams lp = section.getLayoutParams();
                lp.height = ViewGroup.LayoutParams.WRAP_CONTENT;
                section.setLayoutParams(lp);
                section.setAlpha(1f);
                colorPresetAnimator = null;
            }

            @Override
            public void onAnimationCancel(Animator animation) {
                colorPresetAnimator = null;
            }
        });
        colorPresetAnimator.start();
    }

    private void animatePresetSectionCollapse(View section) {
        cancelColorPresetAnimator();

        if (section.getVisibility() != View.VISIBLE) {
            section.setVisibility(View.GONE);
            return;
        }

        int startHeight = section.getHeight();
        if (startHeight <= 0) {
            section.setVisibility(View.GONE);
            return;
        }

        section.setAlpha(1f);
        colorPresetAnimator = ValueAnimator.ofInt(startHeight, 0);
        colorPresetAnimator.setDuration(COLOR_PRESET_ANIM_MS);
        colorPresetAnimator.addUpdateListener(animation -> {
            int value = (int) animation.getAnimatedValue();
            ViewGroup.LayoutParams lp = section.getLayoutParams();
            lp.height = value;
            section.setLayoutParams(lp);
            section.setAlpha(Math.min(1f, Math.max(0f, value * 1f / startHeight)));
        });
        colorPresetAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                ViewGroup.LayoutParams lp = section.getLayoutParams();
                lp.height = ViewGroup.LayoutParams.WRAP_CONTENT;
                section.setLayoutParams(lp);
                section.setAlpha(1f);
                section.setVisibility(View.GONE);
                colorPresetAnimator = null;
            }

            @Override
            public void onAnimationCancel(Animator animation) {
                colorPresetAnimator = null;
            }
        });
        colorPresetAnimator.start();
    }

    private void cancelColorPresetAnimator() {
        if (colorPresetAnimator != null) {
            colorPresetAnimator.cancel();
            colorPresetAnimator = null;
        }
    }

    private void onProjectFooterTapped() {
        long now = SystemClock.elapsedRealtime();
        footerTapTimes.addLast(now);
        while (!footerTapTimes.isEmpty() && now - footerTapTimes.peekFirst() > SECRET_TAP_WINDOW_MS) {
            footerTapTimes.removeFirst();
        }
        if (footerTapTimes.size() >= SECRET_TAP_COUNT) {
            footerTapTimes.clear();
            showCoinAdjustDialog();
        }
    }

    private void showCoinAdjustDialog() {
        EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_SIGNED);
        input.setSingleLine(true);
        input.setHint(R.string.coin_panel_hint);
        input.setText(String.valueOf(AppSettingsManager.getCoins(this)));
        input.setSelection(input.getText().length());

        new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.coin_panel_title)
                .setMessage(R.string.coin_panel_message)
                .setView(input)
                .setPositiveButton(R.string.save_settings, (dialog, which) -> {
                    String raw = input.getText() == null ? "" : input.getText().toString().trim();
                    if (raw.isEmpty()) {
                        Toast.makeText(this, R.string.invalid_coin_value, Toast.LENGTH_SHORT).show();
                        return;
                    }
                    try {
                        int targetCoins = Integer.parseInt(raw);
                        AppSettingsManager.setCoins(this, targetCoins);
                        Toast.makeText(
                                this,
                                getString(R.string.coin_updated, AppSettingsManager.getCoins(this)),
                                Toast.LENGTH_SHORT
                        ).show();
                    } catch (NumberFormatException e) {
                        Toast.makeText(this, R.string.invalid_coin_value, Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    private void openRepositoryPlaceholder() {
        String url = getString(R.string.settings_project_repo_url_placeholder);
        try {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            startActivity(intent);
        } catch (Exception e) {
            Toast.makeText(this, R.string.settings_project_repo_open_failed, Toast.LENGTH_SHORT).show();
        }
    }
}
