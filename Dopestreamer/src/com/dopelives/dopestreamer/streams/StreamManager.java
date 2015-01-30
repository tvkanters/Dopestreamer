package com.dopelives.dopestreamer.streams;

import java.security.InvalidParameterException;
import java.util.LinkedList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import com.dopelives.dopestreamer.gui.StreamState;
import com.dopelives.dopestreamer.shell.ConsoleListener;
import com.dopelives.dopestreamer.shell.ProcessId;
import com.dopelives.dopestreamer.streams.services.StreamService;
import com.dopelives.dopestreamer.streams.services.StreamServiceManager;
import com.dopelives.dopestreamer.util.Pref;

/**
 * A manager for stream related tasks and state management.
 */
public class StreamManager implements ConsoleListener {

    /** The amount of time in milliseconds before buffering times out */
    private static final int BUFFERING_TIMEOUT = 5 * 1000;

    /** The base stream manager to use for global streams */
    private static final StreamManager sInstance = new StreamManager();

    /** The current state of the main stream */
    private StreamState mStreamState = StreamState.INACTIVE;
    /** The currently active stream */
    private Stream mStream;

    /** The timer used for buffering timeouts */
    private final Timer mBufferingTimer = new Timer();
    /** The timer task used for buffering timeouts */
    private TimerTask mBufferingTimeout;
    /** The amount of consecutive buffering attempts have been performed */
    private int mBufferingAttempts = 0;

    /** The listeners that will receive updates of stream changes */
    private final List<StreamListener> mListeners = new LinkedList<>();

    /** Whether or not the stream will autoswitch between channels */
    private boolean mAutoswitchEnabled;
    /** The index of the current autoswitch stream service */
    private static int mCurrentAutoswitchIndex = -1;
    /** The timer used to retry autoswitch services */
    private static Timer mAutoswitchTimer = new Timer();
    /** The timer task used to retry autoswitch services */
    private static TimerTask mAutoswitchTimerTask = null;

    /**
     * @return The base stream manager to use for global streams
     */
    public static StreamManager getInstance() {
        return sInstance;
    }

    /**
     * Starts a stream based on the given parameters with the default channel for the given service.
     *
     * @param streamService
     *            The service to use (e.g., Hitbox)
     * @param quality
     *            The quality of the stream
     */
    public synchronized void startStream(final StreamService streamService, final Quality quality) {
        startStream(streamService, streamService.getDefaultChannel(), quality);
    }

    /**
     * Starts a stream based on the given parameters.
     *
     * @param streamService
     *            The service to use (e.g., Hitbox)
     * @param channel
     *            The channel to start on the service
     * @param quality
     *            The quality of the stream
     *
     * @throws InvalidParameterException
     *             Thrown when the channel is invalid
     */
    public synchronized void startStream(final StreamService streamService, final String channel, final Quality quality)
            throws InvalidParameterException {
        mAutoswitchEnabled = false;

        // Clean up
        stopStreamConsole();

        // Prepare the stream
        mStream = new Stream(streamService, channel, quality);
        mStream.addListener(this);
        updateState(StreamState.CONNECTING);

        // Save the stream settings
        Pref.LAST_STREAM_SERVICE.put(streamService.getKey());
        Pref.LAST_CHANNEL.put(channel.equals(streamService.getDefaultChannel()) ? "" : channel);
        Pref.LAST_QUALITY.put(quality.toString());

        // Start the stream
        mStream.start();
    }

    /**
     * Starts a stream that automatically switches between available default channels.
     *
     * @param quality
     *            The quality of the stream
     */
    public synchronized void startAutoswitch(final Quality quality) {
        mAutoswitchEnabled = true;
        boolean delay = false;

        // Clean up
        stopStreamConsole();

        // Update state
        if (mStreamState != StreamState.WAITING) {
            updateState(StreamState.CONNECTING);
        }

        // Switch to the next stream service
        ++mCurrentAutoswitchIndex;
        if (mCurrentAutoswitchIndex == StreamServiceManager.getAutoswitchServices().size()) {
            mCurrentAutoswitchIndex = 0;
            delay = true;
            updateState(StreamState.WAITING);
        }

        // Prepare the stream
        final StreamService streamService = StreamServiceManager.getAutoswitchServices().get(mCurrentAutoswitchIndex);
        mStream = new Stream(streamService, quality);
        mStream.addListener(this);

        // Save the stream settings
        Pref.LAST_STREAM_SERVICE.put(streamService.getKey());
        Pref.LAST_CHANNEL.put("");
        Pref.LAST_QUALITY.put(quality.toString());

        // Start the stream
        if (delay) {
            mAutoswitchTimerTask = new TimerTask() {
                @Override
                public void run() {
                    mStream.start();
                }
            };
            mAutoswitchTimer.schedule(mAutoswitchTimerTask, Stream.RETRY_DELAY * 1000);

        } else {
            mStream.start();
        }
    }

    /**
     * Resets the autoswitch rotation.
     */
    public synchronized void resetAutoswitch() {
        mCurrentAutoswitchIndex = -1;
    }

