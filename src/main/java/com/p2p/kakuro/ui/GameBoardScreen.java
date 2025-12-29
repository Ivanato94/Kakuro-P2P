package com.p2p.kakuro.ui;

import com.p2p.kakuro.challenge.GameSession;
import com.p2p.kakuro.game.GameBoard;
import com.p2p.kakuro.game.BoardCell;
import com.p2p.kakuro.network.P2PNetworkManager;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.Map;

public class GameBoardScreen extends JPanel {
    
    private final KakuroMainWindow mainFrame;
    private final P2PNetworkManager client;
    
    private JPanel boardPanel;
    private JLabel challengeNameLabel;
    private JLabel statusLabel;
    private JLabel scoreLabel;
    private JTextArea playersArea;
    private JButton startButton;
    private JButton leaveButton;
    private JButton refreshButton;
    private javax.swing.Timer autoRefreshTimer;

    public GameBoardScreen(KakuroMainWindow mainFrame, P2PNetworkManager client) {
        this.mainFrame = mainFrame;
        this.client = client;
        
        initComponents();
        startAutoRefresh();
    }
    
    private void startAutoRefresh() {
        autoRefreshTimer = new javax.swing.Timer(1000, e -> {
            if (client.isInChallenge()) {
                client.refreshCurrentChallenge();
                refresh();
            }
        });
        autoRefreshTimer.start();
    }

