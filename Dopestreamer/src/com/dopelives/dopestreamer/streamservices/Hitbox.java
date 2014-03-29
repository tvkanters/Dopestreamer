package com.dopelives.dopestreamer.streamservices;

import com.dopelives.dopestreamer.streams.StreamService;

/**
 * The service for Hitbox streams.
 */
public class Hitbox extends StreamService {

    /**
     * {@inheritDoc}
     */
    @Override
    public String getLabel() {
        return "Hitbox";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String getIconUrl() {
        return "hitbox.png";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getUrl() {
        return "hitbox.tv/";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getDefaultChannel() {
        return "dopefish";
    }

}
