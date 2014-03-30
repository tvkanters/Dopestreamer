package com.dopelives.dopestreamer;

import java.awt.AWTException;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.SystemTray;
import java.awt.TrayIcon;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javafx.application.Platform;

import javax.swing.ImageIcon;

/**
 * Handles tray icon related functionality.
 */
public class TrayManager {

    /** The tray icon to show in the tray */
    private static TrayIcon sTrayIcon;

    /** Whether or not the tray icon is in the tray */
    private static boolean sInTray = false;

    /**
     * Retrieves the tray icon and constructs it if needed.
     *
     * @return The Dopestreamer tray icon
     */
    private static TrayIcon getTrayIcon() {
        if (sTrayIcon == null) {
            // Construct the tray icon
            sTrayIcon = new TrayIcon(new ImageIcon(MainWindow.class.getResource(Environment.IMAGE_FOLDER
                    + "dopestreamer_small.png")).getImage(), Environment.TITLE);

            // Open the main window upon double clicking the tray icon
            sTrayIcon.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(final ActionEvent e) {
                    Platform.runLater(new Runnable() {
                        @Override
                        public void run() {
                            MainWindow.show();
                        }
                    });
                }
            });

            // Create the right click menu of the tray icon
            final PopupMenu popup = new PopupMenu();
            final MenuItem exitItem = new MenuItem("Exit");
            exitItem.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(final ActionEvent e) {
                    hide();
                    Environment.exit();
                }
            });
            popup.add(exitItem);
            sTrayIcon.setPopupMenu(popup);
        }

        return sTrayIcon;
    }

    /**
     * Shows the tray icon if it's supported and the user wants it there.
     *
     * @return True iff the tray icon is now in the tray
     */
    public static synchronized boolean show() {
        if (sInTray) {
            return true;
        }
        if (!Pref.SHOW_IN_TRAY.getBoolean() || !SystemTray.isSupported()) {
            return false;
        }

        try {
            SystemTray.getSystemTray().add(getTrayIcon());
            sInTray = true;
            return true;

        } catch (final AWTException ex) {
            System.err.println(ex);
            return false;
        }
    }

    /**
     * Hides the tray icon if it was in the tray.
     */
    public static synchronized void hide() {
        if (sInTray) {
            SystemTray.getSystemTray().remove(sTrayIcon);
            sInTray = false;
        }
    }

    /**
     * @return True iff the tray icon is shown in the tray
     */
    public static synchronized boolean isInTray() {
        return sInTray;
    }

    /**
     * This is a static-only class.
     */
    private TrayManager() {}
}
