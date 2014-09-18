package com.dopelives.dopestreamer.streams.services;

import com.dopelives.dopestreamer.streams.Quality;

/**
 * The service for Xphome streams.
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
        return "Vacker";
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
        return "rtmp://vacker.tv/";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getDefaultChannel() {
        return "live/live";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getConnectionDetails(String channel, final Quality quality) {
        if (!channel.contains("/")) {
            channel = channel + "/" + channel;
        }

        return super.getConnectionDetails(channel, quality);
    }

}
