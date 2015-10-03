package com.dopelives.dopestreamer.gui.combobox;

import javafx.scene.control.ComboBox;

/**
 * A combo box made for check boxes that allow oppression of popup closing.
 */
public class CheckComboBox<T> extends ComboBox<T> {

    /** Whether or not the next hiding should be consumed */
    private boolean mConsumeNextHide = false;

    /**
     * Consumes the next hide action such that it won't fire.
     */
    public void consumeNextHide() {
        mConsumeNextHide = true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void hide() {
        if (mConsumeNextHide) {
            mConsumeNextHide = false;
            return;
        }

        super.hide();
    }

}
