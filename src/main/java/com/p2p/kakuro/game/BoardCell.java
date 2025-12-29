package com.p2p.kakuro.game;

import java.io.Serializable;

public class BoardCell implements Serializable {
    private static final long serialVersionUID = 1L;

    public enum CellType {
        BLOCKED,
        CLUE,
        PLAYABLE
    }

    private final CellType type;
    private final int horizontalClue;
    private final int verticalClue;
    private int value;
    private final int solution;

    public BoardCell() {
        this.type = CellType.BLOCKED;
        this.horizontalClue = -1;
        this.verticalClue = -1;
        this.value = 0;
        this.solution = 0;
    }

    public BoardCell(int horizontalClue, int verticalClue) {
        this.type = CellType.CLUE;
        this.horizontalClue = horizontalClue;
        this.verticalClue = verticalClue;
        this.value = 0;
        this.solution = 0;
    }

    public BoardCell(int solution, boolean isPlayable) {
        this.type = CellType.PLAYABLE;
        this.horizontalClue = -1;
        this.verticalClue = -1;
        this.value = 0;
        this.solution = solution;
    }

    public CellType getType() {
        return type;
    }

    public boolean isPlayable() {
        return type == CellType.PLAYABLE;
    }

    public boolean isClue() {
        return type == CellType.CLUE;
    }

    public boolean isBlocked() {
        return type == CellType.BLOCKED;
    }

    public int getHorizontalClue() {
        return horizontalClue;
    }

    public int getVerticalClue() {
        return verticalClue;
    }

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        if (type == CellType.PLAYABLE && value >= 0 && value <= 9) {
            this.value = value;
        }
    }

    public int getSolution() {
        return solution;
    }

    public boolean isEmpty() {
        return type == CellType.PLAYABLE && value == 0;
    }

    public boolean isCorrect() {
        return type == CellType.PLAYABLE && value == solution;
    }

    public BoardCell copyForView() {
        BoardCell copy;
        switch (type) {
            case CLUE:
                copy = new BoardCell(horizontalClue, verticalClue);
                break;
            case PLAYABLE:
                copy = new BoardCell(solution, true);
                copy.value = this.value;
                break;
            default:
                copy = new BoardCell();
        }
        return copy;
    }
}
