package com.example.els.ui.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.ColorUtils;

import com.example.els.R;
import com.example.els.game.GameConstants;
import com.example.els.game.Tetromino;

public class TetrisBoardView extends View {
    private final int[][] board = new int[GameConstants.BOARD_ROWS][GameConstants.BOARD_COLS];
    private Tetromino currentPiece;

    private final Paint emptyCellPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint gridLinePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint[] blockPaints = new Paint[9];
    private final Paint bombLabelPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint guideOutlinePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final RectF tempRect = new RectF();

    private boolean landingGuideEnabled;

    public TetrisBoardView(Context context) {
        super(context);
        init();
    }

    public TetrisBoardView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public TetrisBoardView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        float density = getResources().getDisplayMetrics().density;

        emptyCellPaint.setColor(ContextCompat.getColor(getContext(), R.color.board_cell_empty));
        gridLinePaint.setColor(ContextCompat.getColor(getContext(), R.color.board_grid_line));
        gridLinePaint.setStrokeWidth(density);

        int[] colors = new int[]{
                Color.TRANSPARENT,
                ContextCompat.getColor(getContext(), R.color.tetris_i),
                ContextCompat.getColor(getContext(), R.color.tetris_o),
                ContextCompat.getColor(getContext(), R.color.tetris_t),
                ContextCompat.getColor(getContext(), R.color.tetris_l),
                ContextCompat.getColor(getContext(), R.color.tetris_j),
                ContextCompat.getColor(getContext(), R.color.tetris_s),
                ContextCompat.getColor(getContext(), R.color.tetris_z),
                ContextCompat.getColor(getContext(), R.color.tetris_bomb)
        };
        for (int i = 0; i < blockPaints.length; i++) {
            Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
            paint.setStyle(Paint.Style.FILL);
            paint.setColor(colors[i]);
            blockPaints[i] = paint;
        }

        bombLabelPaint.setColor(ContextCompat.getColor(getContext(), R.color.on_tetris_bomb));
        bombLabelPaint.setTextAlign(Paint.Align.CENTER);
        bombLabelPaint.setFakeBoldText(true);

