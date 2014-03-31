package com.dopelives.dopestreamer.gui;

/**
 * The possible states that a stream can be in.
 */
public enum StreamState {
    INACTIVE("Start stream", "inactive"),
    CONNECTING("Connecting…", "busy"),
    WAITING("Waiting for stream…", "busy"),
    BUFFERING("Buffering…", "busy"),
    ACTIVE("Streaming…", "active");

    /** The label to show in the GUI for this state */
    private final String mLabel;

    /** The class to apply to the button during this state */
    private final String mCssClass;

    private StreamState(final String label, final String cssClass) {
        mLabel = label;
        mCssClass = cssClass;
    }

    /**
     * @return The label to show in the GUI for this state
     */
    public String getLabel() {
        return mLabel;
    }

    /**
     * @return The class to apply to the button during this state
     */
    public String getCssClass() {
        return mCssClass;
    }

}
