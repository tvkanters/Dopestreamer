package com.dopelives.dopestreamer.streams.services;

/**
 * The service for livestream.com streams.
 */
public class LivestreamOld extends StreamService {

    /**
     * {@inheritDoc}
     */
    @Override
    public String getKey() {
        return "livestreamold";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getLabel() {
        return "Livestream (old)";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getStreamServiceIconUrl() {
        return "livestream.png";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getUrl() {
        return "livestream.com/";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getDefaultChannel() {
        return "dopefish";
    }

}
