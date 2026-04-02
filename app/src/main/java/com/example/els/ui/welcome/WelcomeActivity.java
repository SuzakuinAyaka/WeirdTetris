package com.example.els.ui.welcome;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import com.example.els.R;
import com.example.els.core.AppSettingsManager;
import com.example.els.game.GameConstants;
import com.example.els.game.Tetromino;
import com.example.els.ui.base.BaseActivity;
import com.example.els.ui.mode.ModeSelectionActivity;
import com.example.els.ui.settings.SettingsActivity;
import com.example.els.ui.shop.ShopActivity;
import com.example.els.ui.tutorial.TutorialActivity;
import com.example.els.ui.widget.TetrisBoardView;

import java.util.Arrays;
import java.util.Random;

public class WelcomeActivity extends BaseActivity {
    private static final long PREVIEW_TICK_MS = 170L;

    private final Handler previewHandler = new Handler(Looper.getMainLooper());
    private final Runnable previewTickRunnable = this::onPreviewTick;
    private final int[][] previewBoard = new int[GameConstants.BOARD_ROWS][GameConstants.BOARD_COLS];
    private final Random previewRandom = new Random();

    private View cardStartGame;
    private View cardTutorial;
    private View cardShop;
    private View cardSettings;
    private View cardLanguage;
    private TetrisBoardView previewBoardView;

    private Tetromino.BagGenerator previewBag;
    private Tetromino previewPiece;
    private boolean previewRunning;

