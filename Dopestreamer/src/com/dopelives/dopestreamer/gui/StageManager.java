package com.dopelives.dopestreamer.gui;

import java.io.IOException;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.EventHandler;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

import com.dopelives.dopestreamer.Environment;
import com.dopelives.dopestreamer.Pref;
import com.dopelives.dopestreamer.TrayManager;

/**
 * Handles the main window's stage. May only be opened once.
 */
public class StageManager extends Application {

    /** The window's width */
    public static final int WIDTH = 300;
    /** The window's height */
    public static final int HEIGHT = 385;
    /** The margin that JavaFX adds for some reason */
    public static final int MARGIN_CORRECTION = 10;

    /** The stage of the main window */
    private static Stage sStage;
    /** The manager used to change the active screens */
    private static final ScreenManager sScreenManager = new ScreenManager(Screen.STREAMS);

    @Override
    public void start(final Stage stage) throws IOException {
        if (sStage != null) {
            throw new RuntimeException("Main window has already been started");
        }

        // Create main window
        Platform.setImplicitExit(false);
        sStage = stage;
        sStage.setScene(new Scene(new Group(sScreenManager), WIDTH - MARGIN_CORRECTION, HEIGHT - MARGIN_CORRECTION));
        sStage.setTitle(Environment.TITLE);
        sStage.getIcons().add(new Image(getClass().getResourceAsStream(Environment.IMAGE_FOLDER + "dopestreamer.png")));
        sStage.setResizable(false);

        // Add listeners
        sStage.setOnCloseRequest(new CloseHandler());
        sStage.iconifiedProperty().addListener(new MinimiseListener());
        sStage.xProperty().addListener(new PositionListener(Pref.WINDOW_X));
        sStage.yProperty().addListener(new PositionListener(Pref.WINDOW_Y));

        // Open the window but minimise if preferred by user
        if (Pref.START_MINIMISED.getBoolean()) {
            if (!Pref.SHOW_IN_TRAY.getBoolean()) {
                sStage.setIconified(true);
                loadWindowPosition();
                sStage.show();
            }

        } else {
            show();
        }
    }

    /**
     * Shows the main stage in the last set location and focuses it.
     */
    public static void show() {
        sStage.setIconified(false);
        loadWindowPosition();
        sStage.show();
        sStage.requestFocus();
    }

    /**
     * Sets the stage's window position to the stores or default position.
     */
    private static void loadWindowPosition() {
        final int windowX = Pref.WINDOW_X.getInteger();
        final int windowY = Pref.WINDOW_Y.getInteger();

        if (javafx.stage.Screen.getScreensForRectangle(windowX, windowY, WIDTH, HEIGHT).isEmpty()) {
            // If the window is not in a visible area, centre it
            sStage.centerOnScreen();
        } else {
            sStage.setX(windowX);
            sStage.setY(windowY);
        }
    }

    /**
     * @return The manager used to change the active screens
     */
    public static ScreenManager getScreenmanager() {
        return sScreenManager;
    }

    /**
     * Exists the program when closing the main window.
     */
    private class CloseHandler implements EventHandler<WindowEvent> {
        @Override
        public void handle(final WindowEvent event) {
            // Don't delay the window closing
            new Thread(new Runnable() {
                @Override
                public void run() {
                    Environment.exit();
                }
            }).start();
        }
    };

    /**
     * Hides the stage if it gets minimised and the tray icon is visible.
     */
    private class MinimiseListener implements ChangeListener<Boolean> {
        @Override
        public void changed(final ObservableValue<? extends Boolean> observable, final Boolean oldValue,
                final Boolean newValue) {
            if (newValue.booleanValue() && TrayManager.getInstance().isInTray()) {
                sStage.hide();
            }
        }
    }

    /**
     * Updates the position preference upon call-backs.
     */
    private class PositionListener implements ChangeListener<Number> {

        /** The preference to update upon call-back */
        private final Pref mPref;

        /**
         * Updates the position preference upon call-backs.
         *
         * @param pref
         *            The preference to update (e.g., WINDOW_X, WINDOW_Y)
         */
        public PositionListener(final Pref pref) {
            mPref = pref;
        }

        @Override
        public void changed(final ObservableValue<? extends Number> observable, final Number oldValue,
                final Number newValue) {
            if (newValue.intValue() >= 0) {
                mPref.put(newValue.intValue());
            }
        }
    }
}
