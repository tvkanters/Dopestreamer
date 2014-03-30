package com.dopelives.dopestreamer;

import java.io.IOException;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

/**
 * Handles the main window's stage. Must only be opened once.
 */
public class MainWindow extends Application {

    /** The window's width */
    private static final int WIDTH = 300;
    /** The window's height */
    private static final int HEIGHT = 370;

    /** The stage of the main window */
    private static Stage sStage;

    @Override
    public void start(final Stage stage) throws IOException {
        if (sStage != null) {
            throw new RuntimeException("Main window has already been started");
        }
        sStage = stage;
        Platform.setImplicitExit(false);

        // Create main window
        final Parent root = FXMLLoader.load(getClass().getResource(Environment.RESOURCE_FOLDER + "main_window.fxml"));
        sStage.setScene(new Scene(root, WIDTH, HEIGHT));
        sStage.setTitle(Environment.TITLE);
        sStage.getIcons().add(new Image(getClass().getResourceAsStream(Environment.IMAGE_FOLDER + "dopestreamer.png")));
        sStage.setResizable(false);
        loadWindowPosition();

        // Exit when the window gets closed
        sStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
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
        });

        // Make minimise-to-tray possible
        sStage.iconifiedProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(final ObservableValue<? extends Boolean> observable, final Boolean oldValue,
                    final Boolean newValue) {
                if (newValue.booleanValue()) {
                    if (TrayManager.isInTray()) {
                        sStage.hide();
                    }
                }
            }
        });

        // Remember the window position when moving the stage
        sStage.xProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(final ObservableValue<? extends Number> observable, final Number oldValue,
                    final Number newValue) {
                if (newValue.intValue() >= 0) {
                    Pref.WINDOW_X.put(newValue.intValue());
                }
            }
        });
        sStage.yProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(final ObservableValue<? extends Number> observable, final Number oldValue,
                    final Number newValue) {
                if (newValue.intValue() >= 0) {
                    Pref.WINDOW_Y.put(newValue.intValue());
                }
            }
        });

        sStage.show();
    }

    /**
     * Sets the stage's window position to the stores or default position.
     */
    private static void loadWindowPosition() {
        final int windowX = Pref.WINDOW_X.getInteger();
        final int windowY = Pref.WINDOW_Y.getInteger();

        if (Screen.getScreensForRectangle(windowX, windowY, WIDTH, HEIGHT).isEmpty()) {
            // If the window is not in a visible area, centre it
            sStage.centerOnScreen();
        } else {
            sStage.setX(windowX);
            sStage.setY(windowY);
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
}
