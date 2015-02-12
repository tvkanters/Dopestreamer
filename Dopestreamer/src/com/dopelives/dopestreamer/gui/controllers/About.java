package com.dopelives.dopestreamer.gui.controllers;

import java.awt.Desktop;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ResourceBundle;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.text.Text;

import com.dopelives.dopestreamer.Environment;
import com.dopelives.dopestreamer.Updater;

public class About implements Initializable {

    @FXML
    private Label version;
    @FXML
    private Text versionAvailable;

    @Override
    public synchronized void initialize(final URL location, final ResourceBundle resources) {
        version.setText("Version " + Environment.VERSION);

        final String latestVersion = Updater.getLatestVersion();
        if (!latestVersion.equals(Environment.VERSION)) {
            versionAvailable.setText("New version available: v" + latestVersion);
            versionAvailable.setManaged(true);
        }
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
