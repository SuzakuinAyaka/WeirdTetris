package com.example.els.ui.shop;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;

import com.example.els.R;
import com.example.els.core.AppSettingsManager;
import com.example.els.game.GameConstants;
import com.example.els.ui.base.BaseActivity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ShopActivity extends BaseActivity {
    private static final long BRIEF_TOAST_MS = 500L;

    private TextView tvCoins;
    private MaterialAutoCompleteTextView spSlot1;
    private MaterialAutoCompleteTextView spSlot2;

    private final Handler toastHandler = new Handler(Looper.getMainLooper());
    private Toast activeToast;

    private String[] itemOptions = new String[0];
    private String[] optionLabels = new String[0];

    private final Map<String, ItemViewHolder> itemViews = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shop);

        tvCoins = findViewById(R.id.tvShopCoins);
        spSlot1 = findViewById(R.id.spQuickSlot1);
        spSlot2 = findViewById(R.id.spQuickSlot2);
        LinearLayout container = findViewById(R.id.shopItemsContainer);

        populateItems(container);
        bindDropdownInteractions();
        normalizeQuickSlots();
        refreshQuickSlotSection();
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshUi();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        toastHandler.removeCallbacksAndMessages(null);
        if (activeToast != null) {
            activeToast.cancel();
            activeToast = null;
        }
    }

    private void populateItems(LinearLayout container) {
        LayoutInflater inflater = LayoutInflater.from(this);
        itemViews.clear();
        for (String itemId : GameConstants.ITEM_IDS) {
            View itemView = inflater.inflate(R.layout.item_shop_entry, container, false);
            TextView tvName = itemView.findViewById(R.id.tvShopItemName);
            TextView tvDesc = itemView.findViewById(R.id.tvShopItemDesc);
            TextView tvPrice = itemView.findViewById(R.id.tvShopItemPrice);
            TextView tvOwned = itemView.findViewById(R.id.tvShopItemOwned);
            MaterialButton btnBuy = itemView.findViewById(R.id.btnBuyItem);

            tvName.setText(GameConstants.getItemNameRes(itemId));
            tvDesc.setText(GameConstants.getItemDescRes(itemId));
            tvPrice.setText(getString(R.string.buy_for_price, GameConstants.getItemPrice(itemId)));
            btnBuy.setText(R.string.buy_now);
            btnBuy.setOnClickListener(v -> buyItem(itemId));

            container.addView(itemView);
            itemViews.put(itemId, new ItemViewHolder(tvPrice, tvOwned, btnBuy));
        }
    }

    private void buyItem(String itemId) {
        int price = GameConstants.getItemPrice(itemId);
        int coins = AppSettingsManager.getCoins(this);
        if (coins < price) {
            showBriefToast(getString(R.string.not_enough_coins));
            return;
        }
        AppSettingsManager.setCoins(this, coins - price);
        AppSettingsManager.addItemCount(this, itemId, 1);
        refreshUi();
        showBriefToast(getString(R.string.purchase_success, getString(GameConstants.getItemNameRes(itemId))));
    }

    private void refreshUi() {
        tvCoins.setText(getString(R.string.coins_value, AppSettingsManager.getCoins(this)));
        for (String itemId : GameConstants.ITEM_IDS) {
            ItemViewHolder holder = itemViews.get(itemId);
            if (holder == null) {
                continue;
            }
            int count = AppSettingsManager.getItemCount(this, itemId);
            int price = GameConstants.getItemPrice(itemId);
            holder.tvPrice.setText(getString(R.string.buy_for_price, price));
            holder.tvOwned.setText(getString(R.string.owned_count, count));
            holder.btnBuy.setText(R.string.buy_now);
        }

        normalizeQuickSlots();
        refreshQuickSlotSection();
    }

    private void bindDropdownInteractions() {
        bindDropdown(spSlot1, 0);
        bindDropdown(spSlot2, 1);
    }

    private void bindDropdown(MaterialAutoCompleteTextView view, int slotIndex) {
        view.setThreshold(0);
        view.setOnClickListener(v -> view.showDropDown());
        view.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                view.showDropDown();
            }
        });
        view.setOnItemClickListener((parent, selectedView, position, id) -> {
            String selectedItemId = position >= 0 && position < itemOptions.length ? itemOptions[position] : "";
            applyQuickSlotSelection(slotIndex, selectedItemId);
        });
    }

    private void refreshQuickSlotSection() {
        buildQuickSlotOptions();
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_list_item_1,
                optionLabels
        );
        spSlot1.setAdapter(adapter);
        spSlot2.setAdapter(adapter);
        bindSavedSlots();
    }

    private void buildQuickSlotOptions() {
        List<String> optionIdList = new ArrayList<>();
        List<String> optionLabelList = new ArrayList<>();

        optionIdList.add("");
        optionLabelList.add(getString(R.string.quick_slot_empty));

        for (String itemId : GameConstants.ITEM_IDS) {
            int count = AppSettingsManager.getItemCount(this, itemId);
            if (count <= 0) {
                continue;
            }
            optionIdList.add(itemId);
            optionLabelList.add(getString(GameConstants.getItemNameRes(itemId)) + "(" + count + ")");
        }

        itemOptions = optionIdList.toArray(new String[0]);
        optionLabels = optionLabelList.toArray(new String[0]);
    }

    private void bindSavedSlots() {
        String[] slots = AppSettingsManager.getQuickSlots(this);
        spSlot1.setText(displayLabelForItem(slots[0]), false);
        spSlot2.setText(displayLabelForItem(slots[1]), false);
    }

    private String displayLabelForItem(String itemId) {
        if (TextUtils.isEmpty(itemId)) {
            return getString(R.string.quick_slot_empty);
        }
        return getString(GameConstants.getItemNameRes(itemId));
    }

    private void normalizeQuickSlots() {
        String[] slots = AppSettingsManager.getQuickSlots(this);
        Map<String, Integer> selectedCount = new HashMap<>();
        boolean changed = false;

        if (slots.length > 2 && !TextUtils.isEmpty(slots[2])) {
            slots[2] = "";
            changed = true;
        }

        for (int i = 0; i < 2 && i < slots.length; i++) {
            String itemId = slots[i];
            if (TextUtils.isEmpty(itemId)) {
                continue;
            }
            int owned = AppSettingsManager.getItemCount(this, itemId);
            int used = selectedCount.containsKey(itemId) ? selectedCount.get(itemId) : 0;
            if (owned <= 0 || used >= owned) {
                slots[i] = "";
                changed = true;
                continue;
            }
            selectedCount.put(itemId, used + 1);
        }

        if (changed) {
            AppSettingsManager.setQuickSlots(this, slots);
        }
    }

    private void applyQuickSlotSelection(int slotIndex, String selectedItemId) {
        String[] selected = AppSettingsManager.getQuickSlots(this);
        if (slotIndex < 0 || slotIndex >= 2 || slotIndex >= selected.length) {
            return;
        }

        selected[slotIndex] = selectedItemId == null ? "" : selectedItemId;
        if (selected.length > 2) {
            selected[2] = "";
        }

        Map<String, Integer> selectedCount = new HashMap<>();
        for (int i = 0; i < 2 && i < selected.length; i++) {
            String itemId = selected[i];
            if (TextUtils.isEmpty(itemId)) {
                continue;
            }
            int current = selectedCount.containsKey(itemId) ? selectedCount.get(itemId) : 0;
            selectedCount.put(itemId, current + 1);
        }

        for (Map.Entry<String, Integer> entry : selectedCount.entrySet()) {
            int owned = AppSettingsManager.getItemCount(this, entry.getKey());
            if (entry.getValue() > owned) {
                Toast.makeText(this, R.string.quick_slot_exceed_inventory, Toast.LENGTH_SHORT).show();
                bindSavedSlots();
                return;
            }
        }

        AppSettingsManager.setQuickSlots(this, selected);
        bindSavedSlots();
    }

    private void showBriefToast(String message) {
        if (activeToast != null) {
            activeToast.cancel();
        }
        Toast toast = Toast.makeText(this, message, Toast.LENGTH_SHORT);
        activeToast = toast;
        toast.show();

        toastHandler.removeCallbacksAndMessages(null);
        toastHandler.postDelayed(() -> {
            if (activeToast == toast) {
                toast.cancel();
                activeToast = null;
            }
        }, BRIEF_TOAST_MS);
    }

    private static class ItemViewHolder {
        final TextView tvPrice;
        final TextView tvOwned;
        final MaterialButton btnBuy;

        ItemViewHolder(TextView tvPrice, TextView tvOwned, MaterialButton btnBuy) {
            this.tvPrice = tvPrice;
            this.tvOwned = tvOwned;
            this.btnBuy = btnBuy;
        }
    }
}