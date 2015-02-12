package com.dopelives.dopestreamer.util;

import javafx.scene.image.Image;

import javax.swing.ImageIcon;

import com.dopelives.dopestreamer.Environment;
import com.dopelives.dopestreamer.gui.StageManager;

/**
 * A helper class for image related functionality.
 */
public class ImageHelper {

    /**
     * Loads an image based on the filename.
     *
     * @param filename
     *            The filename of the image to load
     *
     * @return The image
     */
    public static java.awt.Image loadAwtImage(final String filename) {
        return new ImageIcon(StageManager.class.getResource(Environment.IMAGE_FOLDER + filename)).getImage();
    }

    /**
     * Loads an image based on the filename.
     *
     * @param filename
     *            The filename of the image to load
     *
     * @return The image
     */
    public static Image loadJavaFXImage(final String filename) {
        return new Image(Environment.IMAGE_FOLDER + filename);
    }

    /**
     * This is a static-only class.
     */
    private ImageHelper() {}
}
