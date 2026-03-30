package com.example.els.game;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.List;
import java.util.Random;

public class Tetromino {
    public static final int TYPE_I = 0;
    public static final int TYPE_O = 1;
    public static final int TYPE_T = 2;
    public static final int TYPE_L = 3;
    public static final int TYPE_J = 4;
    public static final int TYPE_S = 5;
    public static final int TYPE_Z = 6;
    public static final int TYPE_BOMB = 7;

    private static final int[][][][] SHAPES = new int[][][][]{
            {
                    {{0, 0}, {0, 1}, {0, 2}, {0, 3}},
                    {{0, 1}, {1, 1}, {2, 1}, {3, 1}},
                    {{1, 0}, {1, 1}, {1, 2}, {1, 3}},
                    {{0, 2}, {1, 2}, {2, 2}, {3, 2}}
            },
            {
                    {{0, 0}, {0, 1}, {1, 0}, {1, 1}},
                    {{0, 0}, {0, 1}, {1, 0}, {1, 1}},
                    {{0, 0}, {0, 1}, {1, 0}, {1, 1}},
                    {{0, 0}, {0, 1}, {1, 0}, {1, 1}}
            },
            {
                    {{0, 1}, {1, 0}, {1, 1}, {1, 2}},
                    {{0, 1}, {1, 1}, {1, 2}, {2, 1}},
                    {{1, 0}, {1, 1}, {1, 2}, {2, 1}},
                    {{0, 1}, {1, 0}, {1, 1}, {2, 1}}
            },
            {
                    {{0, 2}, {1, 0}, {1, 1}, {1, 2}},
                    {{0, 1}, {1, 1}, {2, 1}, {2, 2}},
                    {{1, 0}, {1, 1}, {1, 2}, {2, 0}},
                    {{0, 0}, {0, 1}, {1, 1}, {2, 1}}
            },
            {
                    {{0, 0}, {1, 0}, {1, 1}, {1, 2}},
                    {{0, 1}, {0, 2}, {1, 1}, {2, 1}},
                    {{1, 0}, {1, 1}, {1, 2}, {2, 2}},
                    {{0, 1}, {1, 1}, {2, 0}, {2, 1}}
            },
            {
                    {{0, 1}, {0, 2}, {1, 0}, {1, 1}},
                    {{0, 1}, {1, 1}, {1, 2}, {2, 2}},
                    {{1, 1}, {1, 2}, {2, 0}, {2, 1}},
                    {{0, 0}, {1, 0}, {1, 1}, {2, 1}}
            },
            {
                    {{0, 0}, {0, 1}, {1, 1}, {1, 2}},
                    {{0, 2}, {1, 1}, {1, 2}, {2, 1}},
                    {{1, 0}, {1, 1}, {2, 1}, {2, 2}},
                    {{0, 1}, {1, 0}, {1, 1}, {2, 0}}
            },
            {
                    {{0, 0}},
                    {{0, 0}},
                    {{0, 0}},
                    {{0, 0}}
            }
    };

    public int type;
    public int rotation;
    public int row;
    public int col;

    public Tetromino(int type) {
        this.type = type;
        this.rotation = 0;
        this.row = 0;
        this.col = 3;
    }

    public Tetromino copy() {
        Tetromino copy = new Tetromino(type);
        copy.rotation = rotation;
        copy.row = row;
        copy.col = col;
        return copy;
    }

    public int[][] cells() {
        return SHAPES[type][rotation];
    }

    public int[][] cellsForRotation(int nextRotation) {
        return SHAPES[type][nextRotation];
    }

    public int nextRotation() {
        return (rotation + 1) % SHAPES[type].length;
    }

    public static int colorCodeForType(int type) {
        return type + 1;
    }

    public static final class BagGenerator {
        private final Random random = new Random();
        private final Deque<Integer> bag = new ArrayDeque<>();

        public int nextClassicType() {
            if (bag.isEmpty()) {
                refill();
            }
            return bag.removeFirst();
        }

        private void refill() {
            List<Integer> pool = new ArrayList<>();
            for (int i = TYPE_I; i <= TYPE_Z; i++) {
                pool.add(i);
            }
            Collections.shuffle(pool, random);
            bag.addAll(pool);
        }
    }
}


