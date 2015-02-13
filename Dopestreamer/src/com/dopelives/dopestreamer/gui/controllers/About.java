package com.dopelives.dopestreamer.gui.controllers;

import java.awt.Desktop;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ResourceBundle;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.text.Text;

import com.dopelives.dopestreamer.Environment;
import com.dopelives.dopestreamer.Updater;
import com.dopelives.dopestreamer.gui.Screen;
import com.dopelives.dopestreamer.gui.StageManager;

public class About implements Initializable, Controller {

    @FXML
    private Node updateBox;
    @FXML
    private Label version;
    @FXML
    private Text versionAvailable;

    @Override
    public synchronized void initialize(final URL location, final ResourceBundle resources) {
        version.setText("Version " + Environment.VERSION);

        if (Updater.isOutdated()) {
            versionAvailable.setText("New update available: v" + Updater.getLatestVersion());

            updateBox.setManaged(true);
            updateBox.setVisible(true);
        }
    }

    @Override
    public void onActived() {}

    @FXML
    public void onChangelogClicked() {
        openUrl("https://github.com/tvkanters/Dopestreamer/releases");
    }

    @FXML
    public void onUpdateClicked() {
        StageManager.getScreenmanager().setScreen(Screen.UPDATE);
    }

    @FXML
    public void onEzidClicked() {
        openUrl("http://tvkdevelopment.com/");
    }

    @FXML
    public void onXphomeClicked() {
        openUrl("http://vacker.me/");
    }

    @FXML
    public void onDopelivesClicked() {
        openUrl("http://dopelives.com/");
    }

    @FXML
    public void onLivestreamerClicked() {
        openUrl("https://github.com/chrippa/livestreamer");
    }

    @FXML
    public void onRtmpDumpClicked() {
        openUrl("http://rtmpdump.mplayerhq.hu/");
    }

    /**
     * Opens a given URL in the default browser.
     *
     * @param url
     *            The URL to open
     */
    private void openUrl(final String url) {
        try {
            Desktop.getDesktop().browse(new URI(url));
        } catch (final IOException | URISyntaxException ex) {
            ex.printStackTrace();
        }
    }

}
