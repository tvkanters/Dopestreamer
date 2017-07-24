package com.dopelives.dopestreamer.streams;

import com.dopelives.dopestreamer.gui.combobox.ComboBoxItem;

/**
 * The possible streaming qualities.
 */
public enum Quality implements ComboBoxItem {

    BEST("Best", "best"),
    P72060("720p60", "720p60"),
    P720("720p", "720p"),
    P480("480p", "480p"),
    P360("360p", "360p"),
    HIGH("High", "high"),
    MEDIUM("Medium", "medium"),
    LOW("Low", "low"),
    WORST("Worst", "worst");

    /** The label to show the user */
    private final String mLabel;
    /** The command to send to Livestreamer */
    private final String mCommand;

    /**
     * Creates a new quality option for streams.
     *
     * @param label
     *            The label to show the user
     * @param command
     *            The command to send to Livestreamer
     */
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
