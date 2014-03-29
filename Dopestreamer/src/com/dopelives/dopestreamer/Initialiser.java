package com.dopelives.dopestreamer;

import javafx.application.Application;
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
    private static final int HEIGHT = 340;

    /** The controller of the main window */
    private MainWindowController mController;

    public static void main(final String[] args) {
        Application.launch(Initialiser.class, args);
    }

    @Override
    public void start(final Stage stage) throws Exception {
        final Class<? extends Initialiser> cls = getClass();

        final FXMLLoader loader = new FXMLLoader(cls.getResource(RESOURCE_FOLDER + "main_window.fxml"));
        final Parent root = loader.load();
        mController = (MainWindowController) loader.getController();

        stage.setTitle(TITLE);
        stage.getIcons().add(new Image(cls.getResourceAsStream(IMAGE_FOLDER + "dopestreamer.png")));
        stage.setScene(new Scene(root, WIDTH, HEIGHT));
        stage.setResizable(false);
        stage.show();

        // Close all child process upon closing
        stage.setOnHidden(new EventHandler<WindowEvent>() {
            @Override
            public void handle(final WindowEvent event) {
                mController.updateState(StreamState.INACTIVE);

                final Shell shell = Shell.getInstance();
                shell.onConsoleStop(shell.getJvmProcessId());
            }
        });
    }

}
