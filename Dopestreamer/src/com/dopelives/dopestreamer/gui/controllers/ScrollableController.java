package com.dopelives.dopestreamer.gui.controllers;

import java.net.URL;
import java.util.ResourceBundle;
import java.util.Set;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.ScrollPane;
import javafx.scene.input.MouseEvent;

public abstract class ScrollableController implements Initializable, Controller {

    /** The factor to multiple the original scroll speed with */
    private static final double SCROLL_SPEED = 3;

    @FXML
    private ScrollPane scrollPane;

    /** Whether or not the scroll bar is being dragged */
    private boolean mIsDragging = false;

    @Override
    public synchronized void initialize(final URL location, final ResourceBundle resources) {
        // Make the scroll speed not be painfully slow
        scrollPane.vvalueProperty().addListener(new ChangeListener<Number>() {
            /** The value set by this object to prevent recursive updates */
            private double mSetValue = 0;

            @Override
            public void changed(final ObservableValue<? extends Number> observable, final Number oldVal,
                    final Number newVal) {
                final double oldValue = oldVal.doubleValue();
                final double newValue = newVal.doubleValue();

                // Recursive update and dragging detection
                if (newValue == mSetValue || mIsDragging) {
                    return;
                }

                final double difference = newValue - oldValue;
                mSetValue = Math.max(scrollPane.getVmin(),
                        Math.min(scrollPane.getVmax(), newValue + difference * (SCROLL_SPEED - 1)));

                scrollPane.vvalueProperty().set(mSetValue);
            }
        });

        // Prevent scroll bar dragging from twitching due to the scroll speed fix
        // Must be done at a later time as the scroll bars are loaded later
        scrollPane.setOnMouseEntered((final MouseEvent event) -> {
            scrollPane.setOnMouseEntered(null);
            final Set<Node> nodes = scrollPane.lookupAll(".scroll-bar .thumb");
            for (final Node node : nodes) {
                node.setOnMousePressed(new EventHandler<MouseEvent>() {
                    @Override
                    public void handle(final MouseEvent event) {
                        mIsDragging = true;
                    }
                });
                node.setOnMouseReleased(new EventHandler<MouseEvent>() {
                    @Override
                    public void handle(final MouseEvent event) {
                        mIsDragging = false;
                    }
                });
            }
        });
    }

}
