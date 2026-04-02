package com.example.els.core;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.os.LocaleListCompat;

import com.example.els.R;
import com.example.els.game.GameConstants;

import java.util.Arrays;
import java.util.List;

public final class AppSettingsManager {
    private static final String PREFS_NAME = "weird_tetris_prefs";

    private static final String KEY_THEME_MODE = "theme_mode";
    private static final String KEY_COLOR_PRESET = "color_preset";
    private static final String KEY_DYNAMIC_COLOR = "dynamic_color";
    private static final String KEY_LANGUAGE = "language";
    private static final String KEY_ITEM_MODE_ENABLED = "item_mode_enabled";
    private static final String KEY_LINE_CLEAR_HAPTICS_ENABLED = "line_clear_haptics_enabled";
    private static final String KEY_COINS = "coins";
    private static final String KEY_HIGH_SCORE = "high_score";
    private static final String KEY_QUICK_SLOTS = "quick_slots";
    private static final String KEY_LAST_SYSTEM_LANGUAGE_TAG = "last_system_language_tag";
    private static final String KEY_SYSTEM_LANGUAGE_CHANGE_PENDING = "system_language_change_pending";

    public static final String COLOR_PRESET_BLUE = "blue";
    public static final String COLOR_PRESET_GREEN = "green";
    public static final String COLOR_PRESET_ORANGE = "orange";

    public static final String LANGUAGE_SYSTEM = "system";
    public static final String LANGUAGE_ZH = "zh";
    public static final String LANGUAGE_EN = "en";

    private AppSettingsManager() {
    }

