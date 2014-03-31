package com.dopelives.dopestreamer.gui;

import java.util.Collection;

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

}
