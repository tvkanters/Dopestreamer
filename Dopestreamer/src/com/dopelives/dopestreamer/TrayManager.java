package com.dopelives.dopestreamer;

import java.awt.AWTException;
import java.awt.Image;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.SystemTray;
import java.awt.TrayIcon;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javafx.application.Platform;

import javax.swing.ImageIcon;

import com.dopelives.dopestreamer.gui.StageManager;
import com.dopelives.dopestreamer.gui.StreamState;
import com.dopelives.dopestreamer.streams.Stream;
import com.dopelives.dopestreamer.streams.StreamListener;
import com.dopelives.dopestreamer.streams.StreamManager;

/**
 * Handles tray icon related functionality.
 */
public class TrayManager implements StreamListener {

    /** The singleton tray manager */
    private static final TrayManager sInstance = new TrayManager();

    private static final Image sIconInactive = loadImage("dopestreamer_small_grey.png");
    private static final Image sIconBusy = loadImage("dopestreamer_small_yellow.png");
    private static final Image sIconActive = loadImage("dopestreamer_small.png");

    /** The popup to show when streams are active */
    private final PopupMenu mPopupActive = new PopupMenu();
    /** The popup to show when streams are inactive */
    private final PopupMenu mPopupInactive = new PopupMenu();

    /** The tray icon to show in the tray */
    private TrayIcon mTrayIcon;
    /** Whether or not the tray icon is in the tray */
    private boolean mInTray = false;

    /**
     * @return The singleton tray manager.
     */
    public static TrayManager getInstance() {
        return sInstance;
    }

    /**
     * This is a singleton.
     */
    private TrayManager() {}

    /**
     * Retrieves the tray icon and constructs it if needed.
     *
     * @return The Dopestreamer tray icon
     */
    private TrayIcon getTrayIcon() {
        if (mTrayIcon == null) {
            // Construct the tray icon
            mTrayIcon = new TrayIcon(sIconInactive, Environment.TITLE);

            // Open the main window upon clicking the tray icon
            mTrayIcon.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(final MouseEvent e) {
                    if (e.getButton() == MouseEvent.BUTTON1) {
                        Platform.runLater(new Runnable() {
                            @Override
                            public void run() {
                                StageManager.show();
                            }
                        });
                    }
                }
            });

            // Prepare right click popup menus
            final MenuItem stopStreamItem = new MenuItem("Stop stream");
            stopStreamItem.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(final ActionEvent e) {
                    StreamManager.getInstance().stopStream();
                }
            });
            mPopupActive.add(stopStreamItem);

            final MenuItem activeExitItem = new MenuItem("Exit");
            activeExitItem.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(final ActionEvent e) {
                    Environment.exit();
                }
            });
            mPopupActive.add(activeExitItem);

            final MenuItem startStreamItem = new MenuItem("Restart last stream");
            startStreamItem.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(final ActionEvent e) {
                    StreamManager.getInstance().restartLastStream();
                }
            });
            mPopupInactive.add(startStreamItem);

            final MenuItem inactiveExitItem = new MenuItem("Exit");
            inactiveExitItem.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(final ActionEvent e) {
                    Environment.exit();
                }
            });
            mPopupInactive.add(inactiveExitItem);

            // Create the right click menu of the tray icon
            final StreamManager streamManager = StreamManager.getInstance();
            streamManager.addListener(this);
            onStateUpdated(streamManager, null, streamManager.getStreamState());
        }

        return mTrayIcon;
    }

    /**
     * Shows the tray icon if it's supported and the user wants it there.
     *
     * @return True iff the tray icon is now in the tray
     */
    public synchronized boolean show() {
        if (mInTray) {
            return true;
        }
        if (!Pref.SHOW_IN_TRAY.getBoolean() || !SystemTray.isSupported()) {
            return false;
        }

        try {
            SystemTray.getSystemTray().add(getTrayIcon());
            mInTray = true;
            return true;

        } catch (final AWTException ex) {
            System.err.println(ex);
            return false;
        }
    }

    /**
     * Hides the tray icon if it was in the tray.
     */
    public synchronized void hide() {
        if (mInTray) {
            SystemTray.getSystemTray().remove(mTrayIcon);
            mInTray = false;
        }
    }

    /**
     * @return True iff the tray icon is shown in the tray
     */
    public synchronized boolean isInTray() {
        return mInTray;
    }

    /**
     * {@inheritDoc}
     *
     * Updates the popup menu based on the stream state.
     */
    @Override
    public void onStateUpdated(final StreamManager streamManager, final StreamState oldState, final StreamState newState) {
        switch (newState) {
            case INACTIVE:
                mTrayIcon.setImage(sIconInactive);
                mTrayIcon.setPopupMenu(mPopupInactive);
                break;

            case CONNECTING:
            case BUFFERING:
            case WAITING:
                mTrayIcon.setImage(sIconBusy);
                mTrayIcon.setPopupMenu(mPopupActive);
                break;

            case ACTIVE:
                mTrayIcon.setImage(sIconActive);
                mTrayIcon.setPopupMenu(mPopupActive);
                break;

            default:
                throw new IllegalStateException("Unknown state: " + newState);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onInvalidChannel(final Stream stream) {}

    /**
     * {@inheritDoc}
     */
    @Override
    public void onInvalidMediaPlayer(final Stream stream) {}

    private static Image loadImage(final String filename) {
        return new ImageIcon(StageManager.class.getResource(Environment.IMAGE_FOLDER + filename)).getImage();
    }

}
