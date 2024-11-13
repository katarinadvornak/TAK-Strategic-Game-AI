package com.Tak.Logic;

/**
 * Enumeration of possible movement directions.
 */
public enum Direction {
    UP(0, 1),
    DOWN(0, -1),
    LEFT(-1, 0),
    RIGHT(1, 0);

    private int deltaX;
    private int deltaY;

    Direction(int dx, int dy) {
        this.deltaX = dx;
        this.deltaY = dy;
    }

    public int getDeltaX() {
        return this.deltaX;
    }

    public int getDeltaY() {
        return this.deltaY;
    }
}
