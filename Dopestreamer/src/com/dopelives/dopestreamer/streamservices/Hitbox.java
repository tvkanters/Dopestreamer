package com.dopelives.dopestreamer.streamservices;

/**
 * The service for Hitbox streams.
 */
public class Hitbox extends StreamService {

    @Override
    public String getLabel() {
        return "Hitbox";
    }

    @Override
    protected String getIconUrl() {
        return "hitbox.png";
    }

    @Override
    public String getUrl() {
        return "hitbox.tv/";
    }

    @Override
    public String getDefaultChannel() {
        return "dopefish";
    }

}
