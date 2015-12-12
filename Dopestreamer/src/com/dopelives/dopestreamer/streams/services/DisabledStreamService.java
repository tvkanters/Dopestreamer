package com.dopelives.dopestreamer.streams.services;

/**
 * The place holder service for a previously used stream service has since been removed.
 */
public class DisabledStreamService extends StreamService {

    /**
     * This stream service can be accessed through {@link StreamServiceManager#DISABLED}.
     */
    /* default */ DisabledStreamService() {}

    /**
     * {@inheritDoc}
     */
    @Override
    public String getKey() {
        return "disabled";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getLabel() {
        return "Disabled";
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
