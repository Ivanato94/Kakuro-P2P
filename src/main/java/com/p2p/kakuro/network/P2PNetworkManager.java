package com.p2p.kakuro.network;

import com.p2p.kakuro.challenge.GameSession;
import com.p2p.kakuro.challenge.PlayerInfo;
import com.p2p.kakuro.game.GameBoard;
import com.p2p.kakuro.game.PuzzleGenerator;
import com.p2p.kakuro.network.P2PMessageHandler.GameMessage;

import net.tomp2p.dht.PeerBuilderDHT;
import net.tomp2p.dht.PeerDHT;
import net.tomp2p.futures.FutureBootstrap;
import net.tomp2p.futures.FutureDirect;
import net.tomp2p.p2p.PeerBuilder;
import net.tomp2p.peers.Number160;
import net.tomp2p.peers.PeerAddress;
import net.tomp2p.rpc.ObjectDataReply;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetAddress;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

public class P2PNetworkManager {
    private static final Logger logger = LoggerFactory.getLogger(P2PNetworkManager.class);

    private final PeerDHT peer;
    private PlayerInfo currentPlayer;
    private GameSession currentChallenge;
    private Set<String> publicChallenges;
    private final List<P2PMessageHandler> listeners;

    public P2PNetworkManager(int localPort, String masterAddress, int masterPort) throws Exception {
        this.listeners = new CopyOnWriteArrayList<>();
        this.publicChallenges = new HashSet<>();

        this.peer = new PeerBuilderDHT(new PeerBuilder(Number160.createHash(UUID.randomUUID().toString()))
                .ports(localPort)
                .start())
                .start();

        peer.peer().objectDataReply(new ObjectDataReply() {
            @Override
            public Object reply(PeerAddress sender, Object request) throws Exception {
                if (request instanceof GameMessage) {
                    handleMessage(sender, (GameMessage) request);
                }
                return "OK";
            }
        });

        if (masterAddress != null && !masterAddress.isEmpty()) {
            InetAddress address = InetAddress.getByName(masterAddress);
            FutureBootstrap bootstrap = peer.peer().bootstrap()
                    .inetAddress(address)
                    .ports(masterPort)
                    .start();
            bootstrap.awaitUninterruptibly();
            
            if (bootstrap.isSuccess()) {
                logger.info("Connected to network via {}:{}", masterAddress, masterPort);
            } else {
                logger.warn("Bootstrap failed, starting as master node");
            }
        } else {
            logger.info("Started as master node on port {}", localPort);
        }
    }

    public void addListener(P2PMessageHandler listener) {
        listeners.add(listener);
    }

    public void removeListener(P2PMessageHandler listener) {
        listeners.remove(listener);
    }

    private void handleMessage(PeerAddress sender, GameMessage message) {
        logger.debug("Received: {}", message);
        
        switch (message.getType()) {
            case PUBLIC_CHALLENGES_UPDATED:
                refreshPublicChallenges();
                break;
            case CHALLENGE_UPDATED:
            case PLAYER_JOINED:
            case PLAYER_LEFT:
            case CHALLENGE_STARTED:
            case NUMBER_PLACED:
            case CHALLENGE_FINISHED:
                if (currentChallenge != null && 
                    message.getChallengeName().equals(currentChallenge.getName())) {
                    refreshCurrentChallenge();
                }
                break;
        }
        
        for (P2PMessageHandler listener : listeners) {
            listener.onMessageReceived(sender, message);
        }
    }

    public boolean login(String nickname) throws IOException {
        if (nickname == null || nickname.trim().isEmpty()) {
            return false;
        }

        Set<PlayerInfo> loggedPlayers = getLoggedPlayers();
        for (PlayerInfo p : loggedPlayers) {
            if (p.getNickname().equalsIgnoreCase(nickname)) {
                return false;
            }
        }

        currentPlayer = new PlayerInfo(nickname, peer.peer().peerAddress().inetAddress(), 
                                   peer.peer().peerAddress().tcpPort());
        loggedPlayers.add(currentPlayer);
        DHTOperations.put(peer, DHTOperations.LOGGED_PLAYERS_KEY, (java.io.Serializable) loggedPlayers);

        refreshPublicChallenges();

        logger.info("Logged in as: {}", nickname);
        return true;
    }

