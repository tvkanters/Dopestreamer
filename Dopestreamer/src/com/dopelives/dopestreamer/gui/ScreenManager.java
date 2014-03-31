package com.dopelives.dopestreamer.gui;

import java.util.Collection;

import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.layout.StackPane;

/**
 * The pane containing the active children screens.
 */
public class ScreenManager extends StackPane {

    /**
     * Prepares the pane containing the active children screens.
     *
     * @param mainScreen
     *            The main screen to show
     */
    public ScreenManager(final Screen mainScreen) {
        super(mainScreen.getNode());
        setPrefSize(StageManager.WIDTH, StageManager.HEIGHT);
    }

    /**
     * Switches the active screen for the given one.
     *
     * @param screen
     *            The screen to switch to
     */
    public void setScreen(final Screen screen) {
        final Collection<Node> screens = getChildren();
        screens.clear();
        screens.add(screen.getNode());
    }

    /**
     * Toggles a screen between visible and invisible states. If a screen was already in the stack but overlapped, it
     * will be brought to the front.
     *
     * @param screen
     *            The screen to toggle
     */
    public void toggleScreen(final Screen screen) {
        final Node screenNode = screen.getNode();
        final ObservableList<Node> screens = getChildren();

        if (screens.contains(screenNode)) {
            if (screens.get(screens.size() - 1) == screenNode) {
                // Screen was already at front so remove
                screens.remove(screenNode);

            } else {
                // Bring screen to front
                screens.remove(screenNode);
                screens.add(screenNode);
            }

        } else {
            // Show the screen
            screens.add(screenNode);
        }
    }

}
