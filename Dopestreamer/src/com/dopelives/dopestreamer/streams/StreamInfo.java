package com.dopelives.dopestreamer.streams;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ScheduledFuture;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.JSONObject;

import com.dopelives.dopestreamer.gui.StreamState;
import com.dopelives.dopestreamer.streams.services.Vacker;
import com.dopelives.dopestreamer.util.Audio;
import com.dopelives.dopestreamer.util.Executor;
import com.dopelives.dopestreamer.util.HttpHelper;
import com.dopelives.dopestreamer.util.Pref;

/**
 * A static helper class for getting stream info from the IRC topic.
 */
public class StreamInfo {

    /** The URL at which to get the topic info */
    private static final String URL_TOPIC = "http://goalitium.kapsi.fi/dopelives_status2";
    /** The URL at which to get the Hitbox info */
    private static final String URL_HITBOX = "http://api.hitbox.tv/media/live/dopefish";
    /** The URL at which to get the Twitch info */
    private static final String URL_TWITCH = "https://api.twitch.tv/kraken/streams/dopelives";

    /** The amount of time in milliseconds before between each topic request */
    private static final int REQUEST_INTERVAL_TOPIC = 5 * 1000;
    /** The amount of time in milliseconds before between each topic request */
    private static final int REQUEST_INTERVAL_VIEWER_COUNT = 15 * 1000;

    /** The pattern used to match active streams */
    private static final Pattern sTopicParser = Pattern.compile("^(.+)\n([^:]+): ?(.+)$");

    /** Whether or not the initial update is processing */
    private static boolean sInitialUpdate = true;
    /** Whether or not a stream is currently active */
    private static boolean sStreamActive = false;
    /** The last detected streamer */
    private static String sStreamer;
    /** The last detected stream type */
    private static String sType;
    /** The last detected game */
    private static String sGame;

    /** The amount of viewers on Vacker */
    private static int sViewersVacker = 0;
    /** The amount of viewers on Hitbox */
    private static int sViewersHitbox = 0;
    /** The amount of viewers on Twitch */
    private static int sViewersTwitch = 0;

    /** The task used for request topic intervals */
    private static ScheduledFuture<?> sRequestTopicTask;
    /** The task used for request viewer count intervals */
    private static ScheduledFuture<?> sRequestViewerCountTask;

    /** The listeners that will receive updates of stream info changes */
    private static final List<StreamInfoListener> sListeners = new LinkedList<>();

    /** The updater performing the HTTP request to get the latest topic info */
    private static final Runnable sTopicUpdater = new Runnable() {
        @Override
        public void run() {
            // Check the newest stream info
            final String result = HttpHelper.getContent(URL_TOPIC);
            if (result != null) {
                final Matcher matcher = sTopicParser.matcher(result.trim());
                if (matcher.find()) {
                    // Stream info found, see if it needs to be updated
                    final String streamer = matcher.group(1);
                    final String type = matcher.group(2);
                    final String game = matcher.group(3);

                    if (!sStreamActive || !sStreamer.equals(streamer) || !sType.equals(type) || !sGame.equals(game)) {
                        sStreamActive = true;
                        sStreamer = streamer;
                        sType = type;
                        sGame = game;

                        // Notify all listeners of a change in stream info
                        for (final StreamInfoListener listener : sListeners) {
                            listener.onStreamInfoUpdated(sStreamer, sType, sGame);
                        }

                        // Notify the user if he wants and if a stream isn't already active
                        if (Pref.NOTIFICATIONS.getBoolean()
                                && StreamManager.getInstance().getStreamState() == StreamState.INACTIVE
                                && !sInitialUpdate) {
                            Audio.playNotification();
                        }
                    }

                } else {
                    // No stream info found
                    if (sStreamActive) {
                        sStreamActive = false;

                        for (final StreamInfoListener listener : sListeners) {
                            listener.onStreamInfoRemoved();
                        }
                    }
                }

                sInitialUpdate = false;
            }
        }
    };

    /** The updater performing the HTTP request to get the latest Vacker info */
    private static final Runnable sVackerUpdater = new Runnable() {

        /** The channels to sum the viewers of */
        private final String[] mChannels = { "live", "live_low" };

        @Override
        public void run() {
            int viewerCount = 0;

            // Retrieve the viewer count for all Vacker servers
            for (final Vacker.Server server : Vacker.Server.values()) {
                // Check the newest stream info
                final String result = HttpHelper.getContent(server.getStatsUrl());

                // Only update the Vacker viewer count if all requests were successful
                if (result == null) {
                    return;
                }

                final JSONObject json = new JSONObject(result);

                // Sum up the viewers of all relevant channels
                for (final String channel : mChannels) {
                    final JSONObject channelInfo = json.getJSONObject(channel);
                    if (channelInfo.has("viewers")) {
                        viewerCount += channelInfo.getInt("viewers");
                    }
                }
            }

            // If the viewer count changed, update it
            if (viewerCount != sViewersVacker) {
                sViewersVacker = viewerCount;
                updateViewerCount();
            }
        }
    };

