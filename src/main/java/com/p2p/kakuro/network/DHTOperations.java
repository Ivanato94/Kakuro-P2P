package com.p2p.kakuro.network;

import com.p2p.kakuro.challenge.GameSession;
import net.tomp2p.dht.FutureGet;
import net.tomp2p.dht.FuturePut;
import net.tomp2p.dht.PeerDHT;
import net.tomp2p.peers.Number160;
import net.tomp2p.storage.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.Serializable;

public class DHTOperations {
    private static final Logger logger = LoggerFactory.getLogger(DHTOperations.class);
    
    public static final Number160 LOGGED_PLAYERS_KEY = Number160.createHash("_logged_players_");
    public static final Number160 PUBLIC_CHALLENGES_KEY = Number160.createHash("_public_challenges_");
    
    private static final long TIMEOUT_MS = 10000;
    private static final int MAX_RETRIES = 5;
    private static final int VERSION_CONFLICT_RETRIES = 3;

    public static <T extends Serializable> boolean put(PeerDHT peer, Number160 key, T value) {
        for (int attempt = 0; attempt < MAX_RETRIES; attempt++) {
            try {
                Data data = new Data(value);
                FuturePut futurePut = peer.put(key).data(data).start();
                futurePut.awaitUninterruptibly(TIMEOUT_MS);
                
                if (futurePut.isSuccess()) {
                    logger.debug("Put success for key: {}", key);
                    return true;
                }
                logger.warn("Put failed (attempt {}): {}", attempt + 1, futurePut.failedReason());
            } catch (IOException e) {
                logger.error("Serialization error: {}", e.getMessage());
                return false;
            }
        }
        return false;
    }

    public static GameSession putWithVersion(PeerDHT peer, Number160 key, GameSession localSession) {
        for (int attempt = 0; attempt < VERSION_CONFLICT_RETRIES; attempt++) {
            GameSession remoteSession = get(peer, key, GameSession.class);
            
            if (remoteSession != null) {
                if (remoteSession.getVersion() > localSession.getVersion()) {
                    logger.warn("Version conflict detected! Remote: {}, Local: {}. Merging...", 
                               remoteSession.getVersion(), localSession.getVersion());
                    return remoteSession;
                }
            }
            
            localSession.incrementVersion();
            
            if (put(peer, key, localSession)) {
                logger.debug("Put with version success. New version: {}", localSession.getVersion());
                return localSession;
            }
            
            logger.warn("Put with version failed (attempt {}), retrying...", attempt + 1);
            
            try {
                Thread.sleep(100 * (attempt + 1));
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
        
        logger.error("Put with version failed after {} attempts", VERSION_CONFLICT_RETRIES);
        return null;
    }

    public static <T extends Serializable> T get(PeerDHT peer, Number160 key, Class<T> type) {
        for (int attempt = 0; attempt < MAX_RETRIES; attempt++) {
            try {
                FutureGet futureGet = peer.get(key).start();
                futureGet.awaitUninterruptibly(TIMEOUT_MS);
                
                if (futureGet.isSuccess() && futureGet.data() != null) {
                    Object obj = futureGet.data().object();
                    if (type.isInstance(obj)) {
                        logger.debug("Get success for key: {}", key);
                        return type.cast(obj);
                    }
                }
            } catch (ClassNotFoundException | IOException e) {
                logger.error("Deserialization error: {}", e.getMessage());
            }
        }
        return null;
    }

    public static boolean remove(PeerDHT peer, Number160 key) {
        try {
            peer.remove(key).start().awaitUninterruptibly(TIMEOUT_MS);
            logger.debug("Removed key: {}", key);
            return true;
        } catch (Exception e) {
            logger.error("Remove error: {}", e.getMessage());
            return false;
        }
    }

    public static Number160 createChallengeKey(String challengeName) {
        return Number160.createHash("challenge_" + challengeName.toLowerCase());
    }
}
