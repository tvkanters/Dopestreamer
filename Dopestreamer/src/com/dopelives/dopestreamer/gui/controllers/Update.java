package com.dopelives.dopestreamer.gui.controllers;

import java.net.URL;
import java.util.ResourceBundle;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;

import com.dopelives.dopestreamer.Updater;
import com.dopelives.dopestreamer.gui.Screen;
import com.dopelives.dopestreamer.gui.StageManager;
import com.dopelives.dopestreamer.streams.StreamManager;
import com.dopelives.dopestreamer.util.Executor;

public class Update implements Initializable, Controller {

    @FXML
    private Node boxUpdating;
    @FXML
    private Node boxFailed;
    @FXML
    private Node boxFinished;

    /** Whether or not updating was a success */
    private boolean mSuccess;

    @Override
    public synchronized void initialize(final URL location, final ResourceBundle resources) {}

    @Override
    public void onActived() {
        Executor.execute(() -> {
            // Close child processes
            StreamManager.getInstance().stopStream();

            // The UI runnable must be created before the class loader is closed
            final Runnable uiRunnable = () -> {
                // Switch screen
                boxUpdating.setManaged(false);
                boxUpdating.setVisible(false);

                if (mSuccess) {
                    StageManager.setCloseWithoutLoading(true);
                    boxFinished.setManaged(true);
                    boxFinished.setVisible(true);
                } else {
                    boxFailed.setManaged(true);
                    boxFailed.setVisible(true);
                }
            };

            // Update Dopestreamer
            mSuccess = Updater.downloadAndInstallUpdate();

            // Update the UI
            Platform.runLater(uiRunnable);
        });
    }

    @FXML
    private void onBackClicked() {
        StageManager.getScreenmanager().setScreen(Screen.STREAMS);

        Platform.runLater(() -> {
            boxUpdating.setManaged(true);
            boxUpdating.setVisible(true);

            boxFailed.setManaged(false);
            boxFailed.setVisible(false);
        });
    }

    @FXML
    private void onCloseClicked() {
        System.exit(0);
    }

}
