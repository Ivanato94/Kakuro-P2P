package com.p2p.kakuro.game;

import java.io.Serializable;

public class GameBoard implements Serializable {
    private static final long serialVersionUID = 1L;

    private final int rows;
    private final int cols;
    private final BoardCell[][] grid;

    public GameBoard(int rows, int cols) {
        this.rows = rows;
        this.cols = cols;
        this.grid = new BoardCell[rows][cols];
        
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                grid[i][j] = new BoardCell();
            }
        }
    }

    public int getRows() {
        return rows;
    }

    public int getCols() {
        return cols;
    }

    public BoardCell getCell(int row, int col) {
        if (row >= 0 && row < rows && col >= 0 && col < cols) {
            return grid[row][col];
        }
        return null;
    }

    public void setCell(int row, int col, BoardCell cell) {
        if (row >= 0 && row < rows && col >= 0 && col < cols) {
            grid[row][col] = cell;
        }
    }

    public boolean placeNumber(int row, int col, int number) {
        BoardCell cell = getCell(row, col);
        if (cell != null && cell.isPlayable() && number >= 1 && number <= 9) {
            cell.setValue(number);
            return cell.isCorrect();
        }
        return false;
    }

    public void clearCell(int row, int col) {
        BoardCell cell = getCell(row, col);
        if (cell != null && cell.isPlayable()) {
            cell.setValue(0);
        }
    }

    public boolean isComplete() {
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                BoardCell cell = grid[i][j];
                if (cell.isPlayable() && !cell.isCorrect()) {
                    return false;
                }
            }
        }
        return true;
    }

    public int getEmptyCellCount() {
        int count = 0;
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                if (grid[i][j].isEmpty()) {
                    count++;
                }
            }
        }
        return count;
    }

    public int getPlayableCellCount() {
        int count = 0;
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                if (grid[i][j].isPlayable()) {
                    count++;
                }
            }
        }
        return count;
    }

    public GameBoard copy() {
        GameBoard copy = new GameBoard(rows, cols);
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                copy.grid[i][j] = grid[i][j].copyForView();
            }
        }
        return copy;
    }

    public GameBoard createPlayerView() {
        GameBoard view = new GameBoard(rows, cols);
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                BoardCell original = grid[i][j];
                if (original.isPlayable()) {
                    view.grid[i][j] = new BoardCell(original.getSolution(), true);
                } else if (original.isClue()) {
                    view.grid[i][j] = new BoardCell(original.getHorizontalClue(), original.getVerticalClue());
                } else {
                    view.grid[i][j] = new BoardCell();
                }
            }
        }
        return view;
    }
}
