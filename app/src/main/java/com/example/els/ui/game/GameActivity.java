package com.example.els.ui.game;

import android.content.Intent;
import android.graphics.RenderEffect;
import android.graphics.Shader;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.os.VibratorManager;
import android.text.TextUtils;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import com.example.els.R;
import com.example.els.core.AppSettingsManager;
import com.example.els.core.UiUtils;
import com.example.els.game.GameConstants;
import com.example.els.game.Tetromino;
import com.example.els.ui.base.BaseActivity;
import com.example.els.ui.shop.ShopActivity;
import com.example.els.ui.welcome.WelcomeActivity;
import com.example.els.ui.widget.NextPieceView;
import com.example.els.ui.widget.TetrisBoardView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public class GameActivity extends BaseActivity {
    private final Handler gameHandler = new Handler(Looper.getMainLooper());
    private final Runnable tickRunnable = this::onGameTick;
    private static final long HORIZONTAL_MOVE_INITIAL_DELAY_MS = 180L;
    private static final long HORIZONTAL_MOVE_REPEAT_DELAY_MS = 70L;
    private static final long COIN_INCOME_HINT_DURATION_MS = 1200L;
    private static final long SOFT_DROP_DOUBLE_TAP_WINDOW_MS = 320L;
    private static final long SOFT_DROP_TAP_MAX_DURATION_MS = 180L;
    private static final String STATE_BOARD = "state_board";
    private static final String STATE_HAS_CURRENT_PIECE = "state_has_current_piece";
    private static final String STATE_CURRENT_TYPE = "state_current_type";
    private static final String STATE_CURRENT_ROTATION = "state_current_rotation";
    private static final String STATE_CURRENT_ROW = "state_current_row";
    private static final String STATE_CURRENT_COL = "state_current_col";
    private static final String STATE_NEXT_PIECE_TYPE = "state_next_piece_type";
    private static final String STATE_SCORE = "state_score";
    private static final String STATE_LINES = "state_lines";
    private static final String STATE_LEVEL = "state_level";
    private static final String STATE_HIGH_SCORE = "state_high_score";
    private static final String STATE_MATCH_COINS_EARNED = "state_match_coins_earned";
    private static final String STATE_GAME_OVER = "state_game_over";
    private static final String STATE_PAUSED = "state_paused";
    private static final String STATE_FREEZE_REMAIN_MS = "state_freeze_remain_ms";
    private static final String STATE_COIN_BOOST_REMAIN_MS = "state_coin_boost_remain_ms";
    private static final String STATE_ELAPSED_MS = "state_elapsed_ms";
    private static final String STATE_QUICK_SLOTS = "state_quick_slots";
    private static final String STATE_SHOW_ROTATION_PAUSE_DIALOG = "state_show_rotation_pause_dialog";

    private final int[][] board = new int[GameConstants.BOARD_ROWS][GameConstants.BOARD_COLS];

    private TetrisBoardView boardView;
    private NextPieceView nextPieceView;
    private TextView tvCoins;
    private TextView tvCoinIncomeHint;
    private TextView tvScore;
    private TextView tvHighScore;
    private TextView tvLevel;
    private TextView tvLines;
    private TextView tvMode;
    private TextView tvFreezeHint;
    private View quickSlotsPanel;

    private Button btnQuick1;
    private Button btnQuick2;
    private Button btnQuick3;
    private Button btnQuickCount1;
    private Button btnQuickCount2;
    private Button btnOpenShop;
    private Button btnOpenDrawerShop;

    private DrawerLayout drawerLayout;
    private TextView tvDrawerCoins;

    private View gameRoot;

    private Tetromino.BagGenerator bagGenerator;
    private Tetromino currentPiece;
    private int nextPieceType;

    private String mode;
    private boolean itemModeEnabled;
    private boolean customSpeedEnabled;
    private int customSpeedLevel = GameConstants.CUSTOM_SPEED_DEFAULT;

    private int score;
    private int lines;
    private int level;
    private int highScore;
    private int matchCoinsEarned;

    private boolean paused;
    private boolean gameOver;
    private boolean softDrop;
    private boolean exitConfirmShowing;
    private boolean rotationPauseShowing;

    private long startTimeMs;
    private long freezeUntilMs;
    private long coinBoostUntilMs;

    private String[] quickSlots = new String[GameConstants.MAX_QUICK_SLOTS];
    private int horizontalMoveDirection;
    private long softDropPressDownMs;
    private long lastSoftDropTapUpMs;
    private final Runnable hideCoinIncomeHintRunnable = () -> {
        if (tvCoinIncomeHint != null) {
            tvCoinIncomeHint.setVisibility(View.INVISIBLE);
        }
    };
    private final Runnable horizontalMoveRunnable = new Runnable() {
        @Override
        public void run() {
            if (horizontalMoveDirection == 0 || paused || gameOver) {
                return;
            }
            if (movePiece(0, horizontalMoveDirection)) {
                renderGame();
            }
            gameHandler.postDelayed(this, HORIZONTAL_MOVE_REPEAT_DELAY_MS);
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        mode = getIntent().getStringExtra(GameConstants.EXTRA_MODE);
        if (!GameConstants.MODE_CHALLENGE.equals(mode)) {
            mode = GameConstants.MODE_ENDLESS;
        }
        itemModeEnabled = getIntent().getBooleanExtra(
                GameConstants.EXTRA_ITEM_MODE_ENABLED,
                AppSettingsManager.isItemModeEnabled(this)
        );
        customSpeedEnabled = getIntent().getBooleanExtra(
                GameConstants.EXTRA_CUSTOM_SPEED_ENABLED,
                false
        );
        customSpeedLevel = GameConstants.normalizeCustomSpeedLevel(
                getIntent().getIntExtra(
                        GameConstants.EXTRA_CUSTOM_SPEED_LEVEL,
                        GameConstants.CUSTOM_SPEED_DEFAULT
                )
        );
        highScore = AppSettingsManager.getHighScore(this);

        bindViews();
        bindActions();
        boolean shouldShowRotationPauseDialog = false;
        if (savedInstanceState != null && restoreGameState(savedInstanceState)) {
            shouldShowRotationPauseDialog = savedInstanceState.getBoolean(
                    STATE_SHOW_ROTATION_PAUSE_DIALOG,
                    false
            );
            renderGame();
            if (shouldShowRotationPauseDialog && !gameOver) {
                showRotationPauseDialog();
            } else if (!gameOver && !exitConfirmShowing && !rotationPauseShowing) {
                scheduleNextTick();
            }
        } else {
            startNewGame();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        pauseGame();
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        persistGameState(outState);
    }

    @Override
    protected void onResume() {
        super.onResume();
        quickSlots = AppSettingsManager.getQuickSlots(this);
        updateQuickSlotPanel();
        updateInfoViews();
        if (!gameOver && !exitConfirmShowing && !rotationPauseShowing) {
            resumeGame();
        }
    }

    @Override
    public void onBackPressed() {
        if (!gameOver) {
            showExitConfirmDialog();
            return;
        }
        super.onBackPressed();
    }

    private void persistGameState(@NonNull Bundle outState) {
        outState.putIntArray(STATE_BOARD, flattenBoard());
        boolean hasCurrentPiece = currentPiece != null;
        outState.putBoolean(STATE_HAS_CURRENT_PIECE, hasCurrentPiece);
        if (hasCurrentPiece) {
            outState.putInt(STATE_CURRENT_TYPE, currentPiece.type);
            outState.putInt(STATE_CURRENT_ROTATION, currentPiece.rotation);
            outState.putInt(STATE_CURRENT_ROW, currentPiece.row);
            outState.putInt(STATE_CURRENT_COL, currentPiece.col);
        }
        outState.putInt(STATE_NEXT_PIECE_TYPE, nextPieceType);
        outState.putInt(STATE_SCORE, score);
        outState.putInt(STATE_LINES, lines);
        outState.putInt(STATE_LEVEL, level);
        outState.putInt(STATE_HIGH_SCORE, highScore);
        outState.putInt(STATE_MATCH_COINS_EARNED, matchCoinsEarned);
        outState.putBoolean(STATE_GAME_OVER, gameOver);
        outState.putBoolean(STATE_PAUSED, paused);
        outState.putStringArray(STATE_QUICK_SLOTS, quickSlots);
        outState.putBoolean(STATE_SHOW_ROTATION_PAUSE_DIALOG, isChangingConfigurations() && !gameOver);

        long now = SystemClock.elapsedRealtime();
        outState.putLong(STATE_FREEZE_REMAIN_MS, Math.max(0L, freezeUntilMs - now));
        outState.putLong(STATE_COIN_BOOST_REMAIN_MS, Math.max(0L, coinBoostUntilMs - now));
        outState.putLong(STATE_ELAPSED_MS, Math.max(0L, now - startTimeMs));
    }

    private boolean restoreGameState(@NonNull Bundle savedInstanceState) {
        int[] flattenedBoard = savedInstanceState.getIntArray(STATE_BOARD);
        if (flattenedBoard == null || flattenedBoard.length != GameConstants.BOARD_ROWS * GameConstants.BOARD_COLS) {
            return false;
        }

        restoreBoard(flattenedBoard);
        bagGenerator = new Tetromino.BagGenerator();
        nextPieceType = savedInstanceState.containsKey(STATE_NEXT_PIECE_TYPE)
                ? savedInstanceState.getInt(STATE_NEXT_PIECE_TYPE, Tetromino.TYPE_I)
                : bagGenerator.nextClassicType();

        score = savedInstanceState.getInt(STATE_SCORE, 0);
        lines = savedInstanceState.getInt(STATE_LINES, 0);
        level = Math.max(1, savedInstanceState.getInt(STATE_LEVEL, 1));
        highScore = Math.max(AppSettingsManager.getHighScore(this), savedInstanceState.getInt(STATE_HIGH_SCORE, 0));
        matchCoinsEarned = savedInstanceState.getInt(STATE_MATCH_COINS_EARNED, 0);
        gameOver = savedInstanceState.getBoolean(STATE_GAME_OVER, false);
        paused = savedInstanceState.getBoolean(STATE_PAUSED, false);
        exitConfirmShowing = false;
        rotationPauseShowing = false;
        softDrop = false;
        stopHorizontalMoveRepeat();
        softDropPressDownMs = 0L;
        lastSoftDropTapUpMs = 0L;
        gameHandler.removeCallbacks(hideCoinIncomeHintRunnable);
        if (tvCoinIncomeHint != null) {
            tvCoinIncomeHint.setText("");
            tvCoinIncomeHint.setVisibility(itemModeEnabled ? View.INVISIBLE : View.GONE);
        }

        long now = SystemClock.elapsedRealtime();
        freezeUntilMs = now + savedInstanceState.getLong(STATE_FREEZE_REMAIN_MS, 0L);
        coinBoostUntilMs = now + savedInstanceState.getLong(STATE_COIN_BOOST_REMAIN_MS, 0L);
        long elapsedMs = Math.max(0L, savedInstanceState.getLong(STATE_ELAPSED_MS, 0L));
        startTimeMs = now - elapsedMs;

        String[] restoredQuickSlots = savedInstanceState.getStringArray(STATE_QUICK_SLOTS);
        if (restoredQuickSlots != null) {
            quickSlots = new String[GameConstants.MAX_QUICK_SLOTS];
            Arrays.fill(quickSlots, "");
            for (int i = 0; i < GameConstants.MAX_QUICK_SLOTS && i < restoredQuickSlots.length; i++) {
                quickSlots[i] = restoredQuickSlots[i] == null ? "" : restoredQuickSlots[i];
            }
        } else {
            quickSlots = AppSettingsManager.getQuickSlots(this);
        }

        if (savedInstanceState.getBoolean(STATE_HAS_CURRENT_PIECE, false)) {
            Tetromino restoredPiece = new Tetromino(savedInstanceState.getInt(STATE_CURRENT_TYPE, Tetromino.TYPE_I));
            restoredPiece.rotation = savedInstanceState.getInt(STATE_CURRENT_ROTATION, 0);
            restoredPiece.row = savedInstanceState.getInt(STATE_CURRENT_ROW, 0);
            restoredPiece.col = savedInstanceState.getInt(STATE_CURRENT_COL, 3);
            if (!canPlace(restoredPiece)) {
                return false;
            }
            currentPiece = restoredPiece;
        } else if (!gameOver) {
            if (!spawnNextPiece()) {
                gameOver = true;
            }
        }

        return true;
    }

    private int[] flattenBoard() {
        int[] flattened = new int[GameConstants.BOARD_ROWS * GameConstants.BOARD_COLS];
        int index = 0;
        for (int r = 0; r < GameConstants.BOARD_ROWS; r++) {
            for (int c = 0; c < GameConstants.BOARD_COLS; c++) {
                flattened[index++] = board[r][c];
            }
        }
        return flattened;
    }

    private void restoreBoard(@NonNull int[] flattened) {
        int index = 0;
        for (int r = 0; r < GameConstants.BOARD_ROWS; r++) {
            for (int c = 0; c < GameConstants.BOARD_COLS; c++) {
                board[r][c] = flattened[index++];
            }
        }
    }
    private void bindViews() {
        gameRoot = findViewById(R.id.gameRoot);
        boardView = findViewById(R.id.boardView);
        boardView.setLandingGuideEnabled(true);
        nextPieceView = findViewById(R.id.nextPieceView);

        tvCoins = findViewById(R.id.tvGameCoins);
        tvCoinIncomeHint = findViewById(R.id.tvCoinIncomeHint);
        tvScore = findViewById(R.id.tvGameScore);
        tvHighScore = findViewById(R.id.tvGameHighScore);
        tvLevel = findViewById(R.id.tvGameLevel);
        tvLines = findViewById(R.id.tvGameLines);
        tvMode = findViewById(R.id.tvGameMode);
        tvFreezeHint = findViewById(R.id.tvFreezeHint);

        quickSlotsPanel = findViewById(R.id.quickSlotsPanel);
        btnQuickCount1 = findViewById(R.id.btnQuickCount1);
        btnQuickCount2 = findViewById(R.id.btnQuickCount2);
        btnQuick1 = findViewById(R.id.btnQuickSlot1);
        btnQuick2 = findViewById(R.id.btnQuickSlot2);
        btnQuick3 = findViewById(R.id.btnQuickSlot3);
        btnOpenShop = findViewById(R.id.btnGameShop);
        btnOpenDrawerShop = findViewById(R.id.btnOpenDrawerShop);

        drawerLayout = findViewById(R.id.drawerLayoutGame);
        tvDrawerCoins = findViewById(R.id.tvDrawerCoins);
    }

    private void bindActions() {
        View leftButton = findViewById(R.id.btnMoveLeft);
        View rightButton = findViewById(R.id.btnMoveRight);

        leftButton.setOnClickListener(v -> moveHorizontallyOnce(-1));
        rightButton.setOnClickListener(v -> moveHorizontallyOnce(1));

        leftButton.setOnTouchListener((v, event) -> handleHorizontalMoveTouch(event, -1));
        rightButton.setOnTouchListener((v, event) -> handleHorizontalMoveTouch(event, 1));
        findViewById(R.id.btnRotate).setOnClickListener(v -> {
            if (!paused && !gameOver) {
                rotatePiece();
            }
        });
        View downButton = findViewById(R.id.btnSoftDrop);
        downButton.setOnTouchListener((v, event) -> {
            if (gameOver) {
                return false;
            }
            int action = event.getActionMasked();
            if (action == MotionEvent.ACTION_DOWN) {
                softDropPressDownMs = SystemClock.elapsedRealtime();
                softDrop = true;
                scheduleNextTick();
                return true;
            }
            if (action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_CANCEL) {
                softDrop = false;
                scheduleNextTick();

                if (action == MotionEvent.ACTION_UP && !paused && !gameOver) {
                    long now = SystemClock.elapsedRealtime();
                    long pressDuration = now - softDropPressDownMs;
                    if (pressDuration <= SOFT_DROP_TAP_MAX_DURATION_MS) {
                        if (lastSoftDropTapUpMs > 0
                                && now - lastSoftDropTapUpMs <= SOFT_DROP_DOUBLE_TAP_WINDOW_MS) {
                            lastSoftDropTapUpMs = 0L;
                            hardDropCurrentPiece();
                            return true;
                        }
                        lastSoftDropTapUpMs = now;
                    } else {
                        lastSoftDropTapUpMs = 0L;
                    }
                } else {
                    lastSoftDropTapUpMs = 0L;
                }
                return true;
            }
            return false;
        });

        findViewById(R.id.btnExitGame).setOnClickListener(v -> showExitConfirmDialog());

        btnOpenShop.setOnClickListener(v -> {
            if (!itemModeEnabled) {
                return;
            }
            pauseGame();
            startActivity(new Intent(this, ShopActivity.class));
        });

        btnQuick1.setOnClickListener(v -> useQuickSlot(0));
        btnQuick2.setOnClickListener(v -> useQuickSlot(1));
        btnQuick3.setOnClickListener(v -> showOwnedItemsDropdown());

        if (btnOpenDrawerShop != null) {
            btnOpenDrawerShop.setOnClickListener(v -> {
                if (drawerLayout != null && itemModeEnabled) {
                    drawerLayout.openDrawer(GravityCompat.START);
                }
            });
        }

        if (drawerLayout != null) {
            View btnDrawerLine = findViewById(R.id.btnDrawerBuyLineClear);
            View btnDrawerBomb = findViewById(R.id.btnDrawerBuyBomb);
            View btnDrawerFreeze = findViewById(R.id.btnDrawerBuyFreeze);
            View btnDrawerFries = findViewById(R.id.btnDrawerBuyFries);
            View btnDrawerIncomeBoost = findViewById(R.id.btnDrawerBuyIncomeBoost);
            View btnDrawerDestroyCurrent = findViewById(R.id.btnDrawerBuyDestroyCurrent);

            if (btnDrawerLine != null) {
                btnDrawerLine.setOnClickListener(v -> buyItemInsideGame(GameConstants.ITEM_LINE_CLEAR));
            }
            if (btnDrawerBomb != null) {
                btnDrawerBomb.setOnClickListener(v -> buyItemInsideGame(GameConstants.ITEM_BOMB_NEXT));
            }
            if (btnDrawerFreeze != null) {
                btnDrawerFreeze.setOnClickListener(v -> buyItemInsideGame(GameConstants.ITEM_FREEZE));
            }
            if (btnDrawerFries != null) {
                btnDrawerFries.setOnClickListener(v -> buyItemInsideGame(GameConstants.ITEM_FRIES));
            }
            if (btnDrawerIncomeBoost != null) {
                btnDrawerIncomeBoost.setOnClickListener(v -> buyItemInsideGame(GameConstants.ITEM_INCOME_BOOST));
            }
            if (btnDrawerDestroyCurrent != null) {
                btnDrawerDestroyCurrent.setOnClickListener(v -> buyItemInsideGame(GameConstants.ITEM_DESTROY_CURRENT));
            }
        }
    }

    private void moveHorizontallyOnce(int direction) {
        if (!paused && !gameOver && movePiece(0, direction)) {
            renderGame();
        }
    }

    private boolean handleHorizontalMoveTouch(MotionEvent event, int direction) {
        if (gameOver) {
            return false;
        }
        int action = event.getActionMasked();
        if (action == MotionEvent.ACTION_DOWN) {
            startHorizontalMoveRepeat(direction);
            return true;
        }
        if (action == MotionEvent.ACTION_MOVE) {
            return true;
        }
        if (action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_CANCEL) {
            stopHorizontalMoveRepeat();
            return true;
        }
        return false;
    }

    private void startHorizontalMoveRepeat(int direction) {
        if (paused || gameOver) {
            return;
        }
        horizontalMoveDirection = direction;
        gameHandler.removeCallbacks(horizontalMoveRunnable);
        moveHorizontallyOnce(direction);
        gameHandler.postDelayed(horizontalMoveRunnable, HORIZONTAL_MOVE_INITIAL_DELAY_MS);
    }

    private void stopHorizontalMoveRepeat() {
        horizontalMoveDirection = 0;
        gameHandler.removeCallbacks(horizontalMoveRunnable);
    }
    private void startNewGame() {
        for (int r = 0; r < GameConstants.BOARD_ROWS; r++) {
            Arrays.fill(board[r], 0);
        }

        bagGenerator = new Tetromino.BagGenerator();
        nextPieceType = bagGenerator.nextClassicType();
        score = 0;
        lines = 0;
        level = 1;
        matchCoinsEarned = 0;
        gameHandler.removeCallbacks(hideCoinIncomeHintRunnable);
        if (tvCoinIncomeHint != null) {
            tvCoinIncomeHint.setText("");
            tvCoinIncomeHint.setVisibility(itemModeEnabled ? View.INVISIBLE : View.GONE);
        }
        freezeUntilMs = 0L;
        coinBoostUntilMs = 0L;
        softDrop = false;
        stopHorizontalMoveRepeat();
        softDropPressDownMs = 0L;
        lastSoftDropTapUpMs = 0L;
        gameOver = false;
        paused = false;
        exitConfirmShowing = false;
        rotationPauseShowing = false;
        startTimeMs = SystemClock.elapsedRealtime();

        quickSlots = AppSettingsManager.getQuickSlots(this);
        updateQuickSlotPanel();
        if (!spawnNextPiece()) {
            showSettlementDialog(false);
            return;
        }
        renderGame();
        scheduleNextTick();
    }

    private void onGameTick() {
        if (paused || gameOver) {
            return;
        }
        if (SystemClock.elapsedRealtime() < freezeUntilMs) {
            updateInfoViews();
            scheduleNextTick();
            return;
        }

        if (!movePiece(1, 0)) {
            lockCurrentPiece();
        } else {
            renderGame();
        }
        scheduleNextTick();
    }

    private void hardDropCurrentPiece() {
        if (paused || gameOver || currentPiece == null) {
            return;
        }
        while (movePiece(1, 0)) {
            // keep moving down until the piece lands.
        }
        lockCurrentPiece();
        scheduleNextTick();
    }

    private void scheduleNextTick() {
        gameHandler.removeCallbacks(tickRunnable);
        if (paused || gameOver) {
            return;
        }
        gameHandler.postDelayed(tickRunnable, currentTickMillis());
    }

    private long currentTickMillis() {
        if (SystemClock.elapsedRealtime() < freezeUntilMs) {
            return 80L;
        }
        if (softDrop) {
            return 70L;
        }
        if (GameConstants.MODE_CHALLENGE.equals(mode)) {
            long initial = customSpeedEnabled
                    ? resolveChallengeInitialSpeedMillis()
                    : 760L;
            return Math.max(240L, initial - (long) (level - 1) * 45L);
        }
        if (!customSpeedEnabled) {
            return 700L;
        }
        return resolveEndlessSpeedMillis();
    }

    private long resolveEndlessSpeedMillis() {
        switch (GameConstants.normalizeCustomSpeedLevel(customSpeedLevel)) {
            case GameConstants.CUSTOM_SPEED_FAST:
                return 540L;
            case GameConstants.CUSTOM_SPEED_VERY_FAST:
                return 400L;
            case GameConstants.CUSTOM_SPEED_DEFAULT:
            default:
                return 700L;
        }
    }

    private long resolveChallengeInitialSpeedMillis() {
        switch (GameConstants.normalizeCustomSpeedLevel(customSpeedLevel)) {
            case GameConstants.CUSTOM_SPEED_FAST:
                return 620L;
            case GameConstants.CUSTOM_SPEED_VERY_FAST:
                return 500L;
            case GameConstants.CUSTOM_SPEED_DEFAULT:
            default:
                return 760L;
        }
    }

    private void pauseGame() {
        paused = true;
        softDrop = false;
        stopHorizontalMoveRepeat();
        gameHandler.removeCallbacks(tickRunnable);
        softDropPressDownMs = 0L;
        lastSoftDropTapUpMs = 0L;
    }

    private void resumeGame() {
        if (gameOver) {
            return;
        }
        paused = false;
        scheduleNextTick();
    }

    private boolean spawnNextPiece() {
        currentPiece = new Tetromino(nextPieceType);
        currentPiece.row = 0;
        currentPiece.col = nextPieceType == Tetromino.TYPE_BOMB ? GameConstants.BOARD_COLS / 2 : 3;

        if (!canPlace(currentPiece)) {
            return false;
        }
        nextPieceType = bagGenerator.nextClassicType();
        nextPieceView.setNextType(nextPieceType);
        return true;
    }

    private boolean canPlace(Tetromino piece) {
        for (int[] offset : piece.cells()) {
            int row = piece.row + offset[0];
            int col = piece.col + offset[1];
            if (row < 0 || row >= GameConstants.BOARD_ROWS || col < 0 || col >= GameConstants.BOARD_COLS) {
                return false;
            }
            if (board[row][col] != 0) {
                return false;
            }
        }
        return true;
    }

    private boolean movePiece(int deltaRow, int deltaCol) {
        Tetromino candidate = currentPiece.copy();
        candidate.row += deltaRow;
        candidate.col += deltaCol;
        if (!canPlace(candidate)) {
            return false;
        }
        currentPiece = candidate;
        return true;
    }

    private void rotatePiece() {
        int targetRotation = currentPiece.nextRotation();
        int[] kicks = new int[]{0, -1, 1, -2, 2};
        for (int kick : kicks) {
            Tetromino candidate = currentPiece.copy();
            candidate.rotation = targetRotation;
            candidate.col += kick;
            if (canPlace(candidate)) {
                currentPiece = candidate;
                renderGame();
                return;
            }
        }
    }

    private void lockCurrentPiece() {
        if (currentPiece.type == Tetromino.TYPE_BOMB) {
            int centerRow = currentPiece.row + currentPiece.cells()[0][0];
            int centerCol = currentPiece.col + currentPiece.cells()[0][1];
            explodeBomb(centerRow, centerCol);
        } else {
            int colorCode = Tetromino.colorCodeForType(currentPiece.type);
            for (int[] offset : currentPiece.cells()) {
                int row = currentPiece.row + offset[0];
                int col = currentPiece.col + offset[1];
                if (row >= 0 && row < GameConstants.BOARD_ROWS && col >= 0 && col < GameConstants.BOARD_COLS) {
                    board[row][col] = colorCode;
                }
            }
        }

        int cleared = clearFullLines();
        if (cleared > 0) {
            addScoreForLineClear(cleared);
        }

        if (!spawnNextPiece()) {
            showSettlementDialog(false);
            return;
        }
        renderGame();
    }

    private int clearFullLines() {
        int cleared = 0;
        for (int row = GameConstants.BOARD_ROWS - 1; row >= 0; row--) {
            boolean full = true;
            for (int col = 0; col < GameConstants.BOARD_COLS; col++) {
                if (board[row][col] == 0) {
                    full = false;
                    break;
                }
            }
            if (full) {
                clearRowAndCollapse(row);
                cleared++;
                row++;
            }
        }
        return cleared;
    }

    private void clearRowAndCollapse(int row) {
        for (int r = row; r > 0; r--) {
            System.arraycopy(board[r - 1], 0, board[r], 0, GameConstants.BOARD_COLS);
        }
        Arrays.fill(board[0], 0);
    }

    private void addScoreForLineClear(int cleared) {
        int base;
        switch (cleared) {
            case 1:
                base = 100;
                break;
            case 2:
                base = 300;
                break;
            case 3:
                base = 500;
                break;
            case 4:
                base = 800;
                break;
            default:
                base = cleared * 250;
                break;
        }
        score += base * level;
        lines += cleared;
        level = Math.max(1, lines / 10 + 1);

        if (itemModeEnabled) {
            int baseCoins = cleared * 10 + (cleared == 4 ? 20 : 0);
            awardCoinsForClearAction(baseCoins);
        }
        vibrateOnLineClearIfEnabled();

        if (score > highScore) {
            highScore = score;
            AppSettingsManager.setHighScore(this, highScore);
        }
    }

    private void explodeBomb(int centerRow, int centerCol) {
        int removed = 0;
        for (int r = centerRow - 1; r <= centerRow + 1; r++) {
            for (int c = centerCol - 1; c <= centerCol + 1; c++) {
                if (r < 0 || r >= GameConstants.BOARD_ROWS || c < 0 || c >= GameConstants.BOARD_COLS) {
                    continue;
                }
                if (board[r][c] != 0) {
                    removed++;
                }
                board[r][c] = 0;
            }
        }
        if (removed > 0) {
            score += removed * 20;
            if (score > highScore) {
                highScore = score;
                AppSettingsManager.setHighScore(this, highScore);
            }
        }
        vibrateLight();
    }

    private void vibrateLight() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                VibratorManager manager = getSystemService(VibratorManager.class);
                if (manager != null) {
                    manager.getDefaultVibrator().vibrate(
                            VibrationEffect.createOneShot(70L, VibrationEffect.DEFAULT_AMPLITUDE)
                    );
                }
            } else {
                Vibrator vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
                if (vibrator != null) {
                    vibrator.vibrate(VibrationEffect.createOneShot(70L, VibrationEffect.DEFAULT_AMPLITUDE));
                }
            }
        } catch (Throwable ignored) {
            // Ignore vibration failures for devices without vibrator support.
        }
    }

    private void useQuickSlot(int index) {
        if (!itemModeEnabled || index < 0 || index >= quickSlots.length) {
            return;
        }
        String itemId = quickSlots[index];
        if (TextUtils.isEmpty(itemId)) {
            Toast.makeText(this, R.string.quick_slot_empty, Toast.LENGTH_SHORT).show();
            return;
        }
        useItem(itemId);
    }

    private void showOwnedItemsDropdown() {
        if (!itemModeEnabled || gameOver) {
            return;
        }

        List<String> availableItems = new ArrayList<>();
        PopupMenu popupMenu = new PopupMenu(this, btnQuick3);

        int menuId = 0;
        for (String itemId : GameConstants.ITEM_IDS) {
            int count = AppSettingsManager.getItemCount(this, itemId);
            if (count <= 0) {
                continue;
            }
            String title = getString(GameConstants.getItemNameRes(itemId)) + "(" + count + ")";
            popupMenu.getMenu().add(0, menuId, menuId, title);
            availableItems.add(itemId);
            menuId++;
        }

        if (availableItems.isEmpty()) {
            Toast.makeText(this, R.string.item_not_available, Toast.LENGTH_SHORT).show();
            return;
        }

        popupMenu.setOnMenuItemClickListener(menuItem -> {
            int index = menuItem.getItemId();
            if (index >= 0 && index < availableItems.size()) {
                useItem(availableItems.get(index));
                return true;
            }
            return false;
        });
        popupMenu.show();
    }

    private void useItem(String itemId) {
        if (TextUtils.isEmpty(itemId)) {
            Toast.makeText(this, R.string.quick_slot_empty, Toast.LENGTH_SHORT).show();
            return;
        }
        if (!AppSettingsManager.consumeItem(this, itemId)) {
            Toast.makeText(this, R.string.item_not_available, Toast.LENGTH_SHORT).show();
            updateQuickSlotPanel();
            return;
        }

        switch (itemId) {
            case GameConstants.ITEM_LINE_CLEAR:
                applyLineClearItem();
                break;
            case GameConstants.ITEM_BOMB_NEXT:
                nextPieceType = Tetromino.TYPE_BOMB;
                nextPieceView.setNextType(nextPieceType);
                Toast.makeText(this, R.string.item_used_bomb_next, Toast.LENGTH_SHORT).show();
                break;
            case GameConstants.ITEM_FREEZE:
                freezeUntilMs = SystemClock.elapsedRealtime() + 5000L;
                Toast.makeText(this, R.string.item_used_freeze, Toast.LENGTH_SHORT).show();
                break;
            case GameConstants.ITEM_FRIES:
                nextPieceType = Tetromino.TYPE_I;
                nextPieceView.setNextType(nextPieceType);
                Toast.makeText(this, R.string.item_used_fries, Toast.LENGTH_SHORT).show();
                break;
            case GameConstants.ITEM_INCOME_BOOST:
                long now = SystemClock.elapsedRealtime();
                coinBoostUntilMs = Math.max(coinBoostUntilMs, now) + 60000L;
                Toast.makeText(this, R.string.item_used_income_boost, Toast.LENGTH_SHORT).show();
                break;
            case GameConstants.ITEM_DESTROY_CURRENT:
                if (!applyDestroyCurrentPieceItem()) {
                    updateQuickSlotPanel();
                    return;
                }
                break;
            default:
                break;
        }

        updateQuickSlotPanel();
        renderGame();
    }

    private void applyLineClearItem() {
        int targetRow = -1;
        int maxEmptyCount = -1;
        for (int row = 0; row < GameConstants.BOARD_ROWS; row++) {
            int empty = 0;
            for (int col = 0; col < GameConstants.BOARD_COLS; col++) {
                if (board[row][col] == 0) {
                    empty++;
                }
            }
            if (empty == GameConstants.BOARD_COLS) {
                continue;
            }
            if (empty > maxEmptyCount) {
                maxEmptyCount = empty;
                targetRow = row;
            }
        }

        if (targetRow < 0) {
            Toast.makeText(this, R.string.item_line_clear_no_effect, Toast.LENGTH_SHORT).show();
            return;
        }

        clearRowAndCollapse(targetRow);
        lines += 1;
        score += 80 * level;
        level = Math.max(1, lines / 10 + 1);

        if (itemModeEnabled) {
            awardCoinsForClearAction(10);
        }
        vibrateOnLineClearIfEnabled();
        if (score > highScore) {
            highScore = score;
            AppSettingsManager.setHighScore(this, highScore);
        }
        Toast.makeText(this, R.string.item_used_line_clear, Toast.LENGTH_SHORT).show();
    }


    private boolean applyDestroyCurrentPieceItem() {
        if (currentPiece == null || gameOver) {
            Toast.makeText(this, R.string.item_destroy_no_effect, Toast.LENGTH_SHORT).show();
            return true;
        }

        if (!spawnNextPiece()) {
            showSettlementDialog(false);
            return false;
        }

        Toast.makeText(this, R.string.item_used_destroy_current, Toast.LENGTH_SHORT).show();
        return true;
    }
    private void awardCoinsForClearAction(int baseCoins) {
        if (!itemModeEnabled || baseCoins <= 0) {
            return;
        }
        int multiplier = SystemClock.elapsedRealtime() < coinBoostUntilMs ? 3 : 1;
        int finalCoins = baseCoins * multiplier;
        AppSettingsManager.addCoins(this, finalCoins);
        matchCoinsEarned += finalCoins;
        showCoinIncomeHint(finalCoins);
    }

    private void showCoinIncomeHint(int coinsAdded) {
        if (!itemModeEnabled || coinsAdded <= 0 || tvCoinIncomeHint == null) {
            return;
        }
        tvCoinIncomeHint.setText(getString(R.string.coin_income_hint, coinsAdded));
        tvCoinIncomeHint.setVisibility(View.VISIBLE);
        gameHandler.removeCallbacks(hideCoinIncomeHintRunnable);
        gameHandler.postDelayed(hideCoinIncomeHintRunnable, COIN_INCOME_HINT_DURATION_MS);
    }

    private void vibrateOnLineClearIfEnabled() {
        if (!AppSettingsManager.isLineClearHapticsEnabled(this)) {
            return;
        }
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                VibratorManager manager = getSystemService(VibratorManager.class);
                if (manager != null) {
                    manager.getDefaultVibrator().vibrate(
                            VibrationEffect.createOneShot(45L, VibrationEffect.DEFAULT_AMPLITUDE)
                    );
                }
            } else {
                Vibrator vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
                if (vibrator != null) {
                    vibrator.vibrate(VibrationEffect.createOneShot(45L, VibrationEffect.DEFAULT_AMPLITUDE));
                }
            }
        } catch (Throwable ignored) {
            // Ignore vibration failures for devices without vibrator support.
        }
    }
    private void buyItemInsideGame(String itemId) {
        if (!itemModeEnabled) {
            return;
        }
        int price = GameConstants.getItemPrice(itemId);
        int coins = AppSettingsManager.getCoins(this);
        if (coins < price) {
            Toast.makeText(this, R.string.not_enough_coins, Toast.LENGTH_SHORT).show();
            return;
        }
        AppSettingsManager.setCoins(this, coins - price);
        AppSettingsManager.addItemCount(this, itemId, 1);
        updateQuickSlotPanel();
        updateInfoViews();
        Toast.makeText(
                this,
                getString(R.string.purchase_success, getString(GameConstants.getItemNameRes(itemId))),
                Toast.LENGTH_SHORT
        ).show();
    }
    private void updateQuickSlotPanel() {
        int visibility = itemModeEnabled ? View.VISIBLE : View.GONE;
        quickSlotsPanel.setVisibility(visibility);
        btnOpenShop.setVisibility(visibility);
        if (btnOpenDrawerShop != null) {
            btnOpenDrawerShop.setVisibility(itemModeEnabled && UiUtils.isTablet(this) ? View.VISIBLE : View.GONE);
        }
        if (tvDrawerCoins != null) {
            tvDrawerCoins.setVisibility(itemModeEnabled ? View.VISIBLE : View.GONE);
        }

        boolean quickSlotsChanged = false;
        for (int i = 0; i < 2; i++) {
            String itemId = quickSlots[i];
            Button useButton = i == 0 ? btnQuick1 : btnQuick2;
            Button countButton = i == 0 ? btnQuickCount1 : btnQuickCount2;
            if (TextUtils.isEmpty(itemId)) {
                useButton.setText(R.string.quick_slot_empty);
                useButton.setEnabled(false);
                if (countButton != null) {
                    countButton.setText("0");
                }
                continue;
            }
            int count = AppSettingsManager.getItemCount(this, itemId);
            if (count <= 0) {
                useButton.setText(R.string.quick_slot_empty);
                useButton.setEnabled(false);
                if (countButton != null) {
                    countButton.setText("0");
                }
                quickSlots[i] = "";
                quickSlotsChanged = true;
                continue;
            }
            if (countButton != null) {
                countButton.setText(String.valueOf(count));
            }
            useButton.setText(getString(GameConstants.getItemNameRes(itemId)));
            useButton.setEnabled(true);
        }
        if (quickSlotsChanged) {
            AppSettingsManager.setQuickSlots(this, quickSlots);
        }

        btnQuick3.setText(R.string.quick_slot_bag_items);
        btnQuick3.setEnabled(true);
    }
    private void updateInfoViews() {
        tvCoins.setVisibility(itemModeEnabled ? View.VISIBLE : View.GONE);
        if (tvCoinIncomeHint != null) {
            if (itemModeEnabled) {
                if (tvCoinIncomeHint.getVisibility() == View.GONE) {
                    tvCoinIncomeHint.setVisibility(View.INVISIBLE);
                }
            } else {
                tvCoinIncomeHint.setText("");
                tvCoinIncomeHint.setVisibility(View.GONE);
                gameHandler.removeCallbacks(hideCoinIncomeHintRunnable);
            }
        }
        if (itemModeEnabled) {
            tvCoins.setText(getString(R.string.coins_value, AppSettingsManager.getCoins(this)));
        }
        tvScore.setText(getString(R.string.score_value, score));
        tvHighScore.setText(getString(R.string.high_score_value, highScore));
        tvLevel.setText(getString(R.string.level_value, level));
        tvLines.setText(getString(R.string.lines_value, lines));
        tvMode.setText(getString(
                R.string.mode_value,
                getString(GameConstants.MODE_CHALLENGE.equals(mode) ? R.string.mode_challenge : R.string.mode_endless)
        ));

        long freezeRemainMs = Math.max(0L, freezeUntilMs - SystemClock.elapsedRealtime());
        long boostRemainMs = Math.max(0L, coinBoostUntilMs - SystemClock.elapsedRealtime());
        StringBuilder hintBuilder = new StringBuilder();
        if (freezeRemainMs > 0) {
            hintBuilder.append(getString(R.string.freeze_remaining_value, freezeRemainMs / 1000L + 1));
        }
        if (boostRemainMs > 0) {
            if (hintBuilder.length() > 0) {
                hintBuilder.append("\n");
            }
            hintBuilder.append(getString(R.string.income_boost_remaining_value, boostRemainMs / 1000L + 1));
        }
        if (hintBuilder.length() > 0) {
            tvFreezeHint.setVisibility(View.VISIBLE);
            tvFreezeHint.setText(hintBuilder.toString());
        } else {
            tvFreezeHint.setVisibility(View.GONE);
        }

        if (tvDrawerCoins != null) {
            tvDrawerCoins.setText(getString(R.string.coins_value, AppSettingsManager.getCoins(this)));
        }
    }

    private void renderGame() {
        boardView.updateState(board, currentPiece);
        nextPieceView.setNextType(nextPieceType);
        updateInfoViews();
    }

    private void showExitConfirmDialog() {
        if (gameOver || exitConfirmShowing) {
            return;
        }
        pauseGame();
        exitConfirmShowing = true;
        applyBlurEffect(true);

        new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.exit_game_title)
                .setMessage(R.string.exit_game_message)
                .setPositiveButton(R.string.confirm_exit, (dialog, which) -> {
                    applyBlurEffect(false);
                    exitConfirmShowing = false;
                    rotationPauseShowing = false;
                    if (lines <= 0) {
                        Intent intent = new Intent(this, WelcomeActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                        startActivity(intent);
                        finish();
                        return;
                    }
                    showSettlementDialog(true);
                })
                .setNegativeButton(R.string.cancel, (dialog, which) -> {
                    applyBlurEffect(false);
                    exitConfirmShowing = false;
                    rotationPauseShowing = false;
                    resumeGame();
                })
                .setOnCancelListener(dialog -> {
                    applyBlurEffect(false);
                    exitConfirmShowing = false;
                    rotationPauseShowing = false;
                    resumeGame();
                })
                .show();
    }

    private void showRotationPauseDialog() {
        if (gameOver || exitConfirmShowing || rotationPauseShowing) {
            return;
        }
        pauseGame();
        rotationPauseShowing = true;
        applyBlurEffect(true);

        new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.rotation_pause_title)
                .setMessage(R.string.rotation_pause_message)
                .setPositiveButton(R.string.resume_game, (dialog, which) -> {
                    applyBlurEffect(false);
                    rotationPauseShowing = false;
                    if (!gameOver && !exitConfirmShowing) {
                        resumeGame();
                    }
                })
                .setOnCancelListener(dialog -> {
                    applyBlurEffect(false);
                    rotationPauseShowing = false;
                    if (!gameOver && !exitConfirmShowing) {
                        resumeGame();
                    }
                })
                .show();
    }

    private void applyBlurEffect(boolean enabled) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && gameRoot != null) {
            if (enabled) {
                gameRoot.setRenderEffect(RenderEffect.createBlurEffect(18f, 18f, Shader.TileMode.CLAMP));
            } else {
                gameRoot.setRenderEffect(null);
            }
        }
    }

    private void showSettlementDialog(boolean activeExit) {
        pauseGame();
        gameOver = true;
        exitConfirmShowing = false;
        rotationPauseShowing = false;
        applyBlurEffect(false);
        long elapsedMs = Math.max(0L, SystemClock.elapsedRealtime() - startTimeMs);
        long minutes = elapsedMs / 60000L;
        long seconds = (elapsedMs % 60000L) / 1000L;
        String duration = String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds);

        String modeText = getString(
                GameConstants.MODE_CHALLENGE.equals(mode) ? R.string.mode_challenge : R.string.mode_endless
        );
        String message = getString(
                R.string.settlement_message,
                duration,
                modeText,
                lines,
                matchCoinsEarned
        );

        int title = activeExit ? R.string.exit_summary_title : R.string.game_over_title;
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_settlement_actions, null, false);
        TextView tvTitle = dialogView.findViewById(R.id.tvSettlementTitle);
        TextView tvMessage = dialogView.findViewById(R.id.tvSettlementMessage);
        MaterialButton btnRestart = dialogView.findViewById(R.id.btnSettlementRestart);
        MaterialButton btnBack = dialogView.findViewById(R.id.btnSettlementBack);

        tvTitle.setText(title);
        tvMessage.setText(message);

        androidx.appcompat.app.AlertDialog dialog = new MaterialAlertDialogBuilder(this)
                .setView(dialogView)
                .setCancelable(false)
                .create();

        btnRestart.setOnClickListener(v -> {
            dialog.dismiss();
            startNewGame();
        });
        btnBack.setOnClickListener(v -> {
            dialog.dismiss();
            Intent intent = new Intent(this, WelcomeActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            finish();
        });

        dialog.show();
    }
}
