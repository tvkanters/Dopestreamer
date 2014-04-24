package com.dopelives.dopestreamer.streams.services;

/**
 * The service for Twitch streams.
 */
public class Xphome extends StreamService {

    /**
     * {@inheritDoc}
     */
    @Override
    public String getKey() {
        return "xphome";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getLabel() {
        return "Xphome";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String getIconUrl() {
        return "xphome.png";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getUrl() {
        return "rtmp://vacker.me/live/";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getDefaultChannel() {
        return "live";
    }

}
