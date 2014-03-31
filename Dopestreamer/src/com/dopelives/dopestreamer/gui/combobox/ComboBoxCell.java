package com.dopelives.dopestreamer.gui.combobox;

import javafx.scene.control.ListCell;

/**
 * The base class for a combo box cell.
 */
public abstract class ComboBoxCell<T> extends ListCell<T> {

    public ComboBoxCell() {
        setPrefHeight(30);
    }
}
