package com.dopelives.dopestreamer.gui.controllers;

import java.net.URL;
import java.util.ResourceBundle;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;

import com.dopelives.dopestreamer.Audio;
import com.dopelives.dopestreamer.gui.Screen;
import com.dopelives.dopestreamer.gui.StageManager;

public class Header implements Initializable {

    @Override
    public synchronized void initialize(final URL location, final ResourceBundle resources) {}

    @FXML
    public void onHomeClicked() {
        StageManager.getScreenmanager().setScreen(Screen.STREAMS);
        Audio.playNotification();
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
