package com.dopelives.dopestreamer.gui.controllers;

import java.net.URL;
import java.util.ResourceBundle;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.CheckBox;

import com.dopelives.dopestreamer.Pref;
import com.dopelives.dopestreamer.TrayManager;
import com.dopelives.dopestreamer.gui.Screen;
import com.dopelives.dopestreamer.gui.StageManager;

public class Settings implements Initializable {

    @FXML
    private CheckBox autoStartToggle;
    @FXML
    private CheckBox showInTrayToggle;

    @Override
    public synchronized void initialize(final URL location, final ResourceBundle resources) {

        // Check the auto-start preference
        autoStartToggle.setSelected(Pref.AUTO_START.getBoolean());

        // Check the minimise-to-tray preference
        showInTrayToggle.setSelected(Pref.SHOW_IN_TRAY.getBoolean());

    }

    @FXML
    public void onAutoStartToggle() {
        Pref.AUTO_START.put(autoStartToggle.isSelected());
    }

    @FXML
    public void onShowInTrayToggle() {
        final boolean showInTray = showInTrayToggle.isSelected();
        Pref.SHOW_IN_TRAY.put(showInTray);

        if (showInTray) {
            TrayManager.show();
        } else {
            TrayManager.hide();
        }
    }

    @FXML
    public void onSettingsClicked() {
        StageManager.setScreen(Screen.STREAMS);
    }

}
