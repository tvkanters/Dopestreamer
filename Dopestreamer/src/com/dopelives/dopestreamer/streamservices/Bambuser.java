package com.dopelives.dopestreamer.streamservices;

/**
 * The service for Bambuser streams.
 */
public class Bambuser extends StreamService {

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
    protected String getIconUrl() {
        return "bambuser.png";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getUrl() {
        return "bambuser.com/v/";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getDefaultChannel() {
        return "dopelives";
    }

}