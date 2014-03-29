package com.dopelives.dopestreamer;

import java.awt.AWTException;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.SystemTray;
import java.awt.Toolkit;
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
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

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

    public static void main(final String[] args) {
        Application.launch(Initialiser.class, args);
    }

    @Override
    public void start(final Stage stage) throws Exception {
        Platform.setImplicitExit(false);

        final Parent root = FXMLLoader.load(getClass().getResource(RESOURCE_FOLDER + "main_window.fxml"));

        stage.setTitle(TITLE);
        stage.getIcons().add(new Image(getClass().getResourceAsStream(IMAGE_FOLDER + "dopestreamer.png")));
        stage.setScene(new Scene(root, WIDTH, HEIGHT));
        stage.setResizable(false);
        stage.show();

        // Exit when the window gets closed
        stage.setOnCloseRequest(new EventHandler<WindowEvent>() {
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
        stage.iconifiedProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(final ObservableValue<? extends Boolean> observable, final Boolean oldValue,
                    final Boolean newValue) {
                if (newValue.booleanValue() && Pref.MINIMISE_TO_TRAY.getBoolean() && SystemTray.isSupported()) {
                    // Construct the tray icon
                    final SystemTray tray = SystemTray.getSystemTray();
                    final TrayIcon trayIcon = new TrayIcon(Toolkit.getDefaultToolkit().getImage(
                            "src/" + IMAGE_FOLDER + "dopestreamer_small.png"), TITLE);

                    // Open the main window upon double clicking the tray icon
                    trayIcon.addActionListener(new ActionListener() {
                        @Override
                        public void actionPerformed(final ActionEvent e) {
                            Platform.runLater(new Runnable() {
                                @Override
                                public void run() {
                                    stage.setIconified(false);
                                    stage.show();
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
                        stage.hide();
                    } catch (final AWTException ex) {
                        System.err.println(ex);
                    }

                }
            }
        });
    }

    /**
     * Closes the JVM and its child processes.
     */
    private void exit() {
        final Shell shell = Shell.getInstance();
        shell.killProcessTree(shell.getJvmProcessId());
    }

}
