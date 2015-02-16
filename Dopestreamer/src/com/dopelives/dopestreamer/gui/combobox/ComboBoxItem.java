package com.dopelives.dopestreamer.gui.combobox;

/**
 * The interface for an item to place in a combo box.
 */
public interface ComboBoxItem {

    /**
     * @return The label to show for this item
     */
    public String getLabel();

    /**
     * @return The URL for the icon to show next to the label, relative to the image path, or null if there is none
     */
    public String getIconUrl();

}
