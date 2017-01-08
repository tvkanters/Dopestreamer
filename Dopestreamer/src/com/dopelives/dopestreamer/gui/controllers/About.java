package com.dopelives.dopestreamer.gui.controllers;

import java.awt.Desktop;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ResourceBundle;

import com.dopelives.dopestreamer.Environment;
import com.dopelives.dopestreamer.Updater;
import com.dopelives.dopestreamer.gui.Screen;
import com.dopelives.dopestreamer.gui.StageManager;
import com.dopelives.dopestreamer.util.Pref;

import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.text.Text;

public class About extends ScrollableController {

    @FXML
    private Node dopestreamerUpdateBox;
    @FXML
    private Label dopestreamerVersion;
    @FXML
    private Text dopestreamerVersionAvailable;

    @FXML
    private Node livestreamerUpdateBox;
    @FXML
    private Label livestreamerVersion;
    @FXML
    private Text liveStreamerVersionAvailable;

    @Override
    public synchronized void initialize(final URL location, final ResourceBundle resources) {
        super.initialize(location, resources);

        dopestreamerVersion.setText("Version " + Environment.VERSION);
        if (Updater.isDopestreamerOutdated()) {
            dopestreamerVersionAvailable.setText("New update available: v" + Updater.getLatestDopestreamerVersion());

            dopestreamerUpdateBox.setManaged(true);
            dopestreamerUpdateBox.setVisible(true);
        }

        final String currentVersion = Updater.getCurrentLivestreamerVersion();
        livestreamerVersion
                .setText("Streamlink " + (currentVersion != null ? "version " + currentVersion : "not found"));
        if (Pref.LIVESTREAMER_UPDATE_CHECK.getBoolean() && Updater.isLivestreamerOutdated()) {
            liveStreamerVersionAvailable.setText("New update available: v" + Updater.getLatestLivestreamerVersion());

            livestreamerUpdateBox.setManaged(true);
            livestreamerUpdateBox.setVisible(true);
        }
    }

    @Override
    public void onActived() {}

    @FXML
    public void onDopestreamerChangelogClicked() {
        openUrl("https://github.com/tvkanters/Dopestreamer/releases");
    }

    @FXML
    public void onLivestreamerChangelogClicked() {
        openUrl("https://github.com/streamlink/streamlink/releases");
    }

    @FXML
    public void onDopestreamerUpdateClicked() {
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
    public void onBoomerClicked() {
        openUrl("https://github.com/Booom3");
    }

    @FXML
    public void onDopelivesClicked() {
        openUrl("http://dopelives.com/");
    }

    @FXML
    public void onLivestreamerClicked() {
        openUrl("https://github.com/streamlink/streamlink");
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