    /**
     * Starts the stream based on the user preferences. Will restart the stream if it was already running.
     */
    public synchronized void restartLastStream() {
        final Quality quality = Quality.valueOf(Pref.LAST_QUALITY.getString());

        final StreamService streamService = StreamServiceManager.getStreamServiceByKey(Pref.LAST_STREAM_SERVICE
                .getString());
        final String channel = Pref.LAST_CHANNEL.getString();

        if (channel.equals("")) {
            if (Pref.AUTOSWITCH.getBoolean()) {
                resetAutoswitch();
                startAutoswitch(quality);
            } else {
                startStream(streamService, quality);
            }
        } else {
            startStream(streamService, channel, quality);
        }
    }

    /**
     * Stops the active stream and transitions to the inactive state.
     */
    public synchronized void stopStream() {
        mBufferingAttempts = 0;
        if (mAutoswitchTimerTask != null) {
            mAutoswitchTimerTask.cancel();
            mAutoswitchTimer.purge();
        }
        updateState(StreamState.INACTIVE);
        stopStreamConsole();
    }

    /**
     * {@inheritDoc}
     *
     * Handles messages from Livestreamer.
     */
    @Override
    public synchronized void onConsoleOutput(final ProcessId processId, final String output) {
        // Starting to buffer
        if (output.contains("Opening stream")) {
            updateState(StreamState.BUFFERING);
            startBufferingTimeout();

            // No stream active at the moment, so waiting for it to start
        } else if (output.contains("Waiting for streams")) {
            if (mAutoswitchEnabled) {
                // Try the next stream service
                stopStreamConsole();
                restartLastStream();

            } else {
                updateState(StreamState.WAITING);
            }

            // Invalid channel name
        } else if (output.contains("Unable to open URL") && !output.contains("Failed to open segment")
                && !output.contains("Failed to reload playlist")) {
            if (mAutoswitchEnabled) {
                // Try the next stream service
                stopStreamConsole();
                restartLastStream();

            } else {
                for (final StreamListener listener : mListeners) {
                    listener.onInvalidChannel(mStream);
                }
                stopStream();
            }

            // Invalid quality for chosen channel
        } else if (output.contains("error: The specified stream(s) '")) {
            for (final StreamListener listener : mListeners) {
                listener.onInvalidQuality(mStream);
            }
            stopStream();

            // Opening the media player
        } else if (output.contains("Starting player")) {
            stopBufferingTimeout();

            // Streaming started
        } else if (output.contains("Writing stream")) {
            updateState(StreamState.ACTIVE);

            // Media player not found
        } else if (output.contains("Failed to start player")
                || output.contains("The default player (VLC) does not seem to be installed.")) {
            stopStream();
            for (final StreamListener listener : mListeners) {
                listener.onInvalidMediaPlayer(mStream);
            }

            // Livestreamer is outdated
        } else if (output.contains("livestreamer: error: unrecognized arguments")) {
            stopStream();
            for (final StreamListener listener : mListeners) {
                listener.onInvalidLivestreamer(mStream);
            }
        }

    }

    /**
     * {@inheritDoc}
     *
     * Indicates Livestreamer being closed.
     */
    @Override
    public synchronized void onConsoleStop(final ProcessId processId) {
        switch (mStreamState) {
            case INACTIVE:
                break;

            case ACTIVE:
                // In game mode, don't attempt to restart the stream
                if (Pref.GAME_MODE.getBoolean()) {
                    stopStream();
                    break;
                }

                // Fall-through

            case CONNECTING:
            case WAITING:
            case BUFFERING:
                // The user didn't cancel streaming, so try starting the stream again
                if (mStream != null && processId != null && processId.equals(mStream.getProcessId())) {
                    System.out.println("Restarting stream");
                    restartLastStream();
                }
                break;

            default:
                throw new IllegalStateException("Unknown state: " + mStreamState);
        }
    }

    /**
     * Transitions to the given state and updates GUI components.
     *
     * @param newState
     *            The stream state to transition to
     */
    public synchronized void updateState(final StreamState newState) {
        if (newState == mStreamState) {
            return;
        }

        stopBufferingTimeout();

        final StreamState oldState = mStreamState;
        mStreamState = newState;

        for (final StreamListener listener : mListeners) {
            listener.onStateUpdated(this, oldState, newState);
        }
    }

    /**
     * Stops the console of the stream, if it was opened.
     */
    private synchronized void stopStreamConsole() {
        if (mStream != null) {
            mStream.stop();
            mStream = null;
        }
    }

    /**
     * Starts the timeout for buffering. Will try to restart the stream after the timeout.
     */
    private synchronized void startBufferingTimeout() {
        mBufferingTimeout = new TimerTask() {
            @Override
            public void run() {
                stopStreamConsole();
                restartLastStream();
            }
        };
        mBufferingTimer.schedule(mBufferingTimeout, ++mBufferingAttempts * BUFFERING_TIMEOUT);
    }

    /**
     * Stops the buffering timeout, preventing it from restarting the stream.
     */
    private synchronized void stopBufferingTimeout() {
        if (mBufferingTimeout != null) {
            mBufferingTimeout.cancel();
            mBufferingTimer.purge();
        }
    }

    /**
     * @return The current state of the stream within this manager
     */
    public StreamState getStreamState() {
        return mStreamState;
    }

    /**
     * Adds a listener that will be informed of any stream related changes.
     *
     * @param listener
     *            The listener to receive updates
     */
    public void addListener(final StreamListener listener) {
        mListeners.add(listener);
    }

    /**
     * @return The service currently used to stream
     */
    public StreamService getCurrentStreamService() {
        return (mStream != null ? mStream.getStreamService() : null);
    }

}
