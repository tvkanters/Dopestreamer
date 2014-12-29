package com.dopelives.dopestreamer.streams.services;

import java.util.LinkedList;
import java.util.List;

import org.json.JSONObject;

import com.dopelives.dopestreamer.streams.Quality;
import com.dopelives.dopestreamer.util.HttpHelper;

/**
 * The service for Twitch streams.
 */
public class Twitch extends StreamService {

    /** The URL where the stream stats are shown, append the channel */
    private static final String STATS_URL = "https://api.twitch.tv/kraken/streams/";

    /**
     * {@inheritDoc}
     */
    @Override
    public String getKey() {
        return "twitch";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getLabel() {
        return "Twitch";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String getIconUrl() {
        return "twitch.png";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getUrl() {
        return "twitch.tv/";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getDefaultChannel() {
        return "dopelives";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected List<Quality> getServiceSpecificQualities() {
        final List<Quality> qualities = new LinkedList<>();
        qualities.add(Quality.HIGH);
        qualities.add(Quality.MEDIUM);
        qualities.add(Quality.LOW);
        return qualities;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isConnectPossible(final String channel) {
        final String result = HttpHelper.getContent(STATS_URL + channel);
        if (result == null) {
            return false;
        }

        final JSONObject json = new JSONObject(result);
        return !json.isNull("stream");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isChannelPossible(final String channel) {
        final String result = HttpHelper.getContent(STATS_URL + channel);
        if (result == null) {
            return false;
        }

        final JSONObject json = new JSONObject(result);
        return json.has("stream");
    }

}
