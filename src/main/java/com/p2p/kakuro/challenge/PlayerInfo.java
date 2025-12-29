package com.p2p.kakuro.challenge;

import java.io.Serializable;
import java.net.InetAddress;
import java.util.Objects;

public class PlayerInfo implements Serializable {
    private static final long serialVersionUID = 1L;

    private final String nickname;
    private final InetAddress address;
    private final int port;

    public PlayerInfo(String nickname, InetAddress address, int port) {
        if (nickname == null || nickname.trim().isEmpty()) {
            throw new IllegalArgumentException("Nickname cannot be null or empty");
        }
        this.nickname = nickname.trim();
        this.address = address;
        this.port = port;
    }

    public String getNickname() {
        return nickname;
    }

    public InetAddress getAddress() {
        return address;
    }

    public int getPort() {
        return port;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PlayerInfo player = (PlayerInfo) o;
        return nickname.equalsIgnoreCase(player.nickname);
    }

    @Override
    public int hashCode() {
        return Objects.hash(nickname.toLowerCase());
    }

    @Override
    public String toString() {
        return nickname;
    }
}
