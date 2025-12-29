package com.p2p.kakuro.network;

import net.tomp2p.peers.PeerAddress;

import java.io.Serializable;

public interface P2PMessageHandler {
    
    void onMessageReceived(PeerAddress sender, GameMessage message);
    
    class GameMessage implements Serializable {
        private static final long serialVersionUID = 1L;
        
        public enum Type {
            CHALLENGE_UPDATED,
            PUBLIC_CHALLENGES_UPDATED,
            PLAYER_JOINED,
            PLAYER_LEFT,
            CHALLENGE_STARTED,
            CHALLENGE_FINISHED,
            NUMBER_PLACED
        }
        
        private final Type type;
        private final String challengeName;
        private final String playerNickname;
        private final String extraInfo;
        
        public GameMessage(Type type, String challengeName, String playerNickname) {
            this(type, challengeName, playerNickname, null);
        }
        
        public GameMessage(Type type, String challengeName, String playerNickname, String extraInfo) {
            this.type = type;
            this.challengeName = challengeName;
            this.playerNickname = playerNickname;
            this.extraInfo = extraInfo;
        }
        
        public Type getType() { return type; }
        public String getChallengeName() { return challengeName; }
        public String getPlayerNickname() { return playerNickname; }
        public String getExtraInfo() { return extraInfo; }
        
        @Override
        public String toString() {
            return "GameMessage{" + type + ", " + challengeName + ", " + playerNickname + "}";
        }
    }
}
