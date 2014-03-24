package com.dopelives.dopestreamer.streamservices;

/**
 * The service for movie streams.
 */
public class Movie extends Hitbox {

    @Override
    public String getLabel() {
        return "Movie";
    }

    @Override
    protected String getIconUrl() {
        return "movie.png";
    }

    @Override
    public String getDefaultChannel() {
        return "nothing2seehere";
    }

}
