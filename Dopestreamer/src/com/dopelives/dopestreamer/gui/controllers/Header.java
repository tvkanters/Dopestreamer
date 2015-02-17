package com.dopelives.dopestreamer.gui.controllers;

import java.net.URL;
import java.util.ResourceBundle;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.image.ImageView;

import com.dopelives.dopestreamer.Updater;
import com.dopelives.dopestreamer.gui.Screen;
import com.dopelives.dopestreamer.gui.StageManager;
import com.dopelives.dopestreamer.util.ImageHelper;
import com.dopelives.dopestreamer.util.Executor;

public class Header implements Initializable {

    @FXML
    private ImageView aboutButton;

    @Override
    public synchronized void initialize(final URL location, final ResourceBundle resources) {
        // Check if a new version is available
        Executor.execute(() -> {
            if (Updater.isOutdated()) {
                // If a new version is available, replace the icon
                Platform.runLater(() -> {
                    aboutButton.setImage(ImageHelper.loadJavaFXImage("exclamation.png"));
                });
            }
        });
    }

    @FXML
    public void onHomeClicked() {
        StageManager.getScreenmanager().setScreen(Screen.STREAMS);
    }

    @FXML
    public void onSettingsClicked() {
        StageManager.getScreenmanager().toggleScreen(Screen.SETTINGS);
    }

    @FXML
    public void onAboutClicked() {
        StageManager.getScreenmanager().toggleScreen(Screen.ABOUT);
    }

}
