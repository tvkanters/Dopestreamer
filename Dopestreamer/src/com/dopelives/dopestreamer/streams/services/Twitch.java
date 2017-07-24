package com.dopelives.dopestreamer.streams.services;

import java.util.LinkedList;
import java.util.List;

import com.dopelives.dopestreamer.streams.Quality;

/**
 * The service for Twitch streams.
 */
public class Twitch extends StreamService {

    /**
     * {@inheritDoc}
     */
    @Override
    public String getKey() {
        return "twitch";
    }

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
    public String getStreamServiceIconUrl() {
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
    protected List<Quality> getServiceSpecificQualities() {
        final List<Quality> qualities = new LinkedList<>();
        qualities.add(Quality.P72060);
        qualities.add(Quality.P720);
        qualities.add(Quality.P480);
        qualities.add(Quality.P360);
        return qualities;
    }

}
