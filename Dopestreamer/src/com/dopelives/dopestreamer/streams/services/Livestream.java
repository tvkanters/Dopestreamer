package com.dopelives.dopestreamer.streams.services;

import com.dopelives.dopestreamer.streams.StreamService;

/**
 * The service for Twitch streams.
 */
public class Livestream extends StreamService {

    /**
     * {@inheritDoc}
     */
    @Override
    public String getKey() {
        return "livestream";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getLabel() {
        return "Livestream (new only)";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String getIconUrl() {
        return "livestream.png";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getUrl() {
        return "new.livestream.com/";
    }

}
