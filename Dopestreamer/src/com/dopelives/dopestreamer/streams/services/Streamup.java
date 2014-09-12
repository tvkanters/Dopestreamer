package com.dopelives.dopestreamer.streams.services;


/**
 * The service for Streamup streams.
 */
public class Streamup extends StreamService {

    /**
     * {@inheritDoc}
     */
    @Override
    public String getKey() {
        return "streamup";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getLabel() {
        return "Streamup";
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
        return "streamup.com/";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getDefaultChannel() {
        return "dopelives";
    }

}
