package com.example.els.ui.inventory;

import android.content.Intent;
import android.os.Bundle;
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
import com.example.els.ui.shop.ShopActivity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class InventoryActivity extends BaseActivity {
    private MaterialAutoCompleteTextView spSlot1;
    private MaterialAutoCompleteTextView spSlot2;

    private LinearLayout ownedItemsContainer;
    private View ownedItemsScroll;
    private TextView tvOwnedItemsEmpty;

    private String[] itemOptions = new String[0];
    private String[] optionLabels = new String[0];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inventory);

        ownedItemsContainer = findViewById(R.id.inventoryOwnedItemsContainer);
        ownedItemsScroll = findViewById(R.id.scrollInventoryOwnedItems);
        tvOwnedItemsEmpty = findViewById(R.id.tvInventoryOwnedEmpty);

        spSlot1 = findViewById(R.id.spQuickSlot1);
        spSlot2 = findViewById(R.id.spQuickSlot2);

        MaterialButton btnGoShop = findViewById(R.id.btnInventoryGoShop);
        setupQuickSlotDropdowns();
        bindSavedSlots();

        btnGoShop.setOnClickListener(v -> startActivity(new Intent(this, ShopActivity.class)));
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshOwnedItems();
        normalizeQuickSlots();
        setupQuickSlotDropdowns();
        bindSavedSlots();
    }

    private void setupQuickSlotDropdowns() {
        buildQuickSlotOptions();

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_list_item_1,
                optionLabels
        );

        bindDropdown(spSlot1, adapter, 0);
        bindDropdown(spSlot2, adapter, 1);
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

    private void bindDropdown(MaterialAutoCompleteTextView view, ArrayAdapter<String> adapter, int slotIndex) {
        view.setAdapter(adapter);
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

    private void refreshOwnedItems() {
        ownedItemsContainer.removeAllViews();
        LayoutInflater inflater = LayoutInflater.from(this);

        int shown = 0;
        for (String itemId : GameConstants.ITEM_IDS) {
            int count = AppSettingsManager.getItemCount(this, itemId);
            if (count <= 0) {
                continue;
            }

            View row = inflater.inflate(R.layout.item_inventory_owned_entry, ownedItemsContainer, false);
            TextView tvName = row.findViewById(R.id.tvOwnedItemName);
            TextView tvDesc = row.findViewById(R.id.tvOwnedItemDesc);
            TextView tvCount = row.findViewById(R.id.tvOwnedItemCount);

            tvName.setText(GameConstants.getItemNameRes(itemId));
            tvDesc.setText(GameConstants.getItemDescRes(itemId));
            tvCount.setText(getString(R.string.owned_count, count));

            ownedItemsContainer.addView(row);
            shown++;
        }

        boolean hasItems = shown > 0;
        ownedItemsScroll.setVisibility(hasItems ? View.VISIBLE : View.GONE);
        tvOwnedItemsEmpty.setVisibility(hasItems ? View.GONE : View.VISIBLE);
    }

    private void bindSavedSlots() {
        String[] slots = AppSettingsManager.getQuickSlots(this);
        spSlot1.setText(labelForItem(slots[0]), false);
        spSlot2.setText(labelForItem(slots[1]), false);
    }

    private String labelForItem(String itemId) {
        for (int i = 0; i < itemOptions.length; i++) {
            if (itemOptions[i].equals(itemId)) {
                return optionLabels[i];
            }
        }
        return optionLabels.length > 0 ? optionLabels[0] : getString(R.string.quick_slot_empty);
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
            if (itemId == null || itemId.isEmpty()) {
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
}