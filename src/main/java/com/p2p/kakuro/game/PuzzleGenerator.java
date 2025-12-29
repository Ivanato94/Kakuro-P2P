package com.p2p.kakuro.game;

import java.util.*;

public class PuzzleGenerator {

    private static final Random random = new Random();

    public enum Difficulty {
        EASY(6, 6),
        MEDIUM(8, 8),
        HARD(10, 10);

        private final int rows;
        private final int cols;

        Difficulty(int rows, int cols) {
            this.rows = rows;
            this.cols = cols;
        }

        public int getRows() { return rows; }
        public int getCols() { return cols; }
    }

    public static GameBoard generate(Difficulty difficulty) {
        return generate(difficulty.getRows(), difficulty.getCols());
    }

    public static GameBoard generate(int rows, int cols) {
        GameBoard board = new GameBoard(rows, cols);
        createPattern(board, rows, cols);
        return board;
    }

    private static void createPattern(GameBoard board, int rows, int cols) {
        int[][] solutions = new int[rows][cols];
        boolean[][] isPlayable = new boolean[rows][cols];
        
        for (int i = 1; i < rows; i++) {
            for (int j = 1; j < cols; j++) {
                if ((i + j) % 3 != 0) {
                    isPlayable[i][j] = true;
                }
            }
        }
        
        ensureValidRuns(isPlayable, rows, cols);
        fillSolutions(solutions, isPlayable, rows, cols);
        
        for (int i = 1; i < rows; i++) {
            for (int j = 1; j < cols; j++) {
                if (isPlayable[i][j]) {
                    board.setCell(i, j, new BoardCell(solutions[i][j], true));
                }
            }
        }
        
        createClues(board, solutions, isPlayable, rows, cols);
    }

    private static void ensureValidRuns(boolean[][] isPlayable, int rows, int cols) {
        for (int i = 1; i < rows; i++) {
            int runStart = -1;
            for (int j = 1; j <= cols; j++) {
                if (j < cols && isPlayable[i][j]) {
                    if (runStart == -1) runStart = j;
                } else {
                    if (runStart != -1) {
                        int runLength = j - runStart;
                        if (runLength == 1) {
                            if (j < cols && random.nextBoolean()) {
                                isPlayable[i][j] = true;
                            } else {
                                isPlayable[i][runStart] = false;
                            }
                        }
                    }
                    runStart = -1;
                }
            }
        }
        
        for (int j = 1; j < cols; j++) {
            int runStart = -1;
            for (int i = 1; i <= rows; i++) {
                if (i < rows && isPlayable[i][j]) {
                    if (runStart == -1) runStart = i;
                } else {
                    if (runStart != -1) {
                        int runLength = i - runStart;
                        if (runLength == 1) {
                            if (i < rows && random.nextBoolean()) {
                                isPlayable[i][j] = true;
                            } else {
                                isPlayable[runStart][j] = false;
                            }
                        }
                    }
                    runStart = -1;
                }
            }
        }
    }

    private static void fillSolutions(int[][] solutions, boolean[][] isPlayable, int rows, int cols) {
        fillSolutionsBacktrack(solutions, isPlayable, rows, cols, 1, 1);
    }

    private static boolean fillSolutionsBacktrack(int[][] solutions, boolean[][] isPlayable, 
                                                   int rows, int cols, int row, int col) {
        while (row < rows && (col >= cols || !isPlayable[row][col])) {
            col++;
            if (col >= cols) {
                col = 1;
                row++;
            }
        }
        
        if (row >= rows) return true;
        
        List<Integer> numbers = new ArrayList<>();
        for (int n = 1; n <= 9; n++) numbers.add(n);
        Collections.shuffle(numbers, random);
        
        for (int num : numbers) {
            if (isValidPlacement(solutions, isPlayable, rows, cols, row, col, num)) {
                solutions[row][col] = num;
                
                int nextCol = col + 1;
                int nextRow = row;
                if (nextCol >= cols) {
                    nextCol = 1;
                    nextRow++;
                }
                
                if (fillSolutionsBacktrack(solutions, isPlayable, rows, cols, nextRow, nextCol)) {
                    return true;
                }
                solutions[row][col] = 0;
            }
        }
        return false;
    }

    private static boolean isValidPlacement(int[][] solutions, boolean[][] isPlayable,
                                            int rows, int cols, int row, int col, int num) {
        int hStart = col;
        while (hStart > 0 && isPlayable[row][hStart - 1]) hStart--;
        for (int j = hStart; j < cols && isPlayable[row][j]; j++) {
            if (j != col && solutions[row][j] == num) return false;
        }
        
        int vStart = row;
        while (vStart > 0 && isPlayable[vStart - 1][col]) vStart--;
        for (int i = vStart; i < rows && isPlayable[i][col]; i++) {
            if (i != row && solutions[i][col] == num) return false;
        }
        
        return true;
    }

    private static void createClues(GameBoard board, int[][] solutions, 
                                    boolean[][] isPlayable, int rows, int cols) {
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                if (isPlayable[i][j]) continue;
                
                int hClue = -1;
                int vClue = -1;
                
                if (j + 1 < cols && isPlayable[i][j + 1]) {
                    hClue = 0;
                    for (int k = j + 1; k < cols && isPlayable[i][k]; k++) {
                        hClue += solutions[i][k];
                    }
                }
                
                if (i + 1 < rows && isPlayable[i + 1][j]) {
                    vClue = 0;
                    for (int k = i + 1; k < rows && isPlayable[k][j]; k++) {
                        vClue += solutions[k][j];
                    }
                }
                
                if (hClue > 0 || vClue > 0) {
                    board.setCell(i, j, new BoardCell(hClue, vClue));
                }
            }
        }
    }

    public static GameBoard generateSimpleTest() {
        GameBoard board = new GameBoard(5, 5);
        
        board.setCell(0, 0, new BoardCell());
        board.setCell(0, 1, new BoardCell(-1, 16));
        board.setCell(0, 2, new BoardCell(-1, 17));
        board.setCell(0, 3, new BoardCell(-1, 3));
        board.setCell(0, 4, new BoardCell());
        
        board.setCell(1, 0, new BoardCell(16, -1));
        board.setCell(1, 1, new BoardCell(7, true));
        board.setCell(1, 2, new BoardCell(9, true));
        board.setCell(1, 3, new BoardCell());
        board.setCell(1, 4, new BoardCell());
        
        board.setCell(2, 0, new BoardCell(17, -1));
        board.setCell(2, 1, new BoardCell(9, true));
        board.setCell(2, 2, new BoardCell(8, true));
        board.setCell(2, 3, new BoardCell());
        board.setCell(2, 4, new BoardCell());
        
        board.setCell(3, 0, new BoardCell());
        board.setCell(3, 1, new BoardCell());
        board.setCell(3, 2, new BoardCell(3, 4));
        board.setCell(3, 3, new BoardCell(1, true));
        board.setCell(3, 4, new BoardCell(2, true));
        
        board.setCell(4, 0, new BoardCell());
        board.setCell(4, 1, new BoardCell());
        board.setCell(4, 2, new BoardCell(6, -1));
        board.setCell(4, 3, new BoardCell(2, true));
        board.setCell(4, 4, new BoardCell(4, true));
        
        return board;
    }
}
