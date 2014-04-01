package com.dopelives.dopestreamer.streams.services;

import com.dopelives.dopestreamer.streams.StreamService;

/**
 * The service for Afreeca streams.
 */
public class Afreeca extends StreamService {

    /**
     * {@inheritDoc}
     */
    @Override
    public String getKey() {
        return "afreeca";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getLabel() {
        return "Afreeca";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String getIconUrl() {
        return "afreeca.png";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getUrl() {
        return "afreeca.com/";
    }

}