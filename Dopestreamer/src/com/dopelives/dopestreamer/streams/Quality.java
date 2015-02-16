package com.dopelives.dopestreamer.streams;

import com.dopelives.dopestreamer.gui.combobox.ComboBoxItem;

/**
 * The possible streaming qualities.
 */
public enum Quality implements ComboBoxItem {

    BEST("Best", "best"),
    P720("720p", "720p"),
    P480("480p", "480p"),
    P360("360p", "360p"),
    HIGH("720p", "high"),
    MEDIUM("480p", "medium"),
    LOW("360p", "low"),
    WORST("Worst", "worst");

    /** The label to show the user */
    private final String mLabel;

    /** The command to send to Livestreamer */
    private final String mCommand;

    private Quality(final String label, final String command) {
        mLabel = label;
        mCommand = command;
    }

    /**
     * @return The label to show the user
     */
    @Override
    public String getLabel() {
        return mLabel;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getIconUrl() {
        return null;
    }

    /**
     * @return The command to send to Livestreamer
     */
    public String getCommand() {
        return mCommand;
    }

}
