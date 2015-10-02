package com.dopelives.dopestreamer.streams.services;

import java.util.LinkedList;
import java.util.List;

import com.dopelives.dopestreamer.streams.Quality;

/**
 * The service for Ustream streams.
 */
public class Ustream extends StreamService {

    /**
     * {@inheritDoc}
     */
    @Override
    public String getKey() {
        return "ustream";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getLabel() {
        return "Ustream";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getIconUrl() {
        return getDisabledIconUrl("services/ustream.png");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getUrl() {
        return "ustream.tv/channel/";
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
