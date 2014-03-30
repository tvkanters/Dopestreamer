package com.dopelives.dopestreamer;

import java.awt.AWTException;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.SystemTray;
import java.awt.TrayIcon;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

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

import javax.swing.ImageIcon;

import com.dopelives.dopestreamer.shell.Shell;

public class Initialiser extends Application {

    /** The absolute path to the folder containing the GUI resources */
    public static final String RESOURCE_FOLDER = "/com/dopelives/dopestreamer/res/";
    /** The absolute path to the folder containing the GUI image resources */
    public static final String IMAGE_FOLDER = RESOURCE_FOLDER + "images/";

    /** The window's title */
    private static final String TITLE = "Dopestreamer";
    /** The window's width */
    private static final int WIDTH = 300;
    /** The window's height */
    private static final int HEIGHT = 370;

    /** The stage of the main window */
    private Stage mStage;

    public static void main(final String[] args) {
        Application.launch(Initialiser.class, args);
    }

    @Override
    public void start(final Stage stage) throws Exception {
        Platform.setImplicitExit(false);
        mStage = stage;

        final Parent root = FXMLLoader.load(getClass().getResource(RESOURCE_FOLDER + "main_window.fxml"));

        // Create main window
        mStage.setTitle(TITLE);
        mStage.getIcons().add(new Image(getClass().getResourceAsStream(IMAGE_FOLDER + "dopestreamer.png")));
        mStage.setScene(new Scene(root, WIDTH, HEIGHT));
        mStage.setResizable(false);
        loadWindowPosition();

        // Exit when the window gets closed
        mStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
            @Override
            public void handle(final WindowEvent event) {
                // Don't delay the window closing
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        exit();
                    }
                }).start();
            }
        });

        // Make minimise-to-tray possible
        mStage.iconifiedProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(final ObservableValue<? extends Boolean> observable, final Boolean oldValue,
                    final Boolean newValue) {
                if (newValue.booleanValue() && Pref.MINIMISE_TO_TRAY.getBoolean() && SystemTray.isSupported()) {
                    // Construct the tray icon
                    final SystemTray tray = SystemTray.getSystemTray();
                    final TrayIcon trayIcon = new TrayIcon(new ImageIcon(getClass().getResource(
                            IMAGE_FOLDER + "dopestreamer_small.png")).getImage(), TITLE);

                    // Open the main window upon double clicking the tray icon
                    trayIcon.addActionListener(new ActionListener() {
                        @Override
                        public void actionPerformed(final ActionEvent e) {
                            Platform.runLater(new Runnable() {
                                @Override
                                public void run() {
                                    mStage.setIconified(false);
                                    loadWindowPosition();
                                    mStage.show();
                                    tray.remove(trayIcon);
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
                            tray.remove(trayIcon);
                            exit();
                        }
                    });
                    popup.add(exitItem);
                    trayIcon.setPopupMenu(popup);

                    // Show the tray icon and hide the main window
                    try {
                        tray.add(trayIcon);
                        mStage.hide();
                    } catch (final AWTException ex) {
                        System.err.println(ex);
                    }

                }
            }
        });

        // Remember the window position when moving the stage
        mStage.xProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(final ObservableValue<? extends Number> observable, final Number oldValue,
                    final Number newValue) {
                if (newValue.intValue() >= 0) {
                    Pref.WINDOW_X.put(newValue.intValue());
                }
            }
        });
        mStage.yProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(final ObservableValue<? extends Number> observable, final Number oldValue,
                    final Number newValue) {
                if (newValue.intValue() >= 0) {
                    Pref.WINDOW_Y.put(newValue.intValue());
                }
            }
        });

        // Initialisation complete, show stage
        mStage.show();
    }

    /**
     * Sets the stage's window position to the stores or default position.
     */
    private void loadWindowPosition() {
        final int windowX = Pref.WINDOW_X.getInteger();
        final int windowY = Pref.WINDOW_Y.getInteger();

        if (Screen.getScreensForRectangle(windowX, windowY, WIDTH, HEIGHT).isEmpty()) {
            // If the window is not in a visible area, centre it
            mStage.centerOnScreen();
        } else {
            mStage.setX(windowX);
            mStage.setY(windowY);
        }
    }

    /**
     * Closes the JVM and its child processes.
     */
    private void exit() {
        final Shell shell = Shell.getInstance();
        shell.killProcessTree(shell.getJvmProcessId());
    }

}
