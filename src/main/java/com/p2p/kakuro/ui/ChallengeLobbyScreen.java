package com.p2p.kakuro.ui;

import com.p2p.kakuro.game.PuzzleGenerator;
import com.p2p.kakuro.network.P2PNetworkManager;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class ChallengeLobbyScreen extends JPanel {
    
    private final KakuroMainWindow mainFrame;
    private final P2PNetworkManager client;
    
    private JList<String> challengeList;
    private DefaultListModel<String> listModel;
    private JLabel playerLabel;
    private JButton refreshButton;
    private JButton joinButton;
    private JButton createPublicButton;
    private JButton createPrivateButton;
    private JButton joinPrivateButton;
    private JButton logoutButton;
    private JComboBox<PuzzleGenerator.Difficulty> difficultyCombo;

    public ChallengeLobbyScreen(KakuroMainWindow mainFrame, P2PNetworkManager client) {
        this.mainFrame = mainFrame;
        this.client = client;
        
        initComponents();
    }

    private void initComponents() {
        setLayout(new BorderLayout(15, 15));
        setBackground(new Color(15, 15, 25));
        setBorder(new EmptyBorder(25, 25, 25, 25));
        
        JPanel headerPanel = createHeaderPanel();
        add(headerPanel, BorderLayout.NORTH);
        
        JPanel listPanel = createListPanel();
        add(listPanel, BorderLayout.CENTER);
        
        JPanel actionsPanel = createActionsPanel();
        add(actionsPanel, BorderLayout.SOUTH);
    }

    private JPanel createHeaderPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);
        
        JLabel titleLabel = new JLabel("LOBBY");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 32));
        titleLabel.setForeground(new Color(120, 200, 255));
        
        playerLabel = new JLabel("Giocatore: ---");
        playerLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        playerLabel.setForeground(new Color(180, 180, 200));
        
        logoutButton = createStyledButton("Esci", new Color(200, 80, 80), new Color(230, 100, 100));
        logoutButton.addActionListener(e -> {
            client.logout();
            mainFrame.showPanel(KakuroMainWindow.LOGIN_PANEL);
        });
        
        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0));
        rightPanel.setOpaque(false);
        rightPanel.add(playerLabel);
        rightPanel.add(logoutButton);
        
        panel.add(titleLabel, BorderLayout.WEST);
        panel.add(rightPanel, BorderLayout.EAST);
        
        return panel;
    }

    private JPanel createListPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 15)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setColor(new Color(30, 30, 45));
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);
            }
        };
        panel.setOpaque(false);
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));
        
        JLabel listTitle = new JLabel("Sfide Pubbliche");
        listTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
        listTitle.setForeground(Color.WHITE);
        
        refreshButton = createStyledButton("Aggiorna", new Color(70, 140, 200), new Color(90, 160, 220));
        refreshButton.addActionListener(e -> refresh());
        
        JPanel titlePanel = new JPanel(new BorderLayout());
        titlePanel.setOpaque(false);
        titlePanel.add(listTitle, BorderLayout.WEST);
        titlePanel.add(refreshButton, BorderLayout.EAST);
        
        listModel = new DefaultListModel<>();
        challengeList = new JList<>(listModel);
        challengeList.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        challengeList.setBackground(new Color(25, 25, 40));
        challengeList.setForeground(Color.WHITE);
        challengeList.setSelectionBackground(new Color(80, 150, 220));
        challengeList.setSelectionForeground(Color.WHITE);
        challengeList.setFixedCellHeight(45);
        challengeList.setBorder(new EmptyBorder(5, 10, 5, 10));
        
        JScrollPane scrollPane = new JScrollPane(challengeList);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(50, 50, 70), 2));
        scrollPane.getViewport().setBackground(new Color(25, 25, 40));
        
        panel.add(titlePanel, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);
        
        return panel;
    }

    private JPanel createActionsPanel() {
        JPanel panel = new JPanel(new GridLayout(2, 1, 15, 15));
        panel.setOpaque(false);
        
        JPanel joinPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setColor(new Color(30, 30, 45));
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 15, 15);
            }
        };
        joinPanel.setOpaque(false);
        joinPanel.setBorder(new EmptyBorder(5, 10, 5, 10));
        
        joinButton = createStyledButton("Unisciti", new Color(70, 170, 90), new Color(90, 190, 110));
        joinButton.addActionListener(e -> joinSelectedChallenge());
        
        joinPrivateButton = createStyledButton("Sfida Privata", new Color(140, 100, 180), new Color(160, 120, 200));
        joinPrivateButton.addActionListener(e -> joinPrivateChallenge());
        
        joinPanel.add(joinButton);
        joinPanel.add(joinPrivateButton);
        
        JPanel createPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setColor(new Color(30, 30, 45));
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 15, 15);
            }
        };
        createPanel.setOpaque(false);
        createPanel.setBorder(new EmptyBorder(5, 10, 5, 10));
        
        JLabel diffLabel = new JLabel("Difficolta:");
        diffLabel.setForeground(Color.WHITE);
        diffLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        
        difficultyCombo = new JComboBox<>(PuzzleGenerator.Difficulty.values());
        difficultyCombo.setSelectedItem(PuzzleGenerator.Difficulty.MEDIUM);
        difficultyCombo.setBackground(new Color(50, 50, 70));
        difficultyCombo.setForeground(Color.WHITE);
        difficultyCombo.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        
        createPublicButton = createStyledButton("+ Crea Pubblica", new Color(80, 150, 220), new Color(100, 170, 240));
        createPublicButton.addActionListener(e -> createChallenge(true));
        
        createPrivateButton = createStyledButton("+ Crea Privata", new Color(200, 140, 80), new Color(220, 160, 100));
        createPrivateButton.addActionListener(e -> createChallenge(false));
        
        createPanel.add(diffLabel);
        createPanel.add(difficultyCombo);
        createPanel.add(createPublicButton);
        createPanel.add(createPrivateButton);
        
        panel.add(joinPanel);
        panel.add(createPanel);
        
        return panel;
    }

    private JButton createStyledButton(String text, Color bgColor, Color hoverColor) {
        JButton button = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                if (getModel().isPressed()) {
                    g2d.setColor(bgColor.darker());
                } else if (getModel().isRollover()) {
                    g2d.setColor(hoverColor);
                } else {
                    g2d.setColor(bgColor);
                }
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                
                g2d.setColor(Color.WHITE);
                g2d.setFont(getFont());
                FontMetrics fm = g2d.getFontMetrics();
                int x = (getWidth() - fm.stringWidth(getText())) / 2;
                int y = (getHeight() + fm.getAscent() - fm.getDescent()) / 2;
                g2d.drawString(getText(), x, y);
            }
        };
        button.setFont(new Font("Segoe UI", Font.BOLD, 13));
        button.setForeground(Color.WHITE);
        button.setPreferredSize(new Dimension(150, 40));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setContentAreaFilled(false);
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        
        return button;
    }

    public void refresh() {
        client.refreshPublicChallenges();
        
        if (client.getCurrentPlayer() != null) {
            playerLabel.setText("Giocatore: " + client.getCurrentPlayer().getNickname());
        }
        
        listModel.clear();
        for (String challenge : client.getPublicChallenges()) {
            listModel.addElement("  > " + challenge);
        }
        
        if (listModel.isEmpty()) {
            listModel.addElement("  (Nessuna sfida pubblica disponibile)");
        }
    }

    private void joinSelectedChallenge() {
        String selected = challengeList.getSelectedValue();
        if (selected == null || selected.contains("Nessuna")) {
            mainFrame.showError("Seleziona una sfida");
            return;
        }
        
        String challengeName = selected.replace("  > ", "").trim();
        
        if (client.joinChallenge(challengeName)) {
            mainFrame.showPanel(KakuroMainWindow.GAME_PANEL);
        } else {
            mainFrame.showError("Impossibile unirsi alla sfida");
        }
    }

    private void joinPrivateChallenge() {
        String name = JOptionPane.showInputDialog(this, 
            "Inserisci nome sfida privata:", "Unisciti a Sfida Privata", JOptionPane.PLAIN_MESSAGE);
        
        if (name != null && !name.trim().isEmpty()) {
            if (client.joinChallenge(name.trim())) {
                mainFrame.showPanel(KakuroMainWindow.GAME_PANEL);
            } else {
                mainFrame.showError("Sfida non trovata o non accessibile");
            }
        }
    }

    private void createChallenge(boolean isPublic) {
        String name = JOptionPane.showInputDialog(this, 
            "Inserisci nome sfida:", "Crea Sfida", JOptionPane.PLAIN_MESSAGE);
        
        if (name != null && !name.trim().isEmpty()) {
            PuzzleGenerator.Difficulty diff = (PuzzleGenerator.Difficulty) difficultyCombo.getSelectedItem();
            
            if (client.createChallenge(name.trim(), isPublic, diff)) {
                mainFrame.showPanel(KakuroMainWindow.GAME_PANEL);
            } else {
                mainFrame.showError("Impossibile creare sfida. Nome già in uso.");
            }
        }
    }
}
