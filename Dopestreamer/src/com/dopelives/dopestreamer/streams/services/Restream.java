package com.dopelives.dopestreamer.streams.services;

/**
 * The service for Vacker restreams.
 */
public class Restream extends Vacker {

    /**
     * {@inheritDoc}
     */
    @Override
    public String getKey() {
        return "restream";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getLabel() {
        return "Restream";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getDefaultChannel() {
        return "restream";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean allowsCustomChannels() {
        return false;
    }

}
