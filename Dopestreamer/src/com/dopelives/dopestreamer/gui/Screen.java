package com.dopelives.dopestreamer.gui;

import java.io.IOException;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;

import com.dopelives.dopestreamer.Environment;
import com.dopelives.dopestreamer.gui.controllers.Controller;

/**
 * The enum containing each screen.
 */
public enum Screen {

    /** The main screen with the interface to control streams */
    STREAMS("streams.fxml"),
    /** The screen where the users can change settings */
    SETTINGS("settings.fxml"),
    /** The screen where the version number and credits are displayed */
    ABOUT("about.fxml"),
    /** The screen shown while Dopestreamer is updating itself */
    UPDATE("update.fxml");

    /** The resource of the screen's contents */
    private final String mResource;
    /** The node for the screen's contents */
    private Parent mNode = null;
    /** The controller for the screen's contents */
    private Controller mController = null;

    /**
     * Creates a new screen for the GUI.
     *
     * @param resource
     *            The resource of the screen's contents
     */
    private Screen(final String resource) {
        mResource = resource;
    }

    /**
     * Initialises the screen's contents if it wasn't already.
     */
    private void initialise() {
        if (mNode == null) {
            try {
                final FXMLLoader loader = new FXMLLoader(getClass().getResource(Environment.LAYOUT_FOLDER + mResource));
                mNode = loader.load();
                mController = loader.getController();
            } catch (final IOException ex) {
                throw new RuntimeException("Couldn't load resource " + mResource, ex);
            }
        }
    }

    /**
     * @return The node for the screen's contents
     */
    public Parent getNode() {
        initialise();

        return mNode;
    }

    /**
     * @return The controller for the screen's contents
     */
    public Controller getController() {
        initialise();

        return mController;
    }
}
