package com.p2p.kakuro.challenge;

import com.p2p.kakuro.game.GameBoard;
import com.p2p.kakuro.game.PuzzleGenerator;

import java.io.Serializable;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class GameSession implements Serializable {
    private static final long serialVersionUID = 1L;

    public enum Status {
        WAITING,
        RUNNING,
        FINISHED
    }

    private final String name;
    private final boolean isPublic;
    private final String ownerNickname;
    private final GameBoard masterBoard;
    private final Map<String, GameBoard> playerBoards;
    private final Map<String, Integer> scores;
    private final Set<String> participants;
    private final Set<String> foundCells;
    private Status status;
    private String winner;
    private long version;

    public GameSession(String name, boolean isPublic, String ownerNickname, PuzzleGenerator.Difficulty difficulty) {
        this.name = name;
        this.isPublic = isPublic;
        this.ownerNickname = ownerNickname;
        this.masterBoard = PuzzleGenerator.generate(difficulty);
        this.playerBoards = new ConcurrentHashMap<>();
        this.scores = new ConcurrentHashMap<>();
        this.participants = ConcurrentHashMap.newKeySet();
        this.foundCells = ConcurrentHashMap.newKeySet();
        this.status = Status.WAITING;
        this.winner = null;
        this.version = 0;
    }

    public GameSession(String name, boolean isPublic, String ownerNickname) {
        this(name, isPublic, ownerNickname, PuzzleGenerator.Difficulty.MEDIUM);
    }

    public GameSession(String name, boolean isPublic, String ownerNickname, GameBoard board) {
        this.name = name;
        this.isPublic = isPublic;
        this.ownerNickname = ownerNickname;
        this.masterBoard = board;
        this.playerBoards = new ConcurrentHashMap<>();
        this.scores = new ConcurrentHashMap<>();
        this.participants = ConcurrentHashMap.newKeySet();
        this.foundCells = ConcurrentHashMap.newKeySet();
        this.status = Status.WAITING;
        this.winner = null;
        this.version = 0;
    }

    public String getName() { return name; }
    public boolean isPublic() { return isPublic; }
    public String getOwnerNickname() { return ownerNickname; }
    public Status getStatus() { return status; }
    public String getWinner() { return winner; }
    public long getVersion() { return version; }
    public GameBoard getMasterBoard() { return masterBoard; }
    public Set<String> getParticipants() { return new HashSet<>(participants); }
    public int getParticipantCount() { return participants.size(); }
    public Map<String, Integer> getScores() { return new HashMap<>(scores); }
    public int getScore(String nickname) { return scores.getOrDefault(nickname, 0); }

    public void incrementVersion() {
        this.version++;
    }

    public boolean join(String nickname) {
        if (status == Status.FINISHED) return false;
        
        if (participants.add(nickname)) {
            playerBoards.put(nickname, masterBoard.createPlayerView());
            scores.put(nickname, 0);
            incrementVersion();
            return true;
        }
        return false;
    }

    public boolean leave(String nickname) {
        if (participants.remove(nickname)) {
            playerBoards.remove(nickname);
            scores.remove(nickname);
            incrementVersion();
            return true;
        }
        return false;
    }

    public boolean start(String requestingNickname) {
        if (!requestingNickname.equals(ownerNickname)) return false;
        if (status != Status.WAITING) return false;
        if (participants.size() < 2) return false;
        
        status = Status.RUNNING;
        incrementVersion();
        return true;
    }

    public int placeNumber(String nickname, int row, int col, int number) {
        if (status != Status.RUNNING) return 0;
        if (!participants.contains(nickname)) return 0;
        
        GameBoard playerBoard = playerBoards.get(nickname);
        if (playerBoard == null) return 0;
        
        int correctValue = masterBoard.getCell(row, col).getSolution();
        int scoreChange = 0;
        
        if (number == correctValue) {
            String cellKey = row + "," + col + "," + number;
            if (foundCells.add(cellKey)) {
                scoreChange = 1;
            }
            playerBoard.placeNumber(row, col, number);
        }
        
        int currentScore = scores.getOrDefault(nickname, 0);
        scores.put(nickname, currentScore + scoreChange);
        
        if (playerBoard.isComplete()) {
            status = Status.FINISHED;
            winner = nickname;
        }
        
        incrementVersion();
        return scoreChange;
    }

    public GameBoard getPlayerBoard(String nickname) {
        return playerBoards.get(nickname);
    }

    public boolean isParticipating(String nickname) {
        return participants.contains(nickname);
    }

    public boolean isOwner(String nickname) {
        return ownerNickname.equals(nickname);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GameSession session = (GameSession) o;
        return name.equalsIgnoreCase(session.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name.toLowerCase());
    }

    @Override
    public String toString() {
        return name + " (" + (isPublic ? "Public" : "Private") + ", " + status + ", " + participants.size() + " players)";
    }
}
