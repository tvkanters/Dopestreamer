package com.dopelives.dopestreamer.streams;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.Charset;
import java.util.LinkedList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.dopelives.dopestreamer.Audio;
import com.dopelives.dopestreamer.Pref;
import com.dopelives.dopestreamer.gui.StreamState;

/**
 * A static helper class for getting stream info from the IRC topic.
 */
public class StreamInfo {

    /** The URL at which to get the stream info */
    private static final String INFO_URL = "http://goalitium.kapsi.fi/dopelives_status";

    /** The amount of time in milliseconds before buffering times out */
    private static final int REQUEST_INTERVAL = 5 * 1000;

    /** The pattern used to match active streams */
    private static final Pattern sTopicPattern = Pattern.compile("^(.+) is playing (.+)$");

    /** Whether or not the initial update is processing */
    private static boolean initialUpdate = true;
    /** Whether or not a stream is currently active */
    private static boolean sStreamActive = false;
    /** The last detected streamer */
    private static String sStreamer;
    /** The last detected game */
    private static String sGame;

    /** The timer used for request intervals */
    private static final Timer sRequestTimer = new Timer();
    /** The timer task used for request intervals */
    private static TimerTask sRequestInterval;

    /** The listeners that will receive updates of stream info changes */
    private static final List<StreamInfoListener> sListeners = new LinkedList<>();

    /** The updater performing the HTTP request to get the latest stream info */
    private static final Runnable sUpdater = new Runnable() {
        @Override
        public void run() {
            try {
                // Check the newest stream info
                final URLConnection connection = new URL(INFO_URL).openConnection();
                final BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream(),
                        Charset.forName("UTF-8")));
                final String result = reader.readLine();
                reader.close();

                if (result != null) {
                    final Matcher matcher = sTopicPattern.matcher(result.trim());
                    if (matcher.find()) {
                        // Stream info found, see if it needs to be updated
                        final String streamer = matcher.group(1);
                        final String game = matcher.group(2);

                        if (!sStreamActive || !sStreamer.equals(streamer) || !sGame.equals(game)) {
                            sStreamActive = true;
                            sStreamer = streamer;
                            sGame = game;

                            // Notify all listeners of a change in stream info
                            for (final StreamInfoListener listener : sListeners) {
                                listener.onStreamInfoUpdated(sStreamer, sGame);
                            }

                            // Notify the user if he wants and if a stream isn't already active
                            if (Pref.NOTIFICATIONS.getBoolean()
                                    && StreamManager.getInstance().getStreamState() == StreamState.INACTIVE
                                    && !initialUpdate) {
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
                }

                initialUpdate = false;

            } catch (final IOException ex) {
                System.out.println("Couldn't fetch stream info");
            }
        }
    };

    /**
     * Performs an HTTP request to get the latest stream info.
     */
    private static void executeRefresh() {
        new Thread(sUpdater).start();
    }

    /**
     * Refreshes the latest stream info. If an interval is active, its timer will reset.
     */
    public synchronized static void requestRefresh() {
        if (sRequestInterval != null) {
            startRequestInterval();
        } else {
            executeRefresh();
        }
    }

    /**
     * Sets an interval to periodically refresh the latest stream info. If an interval is already active, its timer will
     * reset.
     */
    public synchronized static void startRequestInterval() {
        stopRequestInterval();

        sRequestInterval = new TimerTask() {
            @Override
            public void run() {
                executeRefresh();
            }
        };

        sRequestTimer.schedule(sRequestInterval, 0, REQUEST_INTERVAL);
    }

    /**
     * Stops the interval to periodically refresh the latest stream info.
     */
    public synchronized static void stopRequestInterval() {
        if (sRequestInterval != null) {
            sRequestInterval.cancel();
            sRequestInterval = null;

            sRequestTimer.purge();
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
         * @param game
         *            The game being streamed
         */
        void onStreamInfoUpdated(String streamer, String game);

        /**
         * Called when a streamer has stopped.
         */
        void onStreamInfoRemoved();
    }
}
