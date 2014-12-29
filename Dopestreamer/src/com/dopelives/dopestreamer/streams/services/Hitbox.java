package com.dopelives.dopestreamer.streams.services;

import java.util.LinkedList;
import java.util.List;

import org.json.JSONObject;

import com.dopelives.dopestreamer.streams.Quality;
import com.dopelives.dopestreamer.util.HttpHelper;

/**
 * The service for Hitbox streams.
 */
public class Hitbox extends StreamService {

    /** The URL where the stream stats are shown, append the channel */
    private static final String STATS_URL = "http://api.hitbox.tv/user/";

    /**
     * {@inheritDoc}
     */
    @Override
    public String getKey() {
        return "hitbox";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getLabel() {
        return "Hitbox";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String getIconUrl() {
        return "hitbox.png";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getUrl() {
        return "hitbox.tv/";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getDefaultChannel() {
        return "dopefish";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected List<Quality> getServiceSpecificQualities() {
        final List<Quality> qualities = new LinkedList<>();
        qualities.add(Quality.P720);
        qualities.add(Quality.P480);
        qualities.add(Quality.P360);
        return qualities;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isConnectPossible(final String channel) {
        final String result = HttpHelper.getContent(STATS_URL + channel).trim();
        if (result == null || result.equals("[]")) {
            return false;
        }

        final JSONObject json = new JSONObject(result);
        if (json.getInt("is_live") != 1) {
            System.out.println("Hitbox channel not live: " + channel);
            return false;
        } else {
            return true;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isChannelPossible(final String channel) {
        final String result = HttpHelper.getContent(STATS_URL + channel).trim();
        if (result == null || result.equals("[]")) {
            System.out.println("Invalid Hitbox channel: " + channel);
            return false;
        }

        return true;
    }

}
