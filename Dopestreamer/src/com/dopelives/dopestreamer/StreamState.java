package com.dopelives.dopestreamer;

/**
 * The possible states that a stream can be in.
 */
public enum StreamState {
    INACTIVE("Start stream"),
    LOADING("Loading…"),
    ACTIVE("Streaming…");

    /** The label to show in the GUI for this state */
    private final String mLabel;

    private StreamState(final String label) {
        mLabel = label;
    }

    /**
     * @return The label to show in the GUI for this state
     */
    public String getLabel() {
        return mLabel;
    }

}
