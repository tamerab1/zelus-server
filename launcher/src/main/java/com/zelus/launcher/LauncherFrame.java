package com.zelus.launcher;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import javax.imageio.ImageIO;

/**
 * The main launcher window with background image support.
 */
public final class LauncherFrame extends JFrame {

    private static final Color GOLD      = new Color(0xD4, 0xAF, 0x37);
    private static final Color TEXT_DIM  = new Color(0xAA, 0xAA, 0xAA);
    private static final Color ERROR_RED = new Color(0xEF, 0x44, 0x44);
    private static final Color BG_BOTTOM = new Color(0x0A, 0x08, 0x05, 220);

    private final JLabel       statusLabel;
    private final JProgressBar progressBar;
    private final JButton      playButton;
    private final JLabel       errorLabel;

    public LauncherFrame() {
        super("Zelus Launcher");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setResizable(false);

        // ── Load background image ─────────────────────────────────────────────
        BufferedImage bgImage = null;
        try {
            var stream = getClass().getResourceAsStream("/background.jpg");
            if (stream != null) bgImage = ImageIO.read(stream);
        } catch (IOException ignored) {}
        final BufferedImage bg = bgImage;

        // ── Banner panel with background image ────────────────────────────────
        JPanel banner = new JPanel(new GridBagLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                if (bg != null) {
                    g.drawImage(bg, 0, 0, getWidth(), getHeight(), this);
                } else {
                    g.setColor(new Color(0x0D, 0x0D, 0x0D));
                    g.fillRect(0, 0, getWidth(), getHeight());
                }
            }
        };
        banner.setOpaque(false);
        banner.setPreferredSize(new Dimension(800, 420));

        // ── Bottom panel ──────────────────────────────────────────────────────
        JPanel bottom = new JPanel(new BorderLayout(0, 8)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setColor(BG_BOTTOM);
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.dispose();
            }
        };
        bottom.setOpaque(false);
        bottom.setBorder(new EmptyBorder(14, 24, 18, 24));

        // Status label
        statusLabel = new JLabel("Initializing...");
        statusLabel.setForeground(TEXT_DIM);
        statusLabel.setFont(new Font("Dialog", Font.PLAIN, 11));

        // Progress bar
        progressBar = new JProgressBar(0, 100);
        progressBar.setStringPainted(false);
        progressBar.setForeground(GOLD);
        progressBar.setBackground(new Color(0x1E, 0x1A, 0x12));
        progressBar.setBorderPainted(false);
        progressBar.setPreferredSize(new Dimension(0, 5));

        // Error label
        errorLabel = new JLabel(" ");
        errorLabel.setForeground(ERROR_RED);
        errorLabel.setFont(new Font("Dialog", Font.PLAIN, 11));
        errorLabel.setVisible(false);

        // Play button
        playButton = new JButton("PLAY");
        playButton.setFont(new Font("Dialog", Font.BOLD, 13));
        playButton.setForeground(Color.BLACK);
        playButton.setBackground(GOLD);
        playButton.setFocusPainted(false);
        playButton.setBorderPainted(false);
        playButton.setPreferredSize(new Dimension(120, 34));
        playButton.setVisible(false);
        playButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        JPanel statusRow = new JPanel(new BorderLayout());
        statusRow.setOpaque(false);
        statusRow.add(statusLabel, BorderLayout.WEST);

        JPanel progressRow = new JPanel(new BorderLayout(0, 6));
        progressRow.setOpaque(false);
        progressRow.add(progressBar, BorderLayout.CENTER);
        progressRow.add(errorLabel,  BorderLayout.SOUTH);

        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        btnRow.setOpaque(false);
        btnRow.add(playButton);

        bottom.add(statusRow,   BorderLayout.NORTH);
        bottom.add(progressRow, BorderLayout.CENTER);
        bottom.add(btnRow,      BorderLayout.SOUTH);

        // ── Assemble ──────────────────────────────────────────────────────────
        JPanel root = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                if (bg != null) {
                    g.drawImage(bg, 0, 0, getWidth(), getHeight(), this);
                } else {
                    g.setColor(new Color(0x0D, 0x0D, 0x0D));
                    g.fillRect(0, 0, getWidth(), getHeight());
                }
            }
        };
        root.setOpaque(false);
        root.add(banner, BorderLayout.CENTER);
        root.add(bottom, BorderLayout.SOUTH);

        setContentPane(root);
        pack();
        setSize(800, 500);
        setLocationRelativeTo(null);
    }

    public void setStatus(String text) {
        SwingUtilities.invokeLater(() -> statusLabel.setText(text));
    }

    public void setProgress(int pct) {
        SwingUtilities.invokeLater(() -> progressBar.setValue(Math.max(0, Math.min(100, pct))));
    }

    public void showError(String message) {
        SwingUtilities.invokeLater(() -> {
            errorLabel.setText(message);
            errorLabel.setVisible(true);
            statusLabel.setText("Update failed.");

            int choice = JOptionPane.showOptionDialog(
                this,
                "An error occurred:\n\n" + message + "\n\nDo you want to retry?",
                "Launcher Error",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.ERROR_MESSAGE,
                null,
                new String[]{"Retry", "Exit"},
                "Retry"
            );
            if (choice == JOptionPane.NO_OPTION) {
                System.exit(1);
            } else {
                System.exit(0);
            }
        });
    }

    public void showPlayButton(Runnable onPlay) {
        SwingUtilities.invokeLater(() -> {
            progressBar.setValue(100);
            statusLabel.setText("Ready to play!");
            playButton.setVisible(true);
            for (ActionListener l : playButton.getActionListeners()) {
                playButton.removeActionListener(l);
            }
            playButton.addActionListener(e -> onPlay.run());
        });
    }
}
