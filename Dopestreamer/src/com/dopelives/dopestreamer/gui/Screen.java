package com.dopelives.dopestreamer.gui;

import java.io.IOException;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;

import com.dopelives.dopestreamer.Environment;

/**
 * The enum containing each screen.
 */
public enum Screen {

    /** The main screen with the interface to control streams */
    STREAMS("streams.fxml"),
    /** The screen where the users can change settings */
    SETTINGS("settings.fxml"),
    /** The screen where the version number and credits are displayed */
    ABOUT("about.fxml");

    /** The node for the screen's contents */
    private final Parent mNode;

    private Screen(final String resource) {
        try {
            mNode = FXMLLoader.load(getClass().getResource(Environment.LAYOUT_FOLDER + resource));
        } catch (final IOException ex) {
            throw new RuntimeException("Couldn't load resource " + resource, ex);
        }
    }

    /**
     * @return The node for the screen's contents
     */
    public Parent getNode() {
        return mNode;
    }
}
