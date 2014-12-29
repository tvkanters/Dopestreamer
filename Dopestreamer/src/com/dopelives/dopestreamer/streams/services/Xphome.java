package com.dopelives.dopestreamer.streams.services;

import org.json.JSONObject;

import com.dopelives.dopestreamer.streams.Quality;
import com.dopelives.dopestreamer.util.HttpHelper;

/**
 * The service for Xphome streams.
 */
public class Xphome extends StreamService {

    /** The URL where the stream stats are shown */
    private static final String STATS_URL = "http://vacker.tv/infojson.php";

    /**
     * {@inheritDoc}
     */
    @Override
    public String getKey() {
        return "xphome";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getLabel() {
        return "Vacker";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String getIconUrl() {
        return "dopestreamer_small.png";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getUrl() {
        return "rtmp://vacker.tv/";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getDefaultChannel() {
        return "live/live";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getConnectionDetails(String channel, final Quality quality) {
        if (!channel.contains("/")) {
            channel = channel + "/" + channel;
        }

        return super.getConnectionDetails(channel, quality);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isConnectPossible(final String channel) {
        final String result = HttpHelper.getContent(STATS_URL);
        if (result == null) {
            return false;
        }

        final JSONObject json = new JSONObject(result);
        final JSONObject channelInfo = json.getJSONObject(trimChannel(channel));
        if (channelInfo == null || !channelInfo.getBoolean("live")) {
            System.out.println("Vacker channel not live: " + channel);
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
        final String result = HttpHelper.getContent(STATS_URL);
        if (result == null) {
            return false;
        }

        final JSONObject json = new JSONObject(result);
        if (json.isNull(trimChannel(channel))) {
            System.out.println("Invalid Vacker channel: " + channel);
            return false;
        } else {
            return true;
        }
    }

    /**
     * Trims the channel to a non-slash variant.
     *
     * @param channel
     *            The channel to string
     *
     * @return The trimmed channel
     */
    private String trimChannel(final String channel) {
        final int index = channel.indexOf('/');
        if (index >= 0) {
            return channel.substring(0, index);
        }
        return channel;
    }

}
