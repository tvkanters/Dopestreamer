package com.dopelives.dopestreamer.gui.controllers;

import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.scene.Node;

/**
 * A helper class for controller related functionality.
 */
public class ControllerHelper {

    /**
     * Adds or removes a CSS class on an element.
     *
     * @param element
     *            The element to change the CSS class of
     * @param cssClass
     *            The CSS class to add or remove
     * @param enabled
     *            True if the CSS class should be added, false for removal
     */
    public static void setCssClass(final Node element, final String cssClass, final boolean enabled) {
        Platform.runLater(() -> {
            final ObservableList<String> cssClasses = element.getStyleClass();
            if (enabled) {
                if (!cssClasses.contains(cssClass)) {
                    cssClasses.add(cssClass);
                }
            } else {
                cssClasses.remove(cssClass);
            }
        });
    }

    /**
     * This is a static-only class.
     */
    private ControllerHelper() {}
}