    /** The updater performing the HTTP request to get the latest Hitbox info */
    private static final Runnable sHitboxUpdater = new Runnable() {
        @Override
        public void run() {
            // Check the newest stream info
            final String result = HttpHelper.getContent(URL_HITBOX);
            if (result != null) {
                final JSONObject json = new JSONObject(result).getJSONArray("livestream").getJSONObject(0);

                // Viewer count is -2 by default to account for the restream viewers
                int viewerCount = -2;

                // Only add the Hitbox viewer count if it's live
                if (json.getInt("media_is_live") == 1) {
                    viewerCount += json.getInt("media_views");
                }

                // If the viewer count changed, update it
                viewerCount = Math.max(viewerCount, 0);
                if (viewerCount != sViewersHitbox) {
                    sViewersHitbox = viewerCount;
                    updateViewerCount();
                }
            }
        }
    };

    /** The updater performing the HTTP request to get the latest Twitch info */
    private static final Runnable sTwitchUpdater = new Runnable() {
        @Override
        public void run() {
            // Check the newest stream info
            final String result = HttpHelper.getContent(URL_TWITCH);
            if (result != null) {
                final JSONObject json = new JSONObject(result);

                int viewerCount = 0;

                // Only add the Twitch viewer count if it's live
                if (!json.isNull("stream")) {
                    viewerCount += json.getJSONObject("stream").getInt("viewers");
                }

                // If the viewer count changed, update it
                if (viewerCount != sViewersTwitch) {
                    sViewersTwitch = viewerCount;
                    updateViewerCount();
                }
            }
        }
    };

    /**
     * Updates the topic info.
     */
    private static void executeTopicRefresh() {
        Executor.execute(sTopicUpdater);
    }

    /**
     * Updates the viewer counts.
     */
    private static void executeViewerCountRefresh() {
        Executor.execute(sVackerUpdater);
        Executor.execute(sHitboxUpdater);
        Executor.execute(sTwitchUpdater);
    }

    /**
     * Refreshes the latest stream info. If an interval is active, its timer will reset.
     */
    public synchronized static void requestRefresh() {
        if (sRequestTopicTask != null) {
            startRequestInterval();
        } else {
            executeTopicRefresh();
            executeViewerCountRefresh();
        }
    }

    /**
     * Sets an interval to periodically refresh the latest stream info. If an interval is already active, its timer will
     * reset.
     */
    public synchronized static void startRequestInterval() {
        // Stop any current refresh task
        stopRequestInterval();

        // Create the tasks
        sRequestTopicTask = Executor.scheduleInterval(() -> {
            executeTopicRefresh();
        }, 0, REQUEST_INTERVAL_TOPIC);

        sRequestViewerCountTask = Executor.scheduleInterval(() -> {
            executeViewerCountRefresh();
        }, 0, REQUEST_INTERVAL_VIEWER_COUNT);
    }

    /**
     * Stops the interval to periodically refresh the latest stream info.
     */
    public synchronized static void stopRequestInterval() {
        if (sRequestTopicTask != null) {
            sRequestTopicTask.cancel(false);
            sRequestViewerCountTask.cancel(false);

            sRequestTopicTask = null;
            sRequestViewerCountTask = null;
        }
    }

    /**
     * Informs the listeners of an updated viewer count.
     */
    private static synchronized void updateViewerCount() {
        final int viewerCount = sViewersVacker + sViewersHitbox + sViewersTwitch;

        for (final StreamInfoListener listener : sListeners) {
            listener.onViewerCountUpdated(viewerCount);
        }
    }

    /**
     * Adds a listener that will be informed of any stream info update.
     *
     * @param listener
     *            The listener to receive updates
     */
    public static void addListener(final StreamInfoListener listener) {
        sListeners.add(listener);
    }

    /**
     * This is a static-only class.
     */
    private StreamInfo() {}

    /**
     * The interface for receiving updates of stream info changes.
     */
    public interface StreamInfoListener {

        /**
         * Called when the stream info is updated for an active stream.
         *
         * @param streamer
         *            The streamer
         * @param type
         *            The type of stream
         * @param game
         *            The game being streamed
         */
        void onStreamInfoUpdated(String streamer, String type, String game);

        /**
         * Called when a streamer has stopped.
         */
        void onStreamInfoRemoved();

        /**
         * Called when the viewer count is updated.
         *
         * @param viewerCount
         *            The new amount of viewers
         */
        void onViewerCountUpdated(int viewerCount);
    }
}
