package com.dopelives.dopestreamer.streams.players;

import java.util.LinkedList;
import java.util.List;

/**
 * A custom media player of which the user should enter the path.
 */
public class Custom extends MediaPlayer {

    /**
     * {@inheritDoc}
     */
    @Override
    public String getKey() {
        return "";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getLabel() {
        return "Default or custom";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String getIconUrl() {
        return "console.png";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected List<String> getPaths() {
        return new LinkedList<>();
    }

}
