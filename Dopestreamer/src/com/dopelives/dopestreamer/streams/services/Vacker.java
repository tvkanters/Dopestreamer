package com.dopelives.dopestreamer.streams.services;

import java.security.InvalidParameterException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.JSONObject;

import com.dopelives.dopestreamer.gui.combobox.ComboBoxItem;
import com.dopelives.dopestreamer.streams.Quality;
import com.dopelives.dopestreamer.util.HttpHelper;
import com.dopelives.dopestreamer.util.Pref;

/**
 * The service for Vacker streams.
 */
public class Vacker extends StreamService {

    /**
     * The servers available on Vacker
     */
    public enum Server implements ComboBoxItem {

        DE("de", "Germany"),
        NL("nl", "The Netherlands"),
        US("us", "United States");

        /** The key for this server */
        private final String mKey;
        /** The label to show for this server */
        private final String mLabel;

        /**
         * Creates a new server mapped by the given key.
         *
         * @param key
         *            The key for this server
         * @param label
         *            The label to show for this server
         */
        private Server(final String key, final String label) {
            mKey = key;
            mLabel = label;
        }

        /**
         * @return The key for this server
         */
        public String getKey() {
            return mKey;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public String getLabel() {
            return mLabel;
        }

        /**
         * @return The base URL for the server, in the format {country}.vacker.tv/
         */
        public String getUrl() {
            return mKey + ".vacker.tv/";
        }

        /**
         * @return The URL for the server info, in the format http://{country}.vacker.tv/json.php
         */
        public String getStatsUrl() {
            return "http://" + getUrl() + "json.php";
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public String getIconUrl() {
            return "countries/" + mKey + ".png";
        }

        /**
         * @return The selected Vacker server or a default one
         */
        public static Server getSelected() {
            // Try to find the server selected by the user
            final String key = Pref.VACKER_SERVER.getString();
            for (final Server server : values()) {
                if (server.getKey().equals(key)) {
                    return server;
                }
            }

            // If the key wasn't found, return a default server
            return NL;
        }
    }

    /** A regex for parsing Vacker channels */
    private static final Pattern sChannelParser = Pattern.compile("^([a-z]+)(_low)?(/[a-z]+(_low)?)?$",
            Pattern.CASE_INSENSITIVE);

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
    public String getStreamServiceIconUrl() {
        return "vacker.png";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getUrl() {
        return "rtmp://" + Server.getSelected().getUrl();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getDefaultChannel() {
        return "live";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getConnectionDetails(final String channel, final Quality quality) {
        String parsedChannel = parseChannel(channel);
        if (parsedChannel == null) {
            throw new InvalidParameterException("Invalid channel: " + channel);
        }

        if (quality == Quality.WORST) {
            switch (parsedChannel) {
                case "live":
                case "restream":
                    parsedChannel += "_low";
                    break;

                default:
                    // Only live and restream have low quality versions
            }
        }

        parsedChannel += "/" + parsedChannel;

        return super.getConnectionDetails(parsedChannel, quality);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isConnectPossible(final String channel) {
        final String result = HttpHelper.getContent(Server.getSelected().getStatsUrl());
        if (result == null) {
            return false;
        }

        final JSONObject json = new JSONObject(result);
        final JSONObject channelInfo = json.getJSONObject(parseChannel(channel));
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
        final String result = HttpHelper.getContent(Server.getSelected().getStatsUrl());
        if (result == null) {
            return false;
        }

        final JSONObject json = new JSONObject(result);
        if (json.isNull(parseChannel(channel))) {
            System.out.println("Invalid Vacker channel: " + channel);
            return false;
        } else {
            return true;
        }
    }

    /**
     * Parses the channel to retrieve a non-slash variant without quality suffix.
     *
     * @param channel
     *            The channel to parse
     *
     * @return The parsed channel name or null if the channel was invalid
     */
    private String parseChannel(final String channel) {
        final Matcher matcher = sChannelParser.matcher(channel);
        if (!matcher.find()) {
            return null;
        }

        return matcher.group(1);
    }

}
