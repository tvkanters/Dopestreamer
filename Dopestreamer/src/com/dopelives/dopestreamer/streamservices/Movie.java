package com.dopelives.dopestreamer.streamservices;

/**
 * The service for movie streams.
 */
public class Movie extends Hitbox {

    /**
     * {@inheritDoc}
     */
    @Override
    public String getKey() {
        return "movie";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getLabel() {
        return "Movie";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String getIconUrl() {
        return "movie.png";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getDefaultChannel() {
        return "nothing2seehere";
    }

}
