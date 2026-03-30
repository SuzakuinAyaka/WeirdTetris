package com.example.els.ui.mode;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Intent;
import android.graphics.Rect;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import java.util.Locale;

import com.google.android.material.materialswitch.MaterialSwitch;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;

import androidx.core.widget.NestedScrollView;

import com.example.els.R;
import com.example.els.core.AppSettingsManager;
import com.example.els.game.GameConstants;
import com.example.els.ui.base.BaseActivity;
import com.example.els.ui.game.GameActivity;
import com.example.els.ui.shop.ShopActivity;

public class ModeSelectionActivity extends BaseActivity {
    private static final long SHOP_ENTRY_ANIM_MS = 220L;
    private static final long CUSTOM_SPEED_ANIM_MS = 220L;

    private MaterialSwitch switchItemMode;
    private MaterialSwitch switchCustomSpeed;
    private TextView tvItemModeHint;
    private TextView tvModeEndlessName;
    private TextView tvModeChallengeName;
    private View cardShopEntry;
    private View cardCustomSpeedConfig;
    private MaterialAutoCompleteTextView inputCustomSpeed;
    private NestedScrollView modeScrollView;
    private ValueAnimator shopEntryAnimator;
    private ValueAnimator customSpeedAnimator;
    private int selectedCustomSpeedLevel = GameConstants.CUSTOM_SPEED_DEFAULT;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mode_selection);

        switchItemMode = findViewById(R.id.switchItemMode);
        switchCustomSpeed = findViewById(R.id.switchCustomSpeed);
        tvItemModeHint = findViewById(R.id.tvItemModeHint);
        tvModeEndlessName = findViewById(R.id.tvModeEndlessName);
        tvModeChallengeName = findViewById(R.id.tvModeChallengeName);
        cardShopEntry = findViewById(R.id.cardShopEntry);
        cardCustomSpeedConfig = findViewById(R.id.cardCustomSpeedConfig);
        inputCustomSpeed = findViewById(R.id.inputCustomSpeed);
        modeScrollView = findViewById(R.id.modeScrollView);
        View cardEndless = findViewById(R.id.cardModeEndless);
        View cardChallenge = findViewById(R.id.cardModeChallenge);

        setupCustomSpeedInput();

        switchItemMode.setChecked(AppSettingsManager.isItemModeEnabled(this));
        switchCustomSpeed.setChecked(false);
        setCustomSpeedConfigVisible(false, false);
        updateModePresentation(false);

        switchItemMode.setOnCheckedChangeListener((buttonView, isChecked) -> updateModePresentation(true));
        switchCustomSpeed.setOnCheckedChangeListener(
                (buttonView, isChecked) -> setCustomSpeedConfigVisible(isChecked, true)
        );

        cardEndless.setOnClickListener(v -> openGame(GameConstants.MODE_ENDLESS));
        cardChallenge.setOnClickListener(v -> openGame(GameConstants.MODE_CHALLENGE));
        cardShopEntry.setOnClickListener(v -> openShop());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        cancelShopEntryAnimator();
        cancelCustomSpeedAnimator();
    }

    private void setupCustomSpeedInput() {
        if (inputCustomSpeed == null) {
            return;
        }

        String[] labels = new String[]{
                getString(R.string.custom_speed_option_default),
                getString(R.string.custom_speed_option_fast),
                getString(R.string.custom_speed_option_very_fast)
        };

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_list_item_1,
                labels
        );
        inputCustomSpeed.setAdapter(adapter);
        inputCustomSpeed.setThreshold(0);
        inputCustomSpeed.setOnClickListener(v -> inputCustomSpeed.showDropDown());
        inputCustomSpeed.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                inputCustomSpeed.showDropDown();
            }
        });
        inputCustomSpeed.setOnItemClickListener((parent, view, position, id) -> {
            if (position == 1) {
                selectedCustomSpeedLevel = GameConstants.CUSTOM_SPEED_FAST;
            } else if (position == 2) {
                selectedCustomSpeedLevel = GameConstants.CUSTOM_SPEED_VERY_FAST;
            } else {
                selectedCustomSpeedLevel = GameConstants.CUSTOM_SPEED_DEFAULT;
            }
        });

        selectedCustomSpeedLevel = GameConstants.CUSTOM_SPEED_DEFAULT;
        inputCustomSpeed.setText(labels[0], false);
    }

    private void updateModePresentation(boolean animateShopEntry) {
        boolean itemEnabled = switchItemMode.isChecked();
        switchItemMode.setText(itemEnabled ? R.string.item_mode_toggle_enabled : R.string.item_mode_toggle_disabled);
        String styleName;
        if (Locale.getDefault().getLanguage().startsWith("zh")) {
            styleName = itemEnabled ? "\u9053\u5177" : "\u7ecf\u5178";
        } else {
            styleName = getString(itemEnabled
                    ? R.string.play_style_item_mode
                    : R.string.play_style_classic_mode)
                    .replace(" Mode", "")
                    .trim();
        }
        tvModeEndlessName.setText(getString(
                R.string.mode_name_with_style,
                styleName,
                getString(R.string.mode_endless)
        ));
        tvModeChallengeName.setText(getString(
                R.string.mode_name_with_style,
                styleName,
                getString(R.string.mode_challenge)
        ));

        int hintRes = itemEnabled
                ? R.string.item_mode_hint_enabled
                : R.string.item_mode_hint_disabled;
        tvItemModeHint.setText(hintRes);

        setShopEntryVisible(itemEnabled, animateShopEntry);
    }

    private void setShopEntryVisible(boolean visible, boolean animate) {
        if (cardShopEntry == null) {
            return;
        }
        cardShopEntry.setEnabled(visible);

        if (!animate) {
            cancelShopEntryAnimator();
            ViewGroup.LayoutParams params = cardShopEntry.getLayoutParams();
            params.height = ViewGroup.LayoutParams.WRAP_CONTENT;
            cardShopEntry.setLayoutParams(params);
            cardShopEntry.setAlpha(1f);
            cardShopEntry.setVisibility(visible ? View.VISIBLE : View.GONE);
            if (visible) {
                ensureCardVisible(cardShopEntry);
            }
            return;
        }

        if (visible) {
            animateShopEntryExpand();
        } else {
            animateShopEntryCollapse();
        }
    }

    private void animateShopEntryExpand() {
        cancelShopEntryAnimator();

        if (cardShopEntry.getVisibility() == View.VISIBLE && cardShopEntry.getHeight() > 0) {
            cardShopEntry.setAlpha(1f);
            cardShopEntry.setEnabled(true);
            return;
        }

        cardShopEntry.setVisibility(View.VISIBLE);
        cardShopEntry.setEnabled(true);
        cardShopEntry.setAlpha(0f);

        ViewGroup.LayoutParams params = cardShopEntry.getLayoutParams();
        params.height = ViewGroup.LayoutParams.WRAP_CONTENT;
        cardShopEntry.setLayoutParams(params);

        View parentView = (View) cardShopEntry.getParent();
        int parentWidth = parentView == null
                ? 0
                : parentView.getWidth() - parentView.getPaddingLeft() - parentView.getPaddingRight();
        if (parentWidth <= 0) {
            cardShopEntry.post(this::animateShopEntryExpand);
            return;
        }

        int widthSpec = View.MeasureSpec.makeMeasureSpec(parentWidth, View.MeasureSpec.EXACTLY);
        int heightSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
        cardShopEntry.measure(widthSpec, heightSpec);
        int targetHeight = cardShopEntry.getMeasuredHeight();
        if (targetHeight <= 0) {
            params.height = ViewGroup.LayoutParams.WRAP_CONTENT;
            cardShopEntry.setLayoutParams(params);
            cardShopEntry.setAlpha(1f);
            return;
        }

        params.height = 0;
        cardShopEntry.setLayoutParams(params);

        shopEntryAnimator = ValueAnimator.ofInt(0, targetHeight);
        shopEntryAnimator.setDuration(SHOP_ENTRY_ANIM_MS);
        shopEntryAnimator.addUpdateListener(animation -> {
            int value = (int) animation.getAnimatedValue();
            ViewGroup.LayoutParams lp = cardShopEntry.getLayoutParams();
            lp.height = value;
            cardShopEntry.setLayoutParams(lp);
            cardShopEntry.setAlpha(Math.min(1f, Math.max(0f, value * 1f / targetHeight)));
        });
        shopEntryAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                ViewGroup.LayoutParams lp = cardShopEntry.getLayoutParams();
                lp.height = ViewGroup.LayoutParams.WRAP_CONTENT;
                cardShopEntry.setLayoutParams(lp);
                cardShopEntry.setAlpha(1f);
                shopEntryAnimator = null;
                ensureCardVisible(cardShopEntry);
            }

            @Override
            public void onAnimationCancel(Animator animation) {
                shopEntryAnimator = null;
            }
        });
        shopEntryAnimator.start();
    }

    private void animateShopEntryCollapse() {
        cancelShopEntryAnimator();

        if (cardShopEntry.getVisibility() != View.VISIBLE) {
            cardShopEntry.setVisibility(View.GONE);
            cardShopEntry.setEnabled(false);
            return;
        }

        int startHeight = cardShopEntry.getHeight();
        if (startHeight <= 0) {
            cardShopEntry.setVisibility(View.GONE);
            cardShopEntry.setEnabled(false);
            return;
        }

        cardShopEntry.setEnabled(false);
        cardShopEntry.setAlpha(1f);

        shopEntryAnimator = ValueAnimator.ofInt(startHeight, 0);
        shopEntryAnimator.setDuration(SHOP_ENTRY_ANIM_MS);
        shopEntryAnimator.addUpdateListener(animation -> {
            int value = (int) animation.getAnimatedValue();
            ViewGroup.LayoutParams lp = cardShopEntry.getLayoutParams();
            lp.height = value;
            cardShopEntry.setLayoutParams(lp);
            cardShopEntry.setAlpha(Math.min(1f, Math.max(0f, value * 1f / startHeight)));
        });
        shopEntryAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                ViewGroup.LayoutParams lp = cardShopEntry.getLayoutParams();
                lp.height = ViewGroup.LayoutParams.WRAP_CONTENT;
                cardShopEntry.setLayoutParams(lp);
                cardShopEntry.setAlpha(1f);
                cardShopEntry.setVisibility(View.GONE);
                shopEntryAnimator = null;
            }

            @Override
            public void onAnimationCancel(Animator animation) {
                shopEntryAnimator = null;
            }
        });
        shopEntryAnimator.start();
    }

    private void cancelShopEntryAnimator() {
        if (shopEntryAnimator != null) {
            shopEntryAnimator.cancel();
            shopEntryAnimator = null;
        }
    }

    private void setCustomSpeedConfigVisible(boolean visible, boolean animate) {
        if (cardCustomSpeedConfig == null) {
            return;
        }
        cardCustomSpeedConfig.setEnabled(visible);

        if (!animate) {
            cancelCustomSpeedAnimator();
            ViewGroup.LayoutParams params = cardCustomSpeedConfig.getLayoutParams();
            params.height = ViewGroup.LayoutParams.WRAP_CONTENT;
            cardCustomSpeedConfig.setLayoutParams(params);
            cardCustomSpeedConfig.setAlpha(1f);
            cardCustomSpeedConfig.setVisibility(visible ? View.VISIBLE : View.GONE);
            if (visible) {
                ensureCardVisible(cardCustomSpeedConfig);
            }
            return;
        }

        if (visible) {
            animateCustomSpeedExpand();
        } else {
            animateCustomSpeedCollapse();
        }
    }

    private void animateCustomSpeedExpand() {
        cancelCustomSpeedAnimator();

        if (cardCustomSpeedConfig.getVisibility() == View.VISIBLE && cardCustomSpeedConfig.getHeight() > 0) {
            cardCustomSpeedConfig.setAlpha(1f);
            cardCustomSpeedConfig.setEnabled(true);
            return;
        }

        cardCustomSpeedConfig.setVisibility(View.VISIBLE);
        cardCustomSpeedConfig.setEnabled(true);
        cardCustomSpeedConfig.setAlpha(0f);

        ViewGroup.LayoutParams params = cardCustomSpeedConfig.getLayoutParams();
        params.height = ViewGroup.LayoutParams.WRAP_CONTENT;
        cardCustomSpeedConfig.setLayoutParams(params);

        View parentView = (View) cardCustomSpeedConfig.getParent();
        int parentWidth = parentView == null
                ? 0
                : parentView.getWidth() - parentView.getPaddingLeft() - parentView.getPaddingRight();
        if (parentWidth <= 0) {
            cardCustomSpeedConfig.post(this::animateCustomSpeedExpand);
            return;
        }

        int widthSpec = View.MeasureSpec.makeMeasureSpec(parentWidth, View.MeasureSpec.EXACTLY);
        int heightSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
        cardCustomSpeedConfig.measure(widthSpec, heightSpec);
        int targetHeight = cardCustomSpeedConfig.getMeasuredHeight();
        if (targetHeight <= 0) {
            params.height = ViewGroup.LayoutParams.WRAP_CONTENT;
            cardCustomSpeedConfig.setLayoutParams(params);
            cardCustomSpeedConfig.setAlpha(1f);
            return;
        }

        params.height = 0;
        cardCustomSpeedConfig.setLayoutParams(params);

        customSpeedAnimator = ValueAnimator.ofInt(0, targetHeight);
        customSpeedAnimator.setDuration(CUSTOM_SPEED_ANIM_MS);
        customSpeedAnimator.addUpdateListener(animation -> {
            int value = (int) animation.getAnimatedValue();
            ViewGroup.LayoutParams lp = cardCustomSpeedConfig.getLayoutParams();
            lp.height = value;
            cardCustomSpeedConfig.setLayoutParams(lp);
            cardCustomSpeedConfig.setAlpha(Math.min(1f, Math.max(0f, value * 1f / targetHeight)));
        });
        customSpeedAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                ViewGroup.LayoutParams lp = cardCustomSpeedConfig.getLayoutParams();
                lp.height = ViewGroup.LayoutParams.WRAP_CONTENT;
                cardCustomSpeedConfig.setLayoutParams(lp);
                cardCustomSpeedConfig.setAlpha(1f);
                customSpeedAnimator = null;
                ensureCardVisible(cardCustomSpeedConfig);
            }

            @Override
            public void onAnimationCancel(Animator animation) {
                customSpeedAnimator = null;
            }
        });
        customSpeedAnimator.start();
    }

    private void animateCustomSpeedCollapse() {
        cancelCustomSpeedAnimator();

        if (cardCustomSpeedConfig.getVisibility() != View.VISIBLE) {
            cardCustomSpeedConfig.setVisibility(View.GONE);
            cardCustomSpeedConfig.setEnabled(false);
            return;
        }

        int startHeight = cardCustomSpeedConfig.getHeight();
        if (startHeight <= 0) {
            cardCustomSpeedConfig.setVisibility(View.GONE);
            cardCustomSpeedConfig.setEnabled(false);
            return;
        }

        cardCustomSpeedConfig.setEnabled(false);
        cardCustomSpeedConfig.setAlpha(1f);

        customSpeedAnimator = ValueAnimator.ofInt(startHeight, 0);
        customSpeedAnimator.setDuration(CUSTOM_SPEED_ANIM_MS);
        customSpeedAnimator.addUpdateListener(animation -> {
            int value = (int) animation.getAnimatedValue();
            ViewGroup.LayoutParams lp = cardCustomSpeedConfig.getLayoutParams();
            lp.height = value;
            cardCustomSpeedConfig.setLayoutParams(lp);
            cardCustomSpeedConfig.setAlpha(Math.min(1f, Math.max(0f, value * 1f / startHeight)));
        });
        customSpeedAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                ViewGroup.LayoutParams lp = cardCustomSpeedConfig.getLayoutParams();
                lp.height = ViewGroup.LayoutParams.WRAP_CONTENT;
                cardCustomSpeedConfig.setLayoutParams(lp);
                cardCustomSpeedConfig.setAlpha(1f);
                cardCustomSpeedConfig.setVisibility(View.GONE);
                customSpeedAnimator = null;
            }

            @Override
            public void onAnimationCancel(Animator animation) {
                customSpeedAnimator = null;
            }
        });
        customSpeedAnimator.start();
    }

    private void cancelCustomSpeedAnimator() {
        if (customSpeedAnimator != null) {
            customSpeedAnimator.cancel();
            customSpeedAnimator = null;
        }
    }

    private void ensureCardVisible(View cardView) {
        if (modeScrollView == null || cardView == null || cardView.getVisibility() != View.VISIBLE) {
            return;
        }
        modeScrollView.post(() -> {
            Rect rect = new Rect();
            cardView.getDrawingRect(rect);
            modeScrollView.offsetDescendantRectToMyCoords(cardView, rect);
            rect.top = Math.max(0, rect.top - dpToPx(12));
            rect.bottom += dpToPx(24);
            modeScrollView.requestChildRectangleOnScreen(cardView, rect, true);
        });
    }

    private int dpToPx(int dp) {
        return Math.round(dp * getResources().getDisplayMetrics().density);
    }

    private void openShop() {
        startActivity(new Intent(this, ShopActivity.class));
    }

    private void openGame(String mode) {
        Intent intent = new Intent(this, GameActivity.class);
        intent.putExtra(GameConstants.EXTRA_MODE, mode);
        intent.putExtra(GameConstants.EXTRA_ITEM_MODE_ENABLED, switchItemMode.isChecked());
        intent.putExtra(GameConstants.EXTRA_CUSTOM_SPEED_ENABLED, switchCustomSpeed.isChecked());
        intent.putExtra(
                GameConstants.EXTRA_CUSTOM_SPEED_LEVEL,
                GameConstants.normalizeCustomSpeedLevel(selectedCustomSpeedLevel)
        );
        startActivity(intent);
    }
}
