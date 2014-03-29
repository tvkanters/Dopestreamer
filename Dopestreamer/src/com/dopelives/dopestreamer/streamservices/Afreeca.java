package com.dopelives.dopestreamer.streamservices;

/**
 * The service for Afreeca streams.
 */
public class Afreeca extends StreamService {

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
        return "noimage.png";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getUrl() {
        return "afreeca.com/";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getDefaultChannel() {
        return "kimjye";
    }

}