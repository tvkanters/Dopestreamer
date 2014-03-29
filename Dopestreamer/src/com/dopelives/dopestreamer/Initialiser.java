package com.dopelives.dopestreamer;

import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

import com.dopelives.dopestreamer.shell.Shell;

public class Initialiser extends Application {

    /** The absolute path to the folder containing the GUI resources */
    public static final String RESOURCE_FOLDER = "/com/dopelives/dopestreamer/res/";
    /** The absolute path to the folder containing the GUI image resources */
    public static final String IMAGE_FOLDER = RESOURCE_FOLDER + "images/";

    /** The window title */
    private static final String TITLE = "Dopestreamer";

    public static void main(final String[] args) {
        Application.launch(Initialiser.class, args);
    }

    @Override
    public void start(final Stage stage) throws Exception {
        final VBox root = FXMLLoader.load(getClass().getResource(RESOURCE_FOLDER + "main_window.fxml"));

        stage.setTitle(TITLE);
        stage.setScene(new Scene(root, 300, 300));
        stage.setResizable(false);
        stage.show();

        // Close all child process upon closing
        stage.setOnCloseRequest(new EventHandler<WindowEvent>() {
            @Override
            public void handle(final WindowEvent event) {
                final Shell shell = Shell.getInstance();
                shell.onConsoleStop(shell.getJvmProcessId());
            }
        });
    }

}
