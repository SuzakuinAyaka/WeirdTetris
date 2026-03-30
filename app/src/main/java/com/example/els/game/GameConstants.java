package com.example.els.game;

import androidx.annotation.StringRes;

import com.example.els.R;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public final class GameConstants {
    public static final String EXTRA_MODE = "extra_mode";
    public static final String EXTRA_ITEM_MODE_ENABLED = "extra_item_mode_enabled";
    public static final String EXTRA_CUSTOM_SPEED_ENABLED = "extra_custom_speed_enabled";
    public static final String EXTRA_CUSTOM_SPEED_LEVEL = "extra_custom_speed_level";

    public static final String MODE_ENDLESS = "endless";
    public static final String MODE_CHALLENGE = "challenge";
    public static final int CUSTOM_SPEED_DEFAULT = 0;
    public static final int CUSTOM_SPEED_FAST = 1;
    public static final int CUSTOM_SPEED_VERY_FAST = 2;

    public static final String ITEM_LINE_CLEAR = "line_clear";
    public static final String ITEM_BOMB_NEXT = "bomb_next";
    public static final String ITEM_FREEZE = "freeze";
    public static final String ITEM_FRIES = "fries";
    public static final String ITEM_INCOME_BOOST = "income_boost";
    public static final String ITEM_DESTROY_CURRENT = "destroy_current";

    public static final int MAX_QUICK_SLOTS = 3;
    public static final int BOARD_ROWS = 20;
    public static final int BOARD_COLS = 10;

    public static final List<String> ITEM_IDS = Collections.unmodifiableList(
            Arrays.asList(
                    ITEM_LINE_CLEAR,
                    ITEM_BOMB_NEXT,
                    ITEM_FREEZE,
                    ITEM_FRIES,
                    ITEM_INCOME_BOOST,
                    ITEM_DESTROY_CURRENT
            )
    );

    private GameConstants() {
    }
    public static int normalizeCustomSpeedLevel(int level) {
        if (level == CUSTOM_SPEED_FAST || level == CUSTOM_SPEED_VERY_FAST) {
            return level;
        }
        return CUSTOM_SPEED_DEFAULT;
    }
    public static int getItemPrice(String itemId) {
        switch (itemId) {
            case ITEM_LINE_CLEAR:
                return 150;
            case ITEM_BOMB_NEXT:
                return 200;
            case ITEM_FREEZE:
                return 80;
            case ITEM_FRIES:
                return 80;
            case ITEM_INCOME_BOOST:
                return 100;
            case ITEM_DESTROY_CURRENT:
                return 300;
            default:
                return 0;
        }
    }

    @StringRes
    public static int getItemNameRes(String itemId) {
        switch (itemId) {
            case ITEM_LINE_CLEAR:
                return R.string.item_line_clear;
            case ITEM_BOMB_NEXT:
                return R.string.item_bomb_next;
            case ITEM_FREEZE:
                return R.string.item_freeze;
            case ITEM_FRIES:
                return R.string.item_fries;
            case ITEM_INCOME_BOOST:
                return R.string.item_income_boost;
            case ITEM_DESTROY_CURRENT:
                return R.string.item_destroy_current;
            default:
                return R.string.app_name;
        }
    }

    @StringRes
    public static int getItemDescRes(String itemId) {
        switch (itemId) {
            case ITEM_LINE_CLEAR:
                return R.string.item_desc_line_clear;
            case ITEM_BOMB_NEXT:
                return R.string.item_desc_bomb_next;
            case ITEM_FREEZE:
                return R.string.item_desc_freeze;
            case ITEM_FRIES:
                return R.string.item_desc_fries;
            case ITEM_INCOME_BOOST:
                return R.string.item_desc_income_boost;
            case ITEM_DESTROY_CURRENT:
                return R.string.item_desc_destroy_current;
            default:
                return R.string.app_name;
        }
    }
}
