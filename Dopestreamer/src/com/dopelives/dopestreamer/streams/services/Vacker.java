package com.dopelives.dopestreamer.streams.services;

import org.json.JSONObject;

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
    public enum Server {

        DE("de", "Germany"),
        NL("nl", "The Netherlands");

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
         * @return The label to show for this server
         */
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
         * @return The URL for the icon to show next to this server's label, relative to the image path
         */
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

    /** The page where the stream statistics are shown, relative to the server URL */
    private static final String STATS_PAGE = "json.php";

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
    public String getIconUrl() {
        return "services/vacker.png";
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
        return "live/live";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getConnectionDetails(String channel, final Quality quality) {
        // TODO: Add support for live_low

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
        final String result = HttpHelper.getContent(getStatsUrl());
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
        final String result = HttpHelper.getContent(getStatsUrl());
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

    /**
     * @return The URL containing the server statistics
     */
    private String getStatsUrl() {
        return "http://" + Server.getSelected().getUrl() + STATS_PAGE;
    }

}
