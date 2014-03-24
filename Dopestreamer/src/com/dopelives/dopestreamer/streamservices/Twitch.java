package com.dopelives.dopestreamer.streamservices;

/**
 * The service for Twitch streams.
 */
public class Twitch extends StreamService {

    /**
     * {@inheritDoc}
     */
    @Override
    public String getLabel() {
        return "Twitch";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String getIconUrl() {
        return "twitch.png";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getUrl() {
        return "twitch.tv/";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getDefaultChannel() {
        return "dopelives";
    }

}