    private int appliedThemeRes;
    private int appliedNightMode;
    private String appliedLanguage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);

        cardStartGame = findViewById(R.id.cardStartGame);
        cardTutorial = findViewById(R.id.cardTutorial);
        cardSettings = findViewById(R.id.cardSettings);
        cardLanguage = findViewById(R.id.cardLanguage);
        cardShop = findViewById(R.id.cardGoShop);
        previewBoardView = findViewById(R.id.welcomePreviewBoard);

        cardStartGame.setOnClickListener(v ->
                startActivity(new Intent(WelcomeActivity.this, ModeSelectionActivity.class))
        );
        cardTutorial.setOnClickListener(v ->
                startActivity(new Intent(WelcomeActivity.this, TutorialActivity.class))
        );
        cardSettings.setOnClickListener(v ->
                startActivity(new Intent(WelcomeActivity.this, SettingsActivity.class))
        );
        cardLanguage.setOnClickListener(v -> showLanguagePicker());
        cardShop.setOnClickListener(v ->
                startActivity(new Intent(WelcomeActivity.this, ShopActivity.class))
        );
        captureAppliedAppearance();
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (shouldRefreshAppearance()) {
            captureAppliedAppearance();
            recreate();
            return;
        }


        startPreviewAnimation();
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopPreviewAnimation();
    }

    private boolean shouldRefreshAppearance() {
        int currentThemeRes = AppSettingsManager.resolveThemeRes(this);
        int currentNightMode = AppSettingsManager.getThemeMode(this);
        String currentLanguage = AppSettingsManager.getLanguage(this);
        return appliedThemeRes != currentThemeRes
                || appliedNightMode != currentNightMode
                || !currentLanguage.equals(appliedLanguage);
    }

    private void captureAppliedAppearance() {
        appliedThemeRes = AppSettingsManager.resolveThemeRes(this);
        appliedNightMode = AppSettingsManager.getThemeMode(this);
        appliedLanguage = AppSettingsManager.getLanguage(this);
    }

    private void showLanguagePicker() {
        final String[] labels = new String[]{
                getString(R.string.language_system),
                getString(R.string.language_zh),
                getString(R.string.language_en)
        };
        final String[] values = new String[]{
                AppSettingsManager.LANGUAGE_SYSTEM,
                AppSettingsManager.LANGUAGE_ZH,
                AppSettingsManager.LANGUAGE_EN
        };

        int checkedIndex = 0;
        String current = AppSettingsManager.getLanguage(this);
        if (AppSettingsManager.LANGUAGE_ZH.equals(current)) {
            checkedIndex = 1;
        } else if (AppSettingsManager.LANGUAGE_EN.equals(current)) {
            checkedIndex = 2;
        }

        new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.language)
                .setSingleChoiceItems(labels, checkedIndex, (dialog, which) -> {
                    String selected = values[which];
                    if (!selected.equals(AppSettingsManager.getLanguage(this))) {
                        AppSettingsManager.setLanguage(this, selected);
                        AppSettingsManager.applyLocale(this);
                        recreate();
                    }
                    dialog.dismiss();
                })
                .show();
    }

    private void startPreviewAnimation() {
        if (previewRunning) {
            return;
        }
        previewRunning = true;
        resetPreviewScene();
        previewHandler.postDelayed(previewTickRunnable, PREVIEW_TICK_MS);
    }

    private void stopPreviewAnimation() {
        previewRunning = false;
        previewHandler.removeCallbacks(previewTickRunnable);
    }

    private void resetPreviewScene() {
        for (int row = 0; row < GameConstants.BOARD_ROWS; row++) {
            Arrays.fill(previewBoard[row], 0);
        }
        previewBag = new Tetromino.BagGenerator();
        if (!spawnPreviewPiece()) {
            for (int row = 0; row < GameConstants.BOARD_ROWS; row++) {
                Arrays.fill(previewBoard[row], 0);
            }
            spawnPreviewPiece();
        }
        renderPreview();
    }

    private void onPreviewTick() {
        if (!previewRunning) {
            return;
        }
        if (previewPiece == null && !spawnPreviewPiece()) {
            resetPreviewScene();
            previewHandler.postDelayed(previewTickRunnable, PREVIEW_TICK_MS);
            return;
        }

        performPreviewAction();
        if (!movePreviewPiece(1, 0)) {
            lockPreviewPiece();
            clearPreviewLines();
            if (!spawnPreviewPiece()) {
                resetPreviewScene();
            }
        }

        renderPreview();
        previewHandler.postDelayed(previewTickRunnable, PREVIEW_TICK_MS);
    }

    private void performPreviewAction() {
        if (previewPiece == null) {
            return;
        }
        int action = previewRandom.nextInt(12);
        if (action == 0) {
            rotatePreviewPiece();
        } else if (action == 1 || action == 2) {
            movePreviewPiece(0, -1);
        } else if (action == 3 || action == 4) {
            movePreviewPiece(0, 1);
        }

        if (previewPiece.col <= 1) {
            movePreviewPiece(0, 1);
        } else if (previewPiece.col >= GameConstants.BOARD_COLS - 3) {
            movePreviewPiece(0, -1);
        }
    }

    private boolean spawnPreviewPiece() {
        if (previewBag == null) {
            previewBag = new Tetromino.BagGenerator();
        }
        previewPiece = new Tetromino(previewBag.nextClassicType());
        previewPiece.row = 0;
        previewPiece.col = 3;
        return canPlacePreview(previewPiece);
    }

    private boolean movePreviewPiece(int deltaRow, int deltaCol) {
        if (previewPiece == null) {
            return false;
        }
        Tetromino candidate = previewPiece.copy();
        candidate.row += deltaRow;
        candidate.col += deltaCol;
        if (!canPlacePreview(candidate)) {
            return false;
        }
        previewPiece = candidate;
        return true;
    }

    private void rotatePreviewPiece() {
        if (previewPiece == null) {
            return;
        }
        int targetRotation = previewPiece.nextRotation();
        int[] kicks = new int[]{0, -1, 1, -2, 2};
        for (int kick : kicks) {
            Tetromino candidate = previewPiece.copy();
            candidate.rotation = targetRotation;
            candidate.col += kick;
            if (canPlacePreview(candidate)) {
                previewPiece = candidate;
                return;
            }
        }
    }

    private boolean canPlacePreview(Tetromino piece) {
        for (int[] offset : piece.cells()) {
            int row = piece.row + offset[0];
            int col = piece.col + offset[1];
            if (row < 0 || row >= GameConstants.BOARD_ROWS || col < 0 || col >= GameConstants.BOARD_COLS) {
                return false;
            }
            if (previewBoard[row][col] != 0) {
                return false;
            }
        }
        return true;
    }

    private void lockPreviewPiece() {
        if (previewPiece == null) {
            return;
        }
        int colorCode = Tetromino.colorCodeForType(previewPiece.type);
        for (int[] offset : previewPiece.cells()) {
            int row = previewPiece.row + offset[0];
            int col = previewPiece.col + offset[1];
            if (row >= 0 && row < GameConstants.BOARD_ROWS && col >= 0 && col < GameConstants.BOARD_COLS) {
                previewBoard[row][col] = colorCode;
            }
        }
        previewPiece = null;
    }

    private void clearPreviewLines() {
        for (int row = GameConstants.BOARD_ROWS - 1; row >= 0; row--) {
            boolean full = true;
            for (int col = 0; col < GameConstants.BOARD_COLS; col++) {
                if (previewBoard[row][col] == 0) {
                    full = false;
                    break;
                }
            }
            if (!full) {
                continue;
            }
            for (int r = row; r > 0; r--) {
                System.arraycopy(previewBoard[r - 1], 0, previewBoard[r], 0, GameConstants.BOARD_COLS);
            }
            Arrays.fill(previewBoard[0], 0);
            row++;
        }
    }

    private void renderPreview() {
        if (previewBoardView != null) {
            previewBoardView.updateState(previewBoard, previewPiece);
        }
    }
}

