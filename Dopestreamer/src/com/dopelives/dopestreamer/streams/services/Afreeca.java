package com.dopelives.dopestreamer.streams.services;

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
    public String getIconUrl() {
        return "services/afreeca.png";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getUrl() {
        return "afreeca.com/";
    }

}