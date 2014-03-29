package com.dopelives.dopestreamer.streams;

/**
 * The possible streaming qualities.
 */
public enum Quality {

    BEST("Best", "best"),
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
    public String getLabel() {
        return mLabel;
    }

    /**
     * @return The command to send to Livestreamer
     */
    public String getCommand() {
        return mCommand;
    }

}
