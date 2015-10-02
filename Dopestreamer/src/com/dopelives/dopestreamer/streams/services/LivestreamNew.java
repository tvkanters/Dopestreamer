package com.dopelives.dopestreamer.streams.services;

/**
 * The service for new.livestream.com streams.
 */
public class LivestreamNew extends StreamService {

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
        return "Livestream (new)";
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
        return "new.livestream.com/";
    }

}