    private void initComponents() {
        setLayout(new BorderLayout(15, 15));
        setBackground(new Color(15, 15, 25));
        setBorder(new EmptyBorder(20, 20, 20, 20));
        
        JPanel headerPanel = createHeaderPanel();
        add(headerPanel, BorderLayout.NORTH);
        
        boardPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setColor(new Color(30, 30, 45));
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);
            }
        };
        boardPanel.setOpaque(false);
        boardPanel.setBorder(new EmptyBorder(20, 20, 20, 20));
        add(boardPanel, BorderLayout.CENTER);
        
        JPanel sidePanel = createSidePanel();
        add(sidePanel, BorderLayout.EAST);
    }

    private JPanel createHeaderPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);
        
        challengeNameLabel = new JLabel("Sfida: ---");
        challengeNameLabel.setFont(new Font("Segoe UI", Font.BOLD, 26));
        challengeNameLabel.setForeground(new Color(120, 200, 255));
        
        statusLabel = new JLabel("Stato: ---");
        statusLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        statusLabel.setForeground(new Color(180, 180, 200));
        
        JPanel leftPanel = new JPanel();
        leftPanel.setLayout(new BoxLayout(leftPanel, BoxLayout.Y_AXIS));
        leftPanel.setOpaque(false);
        leftPanel.add(challengeNameLabel);
        leftPanel.add(Box.createVerticalStrut(5));
        leftPanel.add(statusLabel);
        
        leaveButton = createStyledButton("Abbandona", new Color(200, 80, 80), new Color(220, 100, 100));
        leaveButton.addActionListener(e -> leaveChallenge());
        
        refreshButton = createStyledButton("Aggiorna", new Color(70, 140, 200), new Color(90, 160, 220));
        refreshButton.addActionListener(e -> {
            client.refreshCurrentChallenge();
            refresh();
        });
        
        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        rightPanel.setOpaque(false);
        rightPanel.add(refreshButton);
        rightPanel.add(leaveButton);
        
        panel.add(leftPanel, BorderLayout.WEST);
        panel.add(rightPanel, BorderLayout.EAST);
        
        return panel;
    }

    private JPanel createSidePanel() {
        JPanel panel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setColor(new Color(30, 30, 45));
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);
            }
        };
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setOpaque(false);
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));
        panel.setPreferredSize(new Dimension(220, 0));
        
        scoreLabel = new JLabel("Punteggio: 0");
        scoreLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        scoreLabel.setForeground(new Color(100, 220, 120));
        scoreLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        JLabel playersTitle = new JLabel("Giocatori");
        playersTitle.setFont(new Font("Segoe UI", Font.BOLD, 16));
        playersTitle.setForeground(Color.WHITE);
        playersTitle.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        playersArea = new JTextArea();
        playersArea.setEditable(false);
        playersArea.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        playersArea.setBackground(new Color(25, 25, 40));
        playersArea.setForeground(new Color(200, 200, 220));
        playersArea.setBorder(new EmptyBorder(10, 10, 10, 10));
        
        JScrollPane scrollPane = new JScrollPane(playersArea);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(50, 50, 70), 2));
        scrollPane.setPreferredSize(new Dimension(180, 120));
        scrollPane.setMaximumSize(new Dimension(180, 120));
        
        startButton = createStyledButton("AVVIA PARTITA", new Color(70, 180, 100), new Color(90, 200, 120));
        startButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        startButton.setMaximumSize(new Dimension(180, 45));
        startButton.addActionListener(e -> startChallenge());
        
        JLabel rulesTitle = new JLabel("Regole");
        rulesTitle.setFont(new Font("Segoe UI", Font.BOLD, 14));
        rulesTitle.setForeground(new Color(150, 150, 180));
        rulesTitle.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        JTextArea rulesArea = new JTextArea();
        rulesArea.setText("+1 : Primo a trovare\n 0 : Già trovato/Sbagliato");
        rulesArea.setEditable(false);
        rulesArea.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        rulesArea.setBackground(new Color(30, 30, 45));
        rulesArea.setForeground(new Color(150, 150, 180));
        rulesArea.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        panel.add(scoreLabel);
        panel.add(Box.createVerticalStrut(25));
        panel.add(playersTitle);
        panel.add(Box.createVerticalStrut(10));
        panel.add(scrollPane);
        panel.add(Box.createVerticalStrut(25));
        panel.add(startButton);
        panel.add(Box.createVerticalStrut(30));
        panel.add(rulesTitle);
        panel.add(Box.createVerticalStrut(8));
        panel.add(rulesArea);
        panel.add(Box.createVerticalGlue());
        
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
        button.setPreferredSize(new Dimension(130, 38));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setContentAreaFilled(false);
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        
        return button;
    }

    public void refresh() {
        GameSession challenge = client.getCurrentChallenge();
        if (challenge == null) {
            return;
        }
        
        challengeNameLabel.setText("Sfida: " + challenge.getName());
        String ownerText = challenge.isOwner(client.getCurrentPlayer().getNickname()) ? " [Host]" : "";
        statusLabel.setText("Stato: " + translateStatus(challenge.getStatus()) + ownerText);
        
        scoreLabel.setText("Punteggio: " + client.getPlayerScore());
        
        StringBuilder sb = new StringBuilder();
        Map<String, Integer> scores = challenge.getScores();
        for (Map.Entry<String, Integer> entry : scores.entrySet()) {
            String marker = entry.getKey().equals(challenge.getOwnerNickname()) ? " [Host]" : "";
            sb.append(entry.getKey()).append(marker).append(": ").append(entry.getValue()).append("\n");
        }
        playersArea.setText(sb.toString());
        
        boolean isOwner = challenge.isOwner(client.getCurrentPlayer().getNickname());
        boolean isWaiting = challenge.getStatus() == GameSession.Status.WAITING;
        startButton.setVisible(isOwner && isWaiting);
        startButton.setEnabled(challenge.getParticipantCount() >= 2);
        
        if (isOwner && isWaiting && challenge.getParticipantCount() < 2) {
            startButton.setText("Attendi giocatori...");
        } else {
            startButton.setText("AVVIA PARTITA");
        }
        
        updateBoard(challenge);
    }

    private void updateBoard(GameSession challenge) {
        boardPanel.removeAll();
        
        if (challenge.getStatus() == GameSession.Status.WAITING) {
            boardPanel.setLayout(new GridBagLayout());
            JLabel waitLabel = new JLabel("In attesa dell'avvio...");
            waitLabel.setFont(new Font("Segoe UI", Font.ITALIC, 20));
            waitLabel.setForeground(new Color(150, 150, 180));
            boardPanel.add(waitLabel);
        } else if (challenge.getStatus() == GameSession.Status.FINISHED) {
            boardPanel.setLayout(new GridBagLayout());
            JPanel finishPanel = new JPanel();
            finishPanel.setLayout(new BoxLayout(finishPanel, BoxLayout.Y_AXIS));
            finishPanel.setOpaque(false);
            
            JLabel winLabel = new JLabel("PARTITA TERMINATA!");
            winLabel.setFont(new Font("Segoe UI", Font.BOLD, 28));
            winLabel.setForeground(new Color(255, 215, 0));
            winLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            
            JLabel winnerLabel = new JLabel("Vincitore: " + challenge.getWinner());
            winnerLabel.setFont(new Font("Segoe UI", Font.BOLD, 22));
            winnerLabel.setForeground(new Color(100, 220, 120));
            winnerLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            
            finishPanel.add(winLabel);
            finishPanel.add(Box.createVerticalStrut(15));
            finishPanel.add(winnerLabel);
            boardPanel.add(finishPanel);
        } else {
            GameBoard board = client.getPlayerBoard();
            if (board != null) {
                int rows = board.getRows();
                int cols = board.getCols();
                
                boardPanel.setLayout(new GridLayout(rows, cols, 2, 2));
                
                for (int i = 0; i < rows; i++) {
                    for (int j = 0; j < cols; j++) {
                        BoardCell cell = board.getCell(i, j);
                        JComponent cellComponent = createCellComponent(cell, i, j);
                        boardPanel.add(cellComponent);
                    }
                }
            }
        }
        
        boardPanel.revalidate();
        boardPanel.repaint();
    }

    private JComponent createCellComponent(BoardCell cell, int row, int col) {
        if (cell.isBlocked()) {
            JPanel blocked = new JPanel() {
                @Override
                protected void paintComponent(Graphics g) {
                    super.paintComponent(g);
                    g.setColor(Color.BLACK);
                    g.fillRect(0, 0, getWidth(), getHeight());
                }
            };
            blocked.setPreferredSize(new Dimension(50, 50));
            return blocked;
        } else if (cell.isClue()) {
            return new CluePanel(cell.getVerticalClue(), cell.getHorizontalClue());
        } else if (cell.isPlayable()) {
            JButton btn = new JButton();
            btn.setFont(new Font("Arial", Font.BOLD, 22));
            btn.setPreferredSize(new Dimension(50, 50));
            btn.setMargin(new Insets(0, 0, 0, 0));
            btn.setFocusPainted(false);
            btn.setBorder(BorderFactory.createLineBorder(Color.GRAY, 1));
            
            if (cell.getValue() > 0) {
                btn.setText(String.valueOf(cell.getValue()));
                btn.setBackground(Color.WHITE);
                btn.setForeground(Color.BLACK);
                btn.setEnabled(false);
            } else {
                btn.setText("");
                btn.setBackground(Color.WHITE);
                btn.setForeground(Color.BLACK);
                
                final int r = row;
                final int c = col;
                btn.addActionListener(e -> {
                    String input = JOptionPane.showInputDialog(GameBoardScreen.this,
                        "Inserisci numero (1-9):", "Numero", JOptionPane.PLAIN_MESSAGE);
                    if (input != null && input.matches("[1-9]")) {
                        placeNumberAt(r, c, Integer.parseInt(input));
                    }
                });
            }
            return btn;
        }
        
        JPanel empty = new JPanel();
        empty.setBackground(Color.BLACK);
        empty.setPreferredSize(new Dimension(50, 50));
        return empty;
    }
    
    private class CluePanel extends JPanel {
        private final int verticalClue;
        private final int horizontalClue;
        
        public CluePanel(int verticalClue, int horizontalClue) {
            this.verticalClue = verticalClue;
            this.horizontalClue = horizontalClue;
            setPreferredSize(new Dimension(50, 50));
        }
        
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2d = (Graphics2D) g;
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            
            int w = getWidth();
            int h = getHeight();
            
            g2d.setColor(Color.BLACK);
            g2d.fillRect(0, 0, w, h);
            
            g2d.setColor(new Color(180, 180, 180));
            g2d.setStroke(new BasicStroke(1.5f));
            g2d.drawLine(0, 0, w, h);
            
            g2d.setColor(Color.WHITE);
            Font numFont = new Font("Arial", Font.BOLD, 12);
            g2d.setFont(numFont);
            FontMetrics fm = g2d.getFontMetrics();
            
            if (horizontalClue > 0) {
                String hText = String.valueOf(horizontalClue);
                int textWidth = fm.stringWidth(hText);
                int textHeight = fm.getAscent();
                int hx = w / 2 + (w / 4) - (textWidth / 2);
                int hy = h / 4 + (textHeight / 2);
                g2d.drawString(hText, hx, hy);
            }
            
            if (verticalClue > 0) {
                String vText = String.valueOf(verticalClue);
                int textWidth = fm.stringWidth(vText);
                int textHeight = fm.getAscent();
                int vx = w / 4 - (textWidth / 2);
                int vy = h / 2 + h / 4 + (textHeight / 2);
                g2d.drawString(vText, vx, vy);
            }
            
            g2d.setColor(new Color(100, 100, 100));
            g2d.setStroke(new BasicStroke(1));
            g2d.drawRect(0, 0, w - 1, h - 1);
        }
    }

    private void placeNumberAt(int row, int col, int number) {
        GameSession challenge = client.getCurrentChallenge();
        if (challenge == null || challenge.getStatus() != GameSession.Status.RUNNING) {
            mainFrame.showError("Partita non in corso");
            return;
        }
        
        int result = client.placeNumber(row, col, number);
        
        String message;
        if (result > 0) {
            message = "Corretto! +1 punto";
        } else {
            message = "Gia trovato o sbagliato";
        }
        
        client.refreshCurrentChallenge();
        refresh();
        
        if (client.getCurrentChallenge().getStatus() == GameSession.Status.FINISHED) {
            String winner = client.getCurrentChallenge().getWinner();
            mainFrame.showInfo("PARTITA TERMINATA!\n\nVincitore: " + winner + "\nIl tuo punteggio: " + client.getPlayerScore());
        } else {
            statusLabel.setText(message);
        }
    }

    private void startChallenge() {
        if (client.startChallenge()) {
            refresh();
        } else {
            mainFrame.showError("Impossibile avviare. Servono almeno 2 giocatori.");
        }
    }

    private void leaveChallenge() {
        int confirm = JOptionPane.showConfirmDialog(this,
            "Sei sicuro di voler abbandonare?\nIl tuo punteggio andra perso.",
            "Abbandona Sfida", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        
        if (confirm == JOptionPane.YES_OPTION) {
            client.leaveChallenge();
            mainFrame.showPanel(KakuroMainWindow.LOBBY_PANEL);
        }
    }
    
    private String translateStatus(GameSession.Status status) {
        switch (status) {
            case WAITING: return "In Attesa";
            case RUNNING: return "In Corso";
            case FINISHED: return "Terminata";
            default: return status.toString();
        }
    }
}
