package com.dopelives.dopestreamer.gui.controllers;

import java.net.URL;
import java.util.ResourceBundle;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;

import com.dopelives.dopestreamer.Environment;

public class About implements Initializable {

    @FXML
    private Label version;

    @Override
    public synchronized void initialize(final URL location, final ResourceBundle resources) {

        version.setText("Version " + Environment.VERSION);

    }

}