    private static SharedPreferences prefs(Context context) {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    public static void applyNightMode(Context context) {
        AppCompatDelegate.setDefaultNightMode(getThemeMode(context));
    }

    public static void applyLocale(Context context) {
        String language = getLanguage(context);
        LocaleListCompat locales = LANGUAGE_SYSTEM.equals(language)
                ? LocaleListCompat.getEmptyLocaleList()
                : LocaleListCompat.forLanguageTags(language);
        AppCompatDelegate.setApplicationLocales(locales);
    }

    public static int resolveThemeRes(Context context) {
        if (isDynamicColorEnabled(context)) {
            return R.style.Theme_Els;
        }
        String preset = getColorPreset(context);
        if (COLOR_PRESET_GREEN.equals(preset)) {
            return R.style.Theme_Els_Green;
        }
        if (COLOR_PRESET_ORANGE.equals(preset)) {
            return R.style.Theme_Els_Orange;
        }
        return R.style.Theme_Els_Blue;
    }

    public static int getThemeMode(Context context) {
        return prefs(context).getInt(KEY_THEME_MODE, AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
    }

    public static void setThemeMode(Context context, int mode) {
        prefs(context).edit().putInt(KEY_THEME_MODE, mode).apply();
    }

    public static String getColorPreset(Context context) {
        String preset = prefs(context).getString(KEY_COLOR_PRESET, COLOR_PRESET_BLUE);
        return preset == null ? COLOR_PRESET_BLUE : preset;
    }

    public static void setColorPreset(Context context, String preset) {
        prefs(context).edit().putString(KEY_COLOR_PRESET, preset).apply();
    }

    public static boolean isDynamicColorEnabled(Context context) {
        return prefs(context).getBoolean(KEY_DYNAMIC_COLOR, true);
    }

    public static void setDynamicColorEnabled(Context context, boolean enabled) {
        prefs(context).edit().putBoolean(KEY_DYNAMIC_COLOR, enabled).apply();
    }

    public static String getLanguage(Context context) {
        String language = prefs(context).getString(KEY_LANGUAGE, LANGUAGE_SYSTEM);
        return language == null ? LANGUAGE_SYSTEM : language;
    }

    public static void setLanguage(Context context, String language) {
        prefs(context).edit().putString(KEY_LANGUAGE, language).apply();
    }

    public static void refreshSystemLanguageChangeFlag(Context context) {
        String currentSystemLanguageTag = resolveSystemLanguageTag();
        SharedPreferences preferences = prefs(context);

        if (!LANGUAGE_SYSTEM.equals(getLanguage(context))) {
            preferences.edit()
                    .putString(KEY_LAST_SYSTEM_LANGUAGE_TAG, currentSystemLanguageTag)
                    .putBoolean(KEY_SYSTEM_LANGUAGE_CHANGE_PENDING, false)
                    .apply();
            return;
        }

        String lastSystemLanguageTag = preferences.getString(KEY_LAST_SYSTEM_LANGUAGE_TAG, "");
        if (lastSystemLanguageTag == null || lastSystemLanguageTag.trim().isEmpty()) {
            preferences.edit()
                    .putString(KEY_LAST_SYSTEM_LANGUAGE_TAG, currentSystemLanguageTag)
                    .apply();
            return;
        }

        if (!currentSystemLanguageTag.equals(lastSystemLanguageTag)) {
            preferences.edit()
                    .putString(KEY_LAST_SYSTEM_LANGUAGE_TAG, currentSystemLanguageTag)
                    .putBoolean(KEY_SYSTEM_LANGUAGE_CHANGE_PENDING, true)
                    .apply();
        }
    }

    public static boolean consumeSystemLanguageChangePending(Context context) {
        SharedPreferences preferences = prefs(context);
        boolean pending = preferences.getBoolean(KEY_SYSTEM_LANGUAGE_CHANGE_PENDING, false);
        if (!pending) {
            return false;
        }
        preferences.edit().putBoolean(KEY_SYSTEM_LANGUAGE_CHANGE_PENDING, false).apply();
        return true;
    }

    public static boolean isItemModeEnabled(Context context) {
        return prefs(context).getBoolean(KEY_ITEM_MODE_ENABLED, false);
    }

    public static void setItemModeEnabled(Context context, boolean enabled) {
        prefs(context).edit().putBoolean(KEY_ITEM_MODE_ENABLED, enabled).apply();
    }

    public static boolean isLineClearHapticsEnabled(Context context) {
        return prefs(context).getBoolean(KEY_LINE_CLEAR_HAPTICS_ENABLED, false);
    }

    public static void setLineClearHapticsEnabled(Context context, boolean enabled) {
        prefs(context).edit().putBoolean(KEY_LINE_CLEAR_HAPTICS_ENABLED, enabled).apply();
    }

    public static int getCoins(Context context) {
        return prefs(context).getInt(KEY_COINS, 0);
    }

    public static void setCoins(Context context, int coins) {
        prefs(context).edit().putInt(KEY_COINS, Math.max(0, coins)).apply();
    }

    public static void addCoins(Context context, int delta) {
        setCoins(context, getCoins(context) + delta);
    }

    public static int getHighScore(Context context) {
        return prefs(context).getInt(KEY_HIGH_SCORE, 0);
    }

    public static void setHighScore(Context context, int highScore) {
        prefs(context).edit().putInt(KEY_HIGH_SCORE, Math.max(0, highScore)).apply();
    }

    public static int getItemCount(Context context, String itemId) {
        return prefs(context).getInt("item_count_" + itemId, 0);
    }

    public static void addItemCount(Context context, String itemId, int delta) {
        int current = getItemCount(context, itemId);
        int next = Math.max(0, current + delta);
        prefs(context).edit().putInt("item_count_" + itemId, next).apply();
    }

    public static boolean consumeItem(Context context, String itemId) {
        int count = getItemCount(context, itemId);
        if (count <= 0) {
            return false;
        }
        addItemCount(context, itemId, -1);
        return true;
    }

    public static String[] getQuickSlots(Context context) {
        String raw = prefs(context).getString(KEY_QUICK_SLOTS, "");
        String[] slots = new String[GameConstants.MAX_QUICK_SLOTS];
        Arrays.fill(slots, "");
        if (raw == null || raw.trim().isEmpty()) {
            return slots;
        }
        String[] parts = raw.split(",", -1);
        for (int i = 0; i < GameConstants.MAX_QUICK_SLOTS && i < parts.length; i++) {
            slots[i] = sanitizeItemId(parts[i].trim());
        }
        return slots;
    }

    public static void setQuickSlots(Context context, String[] slots) {
        if (slots == null) {
            prefs(context).edit().remove(KEY_QUICK_SLOTS).apply();
            return;
        }
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < GameConstants.MAX_QUICK_SLOTS; i++) {
            if (i > 0) {
                builder.append(",");
            }
            String id = i < slots.length ? sanitizeItemId(slots[i]) : "";
            builder.append(id);
        }
        prefs(context).edit().putString(KEY_QUICK_SLOTS, builder.toString()).apply();
    }

    private static String sanitizeItemId(String itemId) {
        if (itemId == null) {
            return "";
        }
        List<String> allItems = GameConstants.ITEM_IDS;
        return allItems.contains(itemId) ? itemId : "";
    }

    private static String resolveSystemLanguageTag() {
        LocaleListCompat localeList = LocaleListCompat.getAdjustedDefault();
        if (localeList.isEmpty() || localeList.get(0) == null) {
            return "";
        }
        String languageTag = localeList.get(0).toLanguageTag();
        return languageTag == null ? "" : languageTag;
    }
}


