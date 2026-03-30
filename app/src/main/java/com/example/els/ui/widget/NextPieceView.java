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

import com.example.els.R;
import com.example.els.game.Tetromino;

public class NextPieceView extends View {
    private int nextType = Tetromino.TYPE_I;

    private final Paint backgroundPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint[] blockPaints = new Paint[9];
    private final Paint bombLabelPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final RectF tempRect = new RectF();

    public NextPieceView(Context context) {
        super(context);
        init();
    }

    public NextPieceView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public NextPieceView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        backgroundPaint.setColor(ContextCompat.getColor(getContext(), R.color.board_cell_empty));
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
    }

    public void setNextType(int type) {
        nextType = type;
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        float width = getWidth() - getPaddingLeft() - getPaddingRight();
        float height = getHeight() - getPaddingTop() - getPaddingBottom();
        if (width <= 0 || height <= 0) {
            return;
        }

        float gridSize = Math.min(width, height);
        float cell = gridSize / 4f;
        float startX = getPaddingLeft() + (width - gridSize) / 2f;
        float startY = getPaddingTop() + (height - gridSize) / 2f;

        for (int row = 0; row < 4; row++) {
            for (int col = 0; col < 4; col++) {
                tempRect.set(
                        startX + col * cell + cell * 0.08f,
                        startY + row * cell + cell * 0.08f,
                        startX + (col + 1) * cell - cell * 0.08f,
                        startY + (row + 1) * cell - cell * 0.08f
                );
                canvas.drawRoundRect(tempRect, cell * 0.12f, cell * 0.12f, backgroundPaint);
            }
        }

        int[][] cells = new Tetromino(nextType).cells();
        int minRow = Integer.MAX_VALUE;
        int minCol = Integer.MAX_VALUE;
        int maxRow = Integer.MIN_VALUE;
        int maxCol = Integer.MIN_VALUE;
        for (int[] c : cells) {
            minRow = Math.min(minRow, c[0]);
            minCol = Math.min(minCol, c[1]);
            maxRow = Math.max(maxRow, c[0]);
            maxCol = Math.max(maxCol, c[1]);
        }

        int shapeRows = maxRow - minRow + 1;
        int shapeCols = maxCol - minCol + 1;
        int rowOffset = (4 - shapeRows) / 2 - minRow;
        int colOffset = (4 - shapeCols) / 2 - minCol;

        int colorCode = Tetromino.colorCodeForType(nextType);
        Paint paint = blockPaints[Math.min(Math.max(colorCode, 0), blockPaints.length - 1)];
        for (int[] c : cells) {
            int r = c[0] + rowOffset;
            int col = c[1] + colOffset;
            tempRect.set(
                    startX + col * cell + cell * 0.08f,
                    startY + r * cell + cell * 0.08f,
                    startX + (col + 1) * cell - cell * 0.08f,
                    startY + (r + 1) * cell - cell * 0.08f
            );
            canvas.drawRoundRect(tempRect, cell * 0.14f, cell * 0.14f, paint);
            if (nextType == Tetromino.TYPE_BOMB) {
                bombLabelPaint.setTextSize(cell * 0.5f);
                canvas.drawText("B", tempRect.centerX(), tempRect.centerY() + cell * 0.16f, bombLabelPaint);
            }
        }
    }
}



