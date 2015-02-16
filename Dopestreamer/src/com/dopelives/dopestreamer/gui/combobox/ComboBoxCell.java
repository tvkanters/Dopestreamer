package com.dopelives.dopestreamer.gui.combobox;

import javafx.scene.control.ListCell;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import com.dopelives.dopestreamer.Environment;

/**
 * The class for a combo box cell.
 */
public class ComboBoxCell<T extends ComboBoxItem> extends ListCell<T> {

    /**
     * Creates a new combo box cell that looks nice in the Dopestreamer GUI.
     */
    public ComboBoxCell() {
        setPrefHeight(30);
    }

    /**
     * {@inheritDoc}
     *
     * Updates the details of an item. Fills the label and, if applicable, the icon of an item.
     */
    @Override
    public void updateItem(final T item, final boolean empty) {
        super.updateItem(item, empty);

        if (item == null) {
            return;
        }

        setText(item.getLabel());

        final String iconUrl = item.getIconUrl();
        if (iconUrl != null) {
            setGraphic(new ImageView(new Image(Environment.IMAGE_FOLDER + iconUrl)));
        }
    }
}
