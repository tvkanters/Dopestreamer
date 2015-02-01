package com.dopelives.dopestreamer.streams.services;

/**
 * The service for Bambuser streams.
 */
public class Bambuser extends StreamService {

    /**
     * {@inheritDoc}
     */
    @Override
    public String getKey() {
        return "bambuser";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getLabel() {
        return "Bambuser";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getIconUrl() {
        return "services/bambuser.png";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getUrl() {
        return "bambuser.com/v/";
    }

}