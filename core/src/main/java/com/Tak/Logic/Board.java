package com.Tak.Logic;

public class Board {
    private Piece[][] grid;
    private int size;

    public Board(int size) {
        this.size = size;
        grid = new Piece[size][size];  // Initialize the grid with empty cells.
    }

    public void placePiece(int x, int y, Piece piece) {
        grid[x][y] = piece;
    }

    public Piece getPiece(int x, int y) {
        return grid[x][y];
    }
}
