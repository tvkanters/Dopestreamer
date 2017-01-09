package com.dopelives.dopestreamer.streams.services;

/**
 * The service for Beam.pro streams.
 */
public class BeamPro extends StreamService {

    /**
     * {@inheritDoc}
     */
    @Override
    public String getKey() {
        return "beampro";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getLabel() {
        return "Beam";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getStreamServiceIconUrl() {
        return "beampro.png";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getUrl() {
        return "beam.pro/";
    }

}
