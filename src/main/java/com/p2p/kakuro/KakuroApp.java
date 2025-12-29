package com.p2p.kakuro;

import com.formdev.flatlaf.FlatDarkLaf;
import com.p2p.kakuro.network.P2PNetworkManager;
import com.p2p.kakuro.ui.KakuroMainWindow;

import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;

public class KakuroApp {
    private static final Logger logger = LoggerFactory.getLogger(KakuroApp.class);

    @Option(name = "-ma", aliases = "--master-address", usage = "Master peer address")
    private String masterAddress = "";

    @Option(name = "-mp", aliases = "--master-port", usage = "Master peer port")
    private int masterPort = 4001;

    @Option(name = "-lp", aliases = "--local-port", usage = "Local peer port")
    private int localPort = 4001;

    public static void main(String[] args) {
        new KakuroApp().run(args);
    }

    public void run(String[] args) {
        CmdLineParser parser = new CmdLineParser(this);
        try {
            parser.parseArgument(args);
        } catch (CmdLineException e) {
            System.err.println("Error: " + e.getMessage());
            parser.printUsage(System.err);
            return;
        }

        try {
            UIManager.setLookAndFeel(new FlatDarkLaf());
        } catch (Exception e) {
            logger.warn("Could not set FlatLaf, using default L&F");
        }

        SwingUtilities.invokeLater(() -> {
            try {
                logger.info("Starting P2P Kakuro...");
                logger.info("Local port: {}, Master: {}:{}", localPort, masterAddress, masterPort);

                P2PNetworkManager client = new P2PNetworkManager(localPort, masterAddress, masterPort);
                KakuroMainWindow frame = new KakuroMainWindow(client);
                frame.setVisible(true);

                logger.info("Application started successfully");
            } catch (Exception e) {
                logger.error("Failed to start application", e);
                JOptionPane.showMessageDialog(null,
                    "Failed to start: " + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
                System.exit(1);
            }
        });
    }
}
