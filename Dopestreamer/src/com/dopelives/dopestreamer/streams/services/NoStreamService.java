package com.dopelives.dopestreamer.streams.services;

/**
 * The place holder service for when none is enabled.
 */
public class NoStreamService extends StreamService {

    /**
     * This stream service can be accessed through {@link StreamServiceManager#NONE}.
     */
    /* default */ NoStreamService() {}

    /**
     * {@inheritDoc}
     */
    @Override
    public String getKey() {
        return "none";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getLabel() {
        return "None enabled";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getStreamServiceIconUrl() {
        return "disabled.png";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getUrl() {
        return "";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isChannelPossible(final String channel) {
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isConnectPossible(final String channel) {
        return false;
    }

}
