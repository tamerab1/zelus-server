package com.zelus.launcher;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;

/**
 * The main launcher window.
 *
 * Layout:
 *   ┌────────────────────────────────────────┐
 *   │        [ZELUS logo / banner]           │  ← banner panel (dark bg)
 *   │                                        │
 *   ├────────────────────────────────────────┤
 *   │  Status: Checking for updates...       │  ← status label
 *   │  [████████████████░░░░░░░░░░░░] 64%   │  ← progress bar
 *   │                          [PLAY ▶]      │  ← play button (hidden during update)
 *   └────────────────────────────────────────┘
 */
public final class LauncherFrame extends JFrame {

    private static final Color BG_DARK    = new Color(0x0D, 0x0D, 0x0D);
    private static final Color BG_PANEL   = new Color(0x14, 0x10, 0x0C);
    private static final Color GOLD       = new Color(0xD4, 0xAF, 0x37);
    private static final Color TEXT_DIM   = new Color(0x66, 0x66, 0x66);
    private static final Color ERROR_RED  = new Color(0xEF, 0x44, 0x44);

    private final JLabel        statusLabel;
    private final JProgressBar  progressBar;
    private final JButton       playButton;
    private final JLabel        errorLabel;

    public LauncherFrame() {
        super("Zelus Launcher");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setResizable(false);
        setBackground(BG_DARK);

        // ── Banner area ───────────────────────────────────────────────────────
        JPanel banner = new JPanel();
        banner.setBackground(BG_DARK);
        banner.setPreferredSize(new Dimension(640, 200));
        banner.setLayout(new GridBagLayout());

        JLabel title = new JLabel("ZELUS");
        title.setFont(new Font("Dialog", Font.BOLD, 52));
        title.setForeground(GOLD);
        banner.add(title);

        JLabel subtitle = new JLabel("PRIVATE SERVER");
        subtitle.setFont(new Font("Dialog", Font.PLAIN, 12));
        subtitle.setForeground(TEXT_DIM);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridy = 1;
        gbc.insets = new Insets(4, 0, 0, 0);
        banner.add(subtitle, gbc);

        // ── Bottom panel ──────────────────────────────────────────────────────
        JPanel bottom = new JPanel(new BorderLayout(0, 8));
        bottom.setBackground(BG_PANEL);
        bottom.setBorder(new EmptyBorder(16, 24, 20, 24));

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
        progressBar.setPreferredSize(new Dimension(0, 6));

        // Error label (hidden by default)
        errorLabel = new JLabel(" ");
        errorLabel.setForeground(ERROR_RED);
        errorLabel.setFont(new Font("Dialog", Font.PLAIN, 11));
        errorLabel.setVisible(false);

        // Play button (hidden during update, shown when ready)
        playButton = new JButton("PLAY");
        playButton.setFont(new Font("Dialog", Font.BOLD, 13));
        playButton.setForeground(Color.BLACK);
        playButton.setBackground(GOLD);
        playButton.setFocusPainted(false);
        playButton.setBorderPainted(false);
        playButton.setPreferredSize(new Dimension(120, 36));
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
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(BG_DARK);
        root.add(banner, BorderLayout.CENTER);
        root.add(bottom, BorderLayout.SOUTH);

        setContentPane(root);
        pack();
        setSize(640, 340);
        setLocationRelativeTo(null);  // center on screen
    }

    // ── Public API (called from UpdateManager / ZelusLauncher) ───────────────

    /** Updates the status text (call from any thread). */
    public void setStatus(String text) {
        SwingUtilities.invokeLater(() -> statusLabel.setText(text));
    }

    /** Sets the progress bar value 0–100 (call from any thread). */
    public void setProgress(int pct) {
        SwingUtilities.invokeLater(() -> progressBar.setValue(Math.clamp(pct, 0, 100)));
    }

    /**
     * Shows an error message and a retry-or-exit dialog.
     * Call from any thread.
     */
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
                // Signal retry — ZelusLauncher will detect this and restart the pipeline
                System.exit(0);   // simplest: just restart the process
            }
        });
    }

    /**
     * Reveals the PLAY button and wires up the given action.
     * Used when the client is already up-to-date and no download is needed.
     */
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
