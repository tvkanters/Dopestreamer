package com.dopelives.dopestreamer.streamservices;

import com.dopelives.dopestreamer.streams.StreamService;

/**
 * The service for Cast3d streams.
 */
public class Cast3d extends StreamService {

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
    protected String getIconUrl() {
        return "noimage.png";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getUrl() {
        return "cast3d.tv/channel/";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getDefaultChannel() {
        return "dopelives";
    }

}