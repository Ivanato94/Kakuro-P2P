package com.p2p.kakuro.ui;

import com.p2p.kakuro.network.P2PNetworkManager;
import com.p2p.kakuro.network.P2PMessageHandler;
import net.tomp2p.peers.PeerAddress;

import javax.swing.*;
import java.awt.*;

public class KakuroMainWindow extends JFrame implements P2PMessageHandler {
    
    private final P2PNetworkManager client;
    private final CardLayout cardLayout;
    private final JPanel mainPanel;
    
    private PlayerLoginScreen loginPanel;
    private ChallengeLobbyScreen lobbyPanel;
    private GameBoardScreen gamePanel;
    
    public static final String LOGIN_PANEL = "login";
    public static final String LOBBY_PANEL = "lobby";
    public static final String GAME_PANEL = "game";

    public KakuroMainWindow(P2PNetworkManager client) {
        this.client = client;
        this.cardLayout = new CardLayout();
        this.mainPanel = new JPanel(cardLayout);
        
        client.addListener(this);
        
        initComponents();
        setupFrame();
    }

    private void initComponents() {
        loginPanel = new PlayerLoginScreen(this, client);
        lobbyPanel = new ChallengeLobbyScreen(this, client);
        gamePanel = new GameBoardScreen(this, client);
        
        mainPanel.add(loginPanel, LOGIN_PANEL);
        mainPanel.add(lobbyPanel, LOBBY_PANEL);
        mainPanel.add(gamePanel, GAME_PANEL);
        
        add(mainPanel);
    }

    private void setupFrame() {
        setTitle("P2P Kakuro");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(900, 700);
        setMinimumSize(new Dimension(800, 600));
        setLocationRelativeTo(null);
        
        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent e) {
                client.shutdown();
            }
        });
    }

    public void showPanel(String panelName) {
        cardLayout.show(mainPanel, panelName);
        
        switch (panelName) {
            case LOBBY_PANEL:
                lobbyPanel.refresh();
                break;
            case GAME_PANEL:
                gamePanel.refresh();
                break;
        }
    }

    public void showError(String message) {
        SwingUtilities.invokeLater(() -> 
            JOptionPane.showMessageDialog(this, message, "Errore", JOptionPane.ERROR_MESSAGE));
    }

    public void showInfo(String message) {
        SwingUtilities.invokeLater(() -> 
            JOptionPane.showMessageDialog(this, message, "Informazione", JOptionPane.INFORMATION_MESSAGE));
    }

    @Override
    public void onMessageReceived(PeerAddress sender, GameMessage message) {
        SwingUtilities.invokeLater(() -> {
            switch (message.getType()) {
                case PUBLIC_CHALLENGES_UPDATED:
                    if (lobbyPanel != null) {
                        lobbyPanel.refresh();
                    }
                    break;
                case CHALLENGE_UPDATED:
                case PLAYER_JOINED:
                case PLAYER_LEFT:
                case CHALLENGE_STARTED:
                case NUMBER_PLACED:
                case CHALLENGE_FINISHED:
                    if (gamePanel != null && client.isInChallenge()) {
                        client.refreshCurrentChallenge();
                        gamePanel.refresh();
                        
                        if (message.getType() == GameMessage.Type.CHALLENGE_FINISHED) {
                            String winner = client.getCurrentChallenge().getWinner();
                            showInfo("Partita Terminata! Vincitore: " + winner);
                        }
                    }
                    break;
            }
        });
    }
}
