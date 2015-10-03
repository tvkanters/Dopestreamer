package com.dopelives.dopestreamer.gui.combobox;

import javafx.scene.control.ListCell;

/**
 * The class for a combo box cell with static text.
 */
public class ComboBoxTextCell<T extends ComboBoxItem> extends ListCell<T> {

    /** The text to show in the combo box cell */
    private final String mLabel;

    /**
     * Creates a new combo box cell with static text.
     *
     * @param label
     *            The label to show
     */
    public ComboBoxTextCell(final String label) {
        mLabel = label;
        setPrefHeight(30);
    }

    /**
     * {@inheritDoc}
     *
     * Sets the text of the cell to the given static label.
     */
    @Override
    public void updateItem(final T item, final boolean empty) {
        super.updateItem(item, empty);

        if (item == null) {
            return;
        }

        setText(mLabel);
    }

}