    public void logout() {
        if (currentPlayer == null) return;

        if (currentChallenge != null) {
            leaveChallenge();
        }

        Set<PlayerInfo> loggedPlayers = getLoggedPlayers();
        loggedPlayers.removeIf(p -> p.getNickname().equals(currentPlayer.getNickname()));
        DHTOperations.put(peer, DHTOperations.LOGGED_PLAYERS_KEY, (java.io.Serializable) loggedPlayers);

        logger.info("Logged out: {}", currentPlayer.getNickname());
        currentPlayer = null;
    }

    @SuppressWarnings("unchecked")
    private Set<PlayerInfo> getLoggedPlayers() {
        Set<PlayerInfo> players = DHTOperations.get(peer, DHTOperations.LOGGED_PLAYERS_KEY, HashSet.class);
        return players != null ? players : new HashSet<>();
    }

    public boolean createChallenge(String name, boolean isPublic, PuzzleGenerator.Difficulty difficulty) {
        if (currentPlayer == null || name == null || name.trim().isEmpty()) {
            return false;
        }
        if (currentChallenge != null) {
            return false;
        }

        Number160 key = DHTOperations.createChallengeKey(name);
        if (DHTOperations.get(peer, key, GameSession.class) != null) {
            return false;
        }

        GameSession challenge = new GameSession(name, isPublic, currentPlayer.getNickname(), difficulty);
        challenge.join(currentPlayer.getNickname());
        
        if (!DHTOperations.put(peer, key, challenge)) {
            return false;
        }

        currentChallenge = challenge;

        if (isPublic) {
            addToPublicChallenges(name);
        }

        logger.info("Created challenge: {}", name);
        return true;
    }

    public boolean joinChallenge(String name) {
        if (currentPlayer == null || currentChallenge != null) {
            return false;
        }

        Number160 key = DHTOperations.createChallengeKey(name);
        GameSession challenge = DHTOperations.get(peer, key, GameSession.class);
        
        if (challenge == null || challenge.getStatus() == GameSession.Status.FINISHED) {
            return false;
        }

        if (!challenge.join(currentPlayer.getNickname())) {
            return false;
        }

        if (!DHTOperations.put(peer, key, challenge)) {
            return false;
        }

        currentChallenge = challenge;
        notifyChallenge(GameMessage.Type.PLAYER_JOINED);

        logger.info("Joined challenge: {}", name);
        return true;
    }

    public boolean leaveChallenge() {
        if (currentPlayer == null || currentChallenge == null) {
            return false;
        }

        String name = currentChallenge.getName();
        Number160 key = DHTOperations.createChallengeKey(name);
        
        GameSession challenge = DHTOperations.get(peer, key, GameSession.class);
        if (challenge != null) {
            challenge.leave(currentPlayer.getNickname());
            
            if (challenge.getParticipantCount() == 0) {
                DHTOperations.remove(peer, key);
                if (challenge.isPublic()) {
                    removeFromPublicChallenges(name);
                }
            } else {
                DHTOperations.put(peer, key, challenge);
                notifyChallenge(GameMessage.Type.PLAYER_LEFT);
            }
        }

        currentChallenge = null;
        logger.info("Left challenge: {}", name);
        return true;
    }

    public boolean startChallenge() {
        if (currentPlayer == null || currentChallenge == null) {
            return false;
        }

        Number160 key = DHTOperations.createChallengeKey(currentChallenge.getName());
        GameSession challenge = DHTOperations.get(peer, key, GameSession.class);
        
        if (challenge == null || !challenge.start(currentPlayer.getNickname())) {
            return false;
        }

        if (!DHTOperations.put(peer, key, challenge)) {
            return false;
        }

        currentChallenge = challenge;
        notifyChallenge(GameMessage.Type.CHALLENGE_STARTED);

        logger.info("Started challenge: {}", currentChallenge.getName());
        return true;
    }

    public int placeNumber(int row, int col, int number) {
        if (currentPlayer == null || currentChallenge == null) {
            return 0;
        }

        Number160 key = DHTOperations.createChallengeKey(currentChallenge.getName());
        
        for (int attempt = 0; attempt < 3; attempt++) {
            GameSession challenge = DHTOperations.get(peer, key, GameSession.class);
            
            if (challenge == null) {
                return 0;
            }

            int scoreChange = challenge.placeNumber(currentPlayer.getNickname(), row, col, number);
            
            GameSession result = DHTOperations.putWithVersion(peer, key, challenge);
            
            if (result != null && result.getVersion() >= challenge.getVersion()) {
                currentChallenge = result;
                
                GameMessage.Type msgType = result.getStatus() == GameSession.Status.FINISHED 
                        ? GameMessage.Type.CHALLENGE_FINISHED 
                        : GameMessage.Type.NUMBER_PLACED;
                notifyChallenge(msgType);
                
                return scoreChange;
            }
            
            logger.debug("Version conflict in placeNumber, retrying... (attempt {})", attempt + 1);
        }
        
        logger.warn("Failed to place number after retries due to conflicts");
        return 0;
    }

