package com.p2p.kakuro.ui;

import com.p2p.kakuro.network.P2PNetworkManager;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class PlayerLoginScreen extends JPanel {
    
    private final KakuroMainWindow mainFrame;
    private final P2PNetworkManager client;
    
    private JTextField nicknameField;
    private JButton loginButton;
    private JLabel statusLabel;

    public PlayerLoginScreen(KakuroMainWindow mainFrame, P2PNetworkManager client) {
        this.mainFrame = mainFrame;
        this.client = client;
        
        initComponents();
    }

    private void initComponents() {
        setLayout(new GridBagLayout());
        setBackground(new Color(15, 15, 25));
        
        JPanel centerPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                GradientPaint gradient = new GradientPaint(0, 0, new Color(35, 35, 55), 
                                                           0, getHeight(), new Color(25, 25, 40));
                g2d.setPaint(gradient);
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 30, 30);
                
                g2d.setColor(new Color(100, 150, 255, 100));
                g2d.setStroke(new BasicStroke(2));
                g2d.drawRoundRect(1, 1, getWidth()-3, getHeight()-3, 30, 30);
            }
        };
        centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));
        centerPanel.setOpaque(false);
        centerPanel.setBorder(new EmptyBorder(50, 60, 50, 60));
        
        JLabel titleLabel = new JLabel("KAKURO");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 42));
        titleLabel.setForeground(new Color(120, 200, 255));
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        JSeparator separator = new JSeparator();
        separator.setMaximumSize(new Dimension(200, 2));
        separator.setForeground(new Color(80, 80, 120));
        
        JLabel nicknameLabel = new JLabel("Inserisci il tuo nickname");
        nicknameLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        nicknameLabel.setForeground(new Color(200, 200, 220));
        nicknameLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        nicknameField = new JTextField(20) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setColor(getBackground());
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 15, 15);
                super.paintComponent(g);
            }
        };
        nicknameField.setMaximumSize(new Dimension(280, 45));
        nicknameField.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        nicknameField.setHorizontalAlignment(JTextField.CENTER);
        nicknameField.setBackground(new Color(50, 50, 70));
        nicknameField.setForeground(Color.WHITE);
        nicknameField.setCaretColor(new Color(120, 200, 255));
        nicknameField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(80, 80, 120), 2),
            new EmptyBorder(10, 15, 10, 15)
        ));
        nicknameField.setOpaque(false);
        
        loginButton = new JButton("ACCEDI") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                if (getModel().isPressed()) {
                    g2d.setColor(new Color(60, 140, 200));
                } else if (getModel().isRollover()) {
                    g2d.setColor(new Color(100, 180, 240));
                } else {
                    GradientPaint gradient = new GradientPaint(0, 0, new Color(80, 160, 230), 
                                                               0, getHeight(), new Color(60, 130, 200));
                    g2d.setPaint(gradient);
                }
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);
                
                g2d.setColor(Color.WHITE);
                g2d.setFont(getFont());
                FontMetrics fm = g2d.getFontMetrics();
                int x = (getWidth() - fm.stringWidth(getText())) / 2;
                int y = (getHeight() + fm.getAscent() - fm.getDescent()) / 2;
                g2d.drawString(getText(), x, y);
            }
        };
        loginButton.setFont(new Font("Segoe UI", Font.BOLD, 16));
        loginButton.setForeground(Color.WHITE);
        loginButton.setMaximumSize(new Dimension(280, 50));
        loginButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        loginButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        loginButton.setContentAreaFilled(false);
        loginButton.setBorderPainted(false);
        loginButton.setFocusPainted(false);
        
        statusLabel = new JLabel(" ");
        statusLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        statusLabel.setForeground(new Color(255, 120, 120));
        statusLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        loginButton.addActionListener(e -> doLogin());
        nicknameField.addActionListener(e -> doLogin());
        
        centerPanel.add(titleLabel);
        centerPanel.add(Box.createVerticalStrut(5));
        centerPanel.add(Box.createVerticalStrut(30));
        centerPanel.add(separator);
        centerPanel.add(Box.createVerticalStrut(30));
        centerPanel.add(nicknameLabel);
        centerPanel.add(Box.createVerticalStrut(15));
        centerPanel.add(nicknameField);
        centerPanel.add(Box.createVerticalStrut(25));
        centerPanel.add(loginButton);
        centerPanel.add(Box.createVerticalStrut(15));
        centerPanel.add(statusLabel);
        
        add(centerPanel);
    }

    private void doLogin() {
        String nickname = nicknameField.getText().trim();
        
        if (nickname.isEmpty()) {
            statusLabel.setText("Inserisci un nickname");
            return;
        }
        
        if (nickname.length() < 3 || nickname.length() > 15) {
            statusLabel.setText("Il nickname deve essere 3-15 caratteri");
            return;
        }
        
        loginButton.setEnabled(false);
        statusLabel.setText("Connessione...");
        statusLabel.setForeground(new Color(255, 200, 100));
        
        SwingWorker<Boolean, Void> worker = new SwingWorker<>() {
            @Override
            protected Boolean doInBackground() throws Exception {
                return client.login(nickname);
            }
            
            @Override
            protected void done() {
                try {
                    if (get()) {
                        statusLabel.setText(" ");
                        mainFrame.showPanel(KakuroMainWindow.LOBBY_PANEL);
                    } else {
                        statusLabel.setText("Nickname già in uso");
                        statusLabel.setForeground(new Color(255, 120, 120));
                    }
                } catch (Exception e) {
                    statusLabel.setText("Connessione fallita: " + e.getMessage());
                    statusLabel.setForeground(new Color(255, 120, 120));
                }
                loginButton.setEnabled(true);
            }
        };
        
        worker.execute();
    }
}
