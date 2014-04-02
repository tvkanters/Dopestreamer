package com.dopelives.dopestreamer.gui.controllers;

import java.io.File;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.ResourceBundle;
import java.util.Timer;
import java.util.TimerTask;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;

import com.dopelives.dopestreamer.Environment;
import com.dopelives.dopestreamer.Pref;
import com.dopelives.dopestreamer.TrayManager;

public class Settings implements Initializable {

    @FXML
    private CheckBox autoStartToggle;
    @FXML
    private CheckBox showInTrayToggle;
    @FXML
    private TextField playerLocation;
    @FXML
    private Button saveOutputButton;

    @Override
    public synchronized void initialize(final URL location, final ResourceBundle resources) {

        // Check the auto-start preference
        autoStartToggle.setSelected(Pref.AUTO_START.getBoolean());

        // Check the minimise-to-tray preference
        showInTrayToggle.setSelected(Pref.SHOW_IN_TRAY.getBoolean());

        // Set text, prompt text and tooltip of player location field
        final String playerLocationInfo = "Path to custom media player";
        playerLocation.setPromptText(playerLocationInfo);
        playerLocation.setTooltip(new Tooltip(playerLocationInfo));
        playerLocation.setText(Pref.PLAYER_LOCATION.getString());
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
    public void onSaveOutput() {
        saveOutputButton.setDisable(true);

        final String filename = new SimpleDateFormat("'dopelog-'yyyyMMddhhmmss'.txt'").format(new Date());
        Environment.getOutputSpy().writeToFile(filename);

        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        saveOutputButton.setDisable(false);
                    }
                });
            }
        }, 1000);
    }

    @FXML
    public void onPlayerLocationChanged(final KeyEvent keyEvent) {
        if (keyEvent.getCode() == KeyCode.ENTER) {
            playerLocation.getParent().requestFocus();
            return;
        }

        final String input = playerLocation.getText();

        // Check input validity
        final boolean inputValid;
        if (input.equals("")) {
            inputValid = true;
        } else {
            final File inputFile = new File(input);
            inputValid = inputFile.exists() && !inputFile.isDirectory();
        }

        ControllerHelper.setCssClass(playerLocation, "invalid", !inputValid);
        if (inputValid) {
            Pref.PLAYER_LOCATION.put(input);
        }

    }

}