        guideOutlinePaint.setColor(ColorUtils.setAlphaComponent(
                ContextCompat.getColor(getContext(), R.color.tetris_i),
                190
        ));
        guideOutlinePaint.setStyle(Paint.Style.STROKE);
        guideOutlinePaint.setStrokeWidth(Math.max(1f, density * 1.4f));
    }

    public void setLandingGuideEnabled(boolean enabled) {
        if (landingGuideEnabled == enabled) {
            return;
        }
        landingGuideEnabled = enabled;
        invalidate();
    }

    public void updateState(int[][] nextBoard, @Nullable Tetromino activePiece) {
        for (int r = 0; r < GameConstants.BOARD_ROWS; r++) {
            System.arraycopy(nextBoard[r], 0, board[r], 0, GameConstants.BOARD_COLS);
        }
        currentPiece = activePiece == null ? null : activePiece.copy();
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        float availableWidth = getWidth() - getPaddingLeft() - getPaddingRight();
        float availableHeight = getHeight() - getPaddingTop() - getPaddingBottom();
        if (availableWidth <= 0 || availableHeight <= 0) {
            return;
        }

        float cellSize = Math.min(availableWidth / GameConstants.BOARD_COLS, availableHeight / GameConstants.BOARD_ROWS);
        float boardWidth = cellSize * GameConstants.BOARD_COLS;
        float boardHeight = cellSize * GameConstants.BOARD_ROWS;
        float startX = getPaddingLeft() + (availableWidth - boardWidth) / 2f;
        float startY = getPaddingTop() + (availableHeight - boardHeight) / 2f;

        for (int row = 0; row < GameConstants.BOARD_ROWS; row++) {
            for (int col = 0; col < GameConstants.BOARD_COLS; col++) {
                float left = startX + col * cellSize;
                float top = startY + row * cellSize;
                drawCell(canvas, left, top, cellSize, board[row][col]);
            }
        }

        if (currentPiece != null) {
            if (landingGuideEnabled) {
                drawLandingGuide(canvas, startX, startY, cellSize, currentPiece);
            }

            int colorCode = Tetromino.colorCodeForType(currentPiece.type);
            for (int[] offset : currentPiece.cells()) {
                int row = currentPiece.row + offset[0];
                int col = currentPiece.col + offset[1];
                if (row < 0 || row >= GameConstants.BOARD_ROWS || col < 0 || col >= GameConstants.BOARD_COLS) {
                    continue;
                }
                float left = startX + col * cellSize;
                float top = startY + row * cellSize;
                drawCell(canvas, left, top, cellSize, colorCode);
            }
        }

        for (int r = 0; r <= GameConstants.BOARD_ROWS; r++) {
            float y = startY + r * cellSize;
            canvas.drawLine(startX, y, startX + boardWidth, y, gridLinePaint);
        }
        for (int c = 0; c <= GameConstants.BOARD_COLS; c++) {
            float x = startX + c * cellSize;
            canvas.drawLine(x, startY, x, startY + boardHeight, gridLinePaint);
        }
    }

    private void drawLandingGuide(
            Canvas canvas,
            float startX,
            float startY,
            float cellSize,
            Tetromino activePiece
    ) {
        Tetromino landingPiece = computeLandingPiece(activePiece);
        if (landingPiece == null) {
            return;
        }

        if (landingPiece.row == activePiece.row
                && landingPiece.col == activePiece.col
                && landingPiece.rotation == activePiece.rotation) {
            return;
        }

        int colorCode = Tetromino.colorCodeForType(activePiece.type);
        int guideBaseColor = (colorCode > 0 && colorCode < blockPaints.length)
                ? blockPaints[colorCode].getColor()
                : ContextCompat.getColor(getContext(), R.color.tetris_i);
        guideOutlinePaint.setColor(ColorUtils.setAlphaComponent(guideBaseColor, 210));

        for (int[] offset : landingPiece.cells()) {
            int row = landingPiece.row + offset[0];
            int col = landingPiece.col + offset[1];

            float left = startX + col * cellSize;
            float top = startY + row * cellSize;
            tempRect.set(
                    left + cellSize * 0.14f,
                    top + cellSize * 0.14f,
                    left + cellSize * 0.86f,
                    top + cellSize * 0.86f
            );
            canvas.drawRoundRect(tempRect, cellSize * 0.12f, cellSize * 0.12f, guideOutlinePaint);
        }
    }

    @Nullable
    private Tetromino computeLandingPiece(Tetromino activePiece) {
        Tetromino landing = activePiece.copy();
        while (true) {
            Tetromino next = landing.copy();
            next.row += 1;
            if (!canPlaceOnBoard(next)) {
                return landing;
            }
            landing = next;
        }
    }

    private boolean canPlaceOnBoard(Tetromino piece) {
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

    private void drawCell(Canvas canvas, float left, float top, float cellSize, int colorCode) {
        tempRect.set(
                left + cellSize * 0.08f,
                top + cellSize * 0.08f,
                left + cellSize * 0.92f,
                top + cellSize * 0.92f
        );
        if (colorCode <= 0 || colorCode >= blockPaints.length) {
            canvas.drawRoundRect(tempRect, cellSize * 0.14f, cellSize * 0.14f, emptyCellPaint);
            return;
        }
        canvas.drawRoundRect(tempRect, cellSize * 0.14f, cellSize * 0.14f, blockPaints[colorCode]);
        if (colorCode == Tetromino.colorCodeForType(Tetromino.TYPE_BOMB)) {
            bombLabelPaint.setTextSize(cellSize * 0.55f);
            float x = tempRect.centerX();
            float y = tempRect.centerY() + cellSize * 0.18f;
            canvas.drawText("B", x, y, bombLabelPaint);
        }
    }
}