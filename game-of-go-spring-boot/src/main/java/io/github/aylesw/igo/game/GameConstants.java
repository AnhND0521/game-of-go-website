package io.github.aylesw.igo.game;

public class GameConstants {
    public static final int EMPTY = 0;
    public static final int BLACK = 1;
    public static final int WHITE = 2;
    public static final int MARKER = 4;
    public static final int LIBERTY = 8;
    public static final int OFF_BOARD = 12;

    public static int oppositeColor(int color) {
        if (color != BLACK && color != WHITE) return 0;
        return 3 - color;
    }
}
