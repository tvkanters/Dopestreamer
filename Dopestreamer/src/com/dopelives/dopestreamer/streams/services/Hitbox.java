package com.dopelives.dopestreamer.streams.services;

import java.util.LinkedList;
import java.util.List;

import com.dopelives.dopestreamer.streams.Quality;

/**
 * The service for Hitbox streams.
 */
public class Hitbox extends StreamService {

    /**
     * {@inheritDoc}
     */
    @Override
    public String getKey() {
        return "hitbox";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getLabel() {
        return "Hitbox";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getIconUrl() {
        return getDisabledIconUrl("services/hitbox.png");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getUrl() {
        return "hitbox.tv/";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getDefaultChannel() {
        return "dopefish";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected List<Quality> getServiceSpecificQualities() {
        final List<Quality> qualities = new LinkedList<>();
        qualities.add(Quality.P720);
        qualities.add(Quality.P480);
        qualities.add(Quality.P360);
        return qualities;
    }

}
