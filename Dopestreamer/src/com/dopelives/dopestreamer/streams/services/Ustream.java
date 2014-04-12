package com.dopelives.dopestreamer.streams.services;

/**
 * The service for Ustream streams.
 */
public class Ustream extends StreamService {

    /**
     * {@inheritDoc}
     */
    @Override
    public String getKey() {
        return "ustream";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getLabel() {
        return "Ustream";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String getIconUrl() {
        return "ustream.png";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getUrl() {
        return "ustream.tv/channel/";
    }

}