    public void refreshCurrentChallenge() {
        if (currentChallenge == null) return;
        
        Number160 key = DHTOperations.createChallengeKey(currentChallenge.getName());
        GameSession challenge = DHTOperations.get(peer, key, GameSession.class);
        if (challenge != null) {
            currentChallenge = challenge;
        }
    }

    @SuppressWarnings("unchecked")
    public void refreshPublicChallenges() {
        Set<String> challenges = DHTOperations.get(peer, DHTOperations.PUBLIC_CHALLENGES_KEY, HashSet.class);
        publicChallenges = challenges != null ? challenges : new HashSet<>();
    }

    private void addToPublicChallenges(String name) {
        refreshPublicChallenges();
        publicChallenges.add(name);
        DHTOperations.put(peer, DHTOperations.PUBLIC_CHALLENGES_KEY, (java.io.Serializable) publicChallenges);
        notifyAllPlayers(new GameMessage(GameMessage.Type.PUBLIC_CHALLENGES_UPDATED, name, currentPlayer.getNickname()));
    }

    private void removeFromPublicChallenges(String name) {
        refreshPublicChallenges();
        publicChallenges.remove(name);
        DHTOperations.put(peer, DHTOperations.PUBLIC_CHALLENGES_KEY, (java.io.Serializable) publicChallenges);
        notifyAllPlayers(new GameMessage(GameMessage.Type.PUBLIC_CHALLENGES_UPDATED, name, currentPlayer.getNickname()));
    }

    private void notifyChallenge(GameMessage.Type type) {
        if (currentChallenge == null || currentPlayer == null) return;
        
        GameMessage msg = new GameMessage(type, currentChallenge.getName(), currentPlayer.getNickname());
        
        for (String participant : currentChallenge.getParticipants()) {
            if (!participant.equals(currentPlayer.getNickname())) {
                sendToPlayer(participant, msg);
            }
        }
    }

    private void notifyAllPlayers(GameMessage msg) {
        for (PlayerInfo player : getLoggedPlayers()) {
            if (!player.getNickname().equals(currentPlayer.getNickname())) {
                sendToPlayer(player, msg);
            }
        }
    }

    private void sendToPlayer(String nickname, GameMessage msg) {
        for (PlayerInfo player : getLoggedPlayers()) {
            if (player.getNickname().equals(nickname)) {
                sendToPlayer(player, msg);
                return;
            }
        }
    }

    private void sendToPlayer(PlayerInfo player, GameMessage msg) {
        try {
            PeerAddress address = new PeerAddress(Number160.createHash(player.getNickname()), 
                    player.getAddress(), player.getPort(), player.getPort());
            FutureDirect future = peer.peer().sendDirect(address).object(msg).start();
            future.awaitUninterruptibly(5000);
        } catch (Exception e) {
            logger.debug("Failed to send message to {}: {}", player.getNickname(), e.getMessage());
        }
    }

    public void shutdown() {
        logout();
        peer.shutdown();
        logger.info("Client shutdown");
    }

    public PlayerInfo getCurrentPlayer() { return currentPlayer; }
    public GameSession getCurrentChallenge() { return currentChallenge; }
    public Set<String> getPublicChallenges() { return new HashSet<>(publicChallenges); }
    
    public GameBoard getPlayerBoard() {
        if (currentChallenge == null || currentPlayer == null) return null;
        return currentChallenge.getPlayerBoard(currentPlayer.getNickname());
    }
    
    public int getPlayerScore() {
        if (currentChallenge == null || currentPlayer == null) return 0;
        return currentChallenge.getScore(currentPlayer.getNickname());
    }

    public boolean isLoggedIn() {
        return currentPlayer != null;
    }

    public boolean isInChallenge() {
        return currentChallenge != null;
    }

    public boolean isChallengeOwner() {
        return currentChallenge != null && currentPlayer != null && 
               currentChallenge.isOwner(currentPlayer.getNickname());
    }
}
