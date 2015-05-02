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
import com.dopelives.dopestreamer.util.Executor;
import com.dopelives.dopestreamer.util.ImageHelper;
import com.dopelives.dopestreamer.util.Pref;

public class Header implements Initializable {

    @FXML
    private ImageView aboutButton;

    @Override
    public synchronized void initialize(final URL location, final ResourceBundle resources) {
        // Check if a new version is available, and if so, replace the icon
        if (Updater.isDopestreamerVersionCheckComplete()) {
            if (isUpdateAvailable()) {
                aboutButton.setImage(ImageHelper.loadJavaFXImage("exclamation.png"));
            }
        } else {
            Executor.execute(() -> {
                if (isUpdateAvailable()) {
                    Platform.runLater(() -> {
                        aboutButton.setImage(ImageHelper.loadJavaFXImage("exclamation.png"));
                    });
                }
            });
        }
    }

    /**
     * @return True if an update is available for something that should be checked
     */
    private boolean isUpdateAvailable() {
        return Updater.isDopestreamerOutdated()
                || (Pref.LIVESTREAMER_UPDATE_CHECK.getBoolean() && Updater.isLivestreamerOutdated());
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
