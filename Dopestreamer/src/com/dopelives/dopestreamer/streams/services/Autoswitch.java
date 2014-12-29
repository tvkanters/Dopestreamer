package com.dopelives.dopestreamer.streams.services;

import com.dopelives.dopestreamer.streams.Quality;

/**
 * The service for auto switched streams.
 */
public class Autoswitch extends StreamService {

    /** The current service to use */
    private StreamService mCurrentService;

    /**
     * {@inheritDoc}
     */
    @Override
    public String getKey() {
        return "autoswitch";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getLabel() {
        return "Autoswitch";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String getIconUrl() {
        return "dopestreamer_small.png";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getUrl() {
        return mCurrentService.getUrl();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getDefaultChannel() {
        if (mCurrentService == null) {
            return "default";
        }

        return mCurrentService.getDefaultChannel();
    }

    /**
     * {@inheritDoc}
     *
     * Autoswitch will ignore the channel and use the default one.
     */
    @Override
    public String getConnectionDetails(final String channel, final Quality quality) {
        return mCurrentService.getConnectionDetails(mCurrentService.getDefaultChannel(), quality);
    }

    /**
     * {@inheritDoc}
     *
     * Autoswitch will ignore the channel and use the default one.
     */
    @Override
    public boolean isConnectPossible(final String channel) {
        for (final StreamService service : StreamServiceManager.getAutoswitchServices()) {
            if (service.isConnectPossible(service.getDefaultChannel())) {
                mCurrentService = service;
                return true;
            }
        }

        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean allowsCustomChannels() {
        return false;
    }

    /**
     * @return The currently used stream service
     */
    public StreamService getCurrentService() {
        return mCurrentService;
    }

}
