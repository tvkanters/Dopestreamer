package com.dopelives.dopestreamer.gui.combobox;

import javafx.scene.control.ListCell;

/**
 * The class for a combo box cell with static text.
 */
public class ComboBoxTextCell<T extends ComboBoxItem> extends ListCell<T> {

    /**
     * Creates a new combo box cell with static text.
     *
     * @param label
     *            The label to show
     */
    public ComboBoxTextCell(final String label) {
        setPrefHeight(30);
        setText(label);
    }

}
