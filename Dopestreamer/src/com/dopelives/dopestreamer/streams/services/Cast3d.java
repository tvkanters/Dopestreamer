package com.dopelives.dopestreamer.streams.services;


/**
 * The service for Cast3d streams.
 */
public class Cast3d extends StreamService {

    /**
     * {@inheritDoc}
     */
    @Override
    public String getKey() {
        return "cast3d";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getLabel() {
        return "Cast3d";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getIconUrl() {
        return "noimage.png";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getUrl() {
        return "cast3d.tv/channel/";
    }

}