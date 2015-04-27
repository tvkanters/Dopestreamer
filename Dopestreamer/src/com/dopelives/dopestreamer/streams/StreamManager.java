package com.dopelives.dopestreamer.streams;

import java.security.InvalidParameterException;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ScheduledFuture;

import com.dopelives.dopestreamer.gui.StreamState;
import com.dopelives.dopestreamer.shell.ConsoleListener;
import com.dopelives.dopestreamer.shell.ProcessId;
import com.dopelives.dopestreamer.streams.services.StreamService;
import com.dopelives.dopestreamer.streams.services.StreamServiceManager;
import com.dopelives.dopestreamer.util.Executor;
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

    /** The task used for buffering timeouts */
    private ScheduledFuture<?> mBufferingTimeout;
    /** The amount of consecutive buffering attempts have been performed */
    private int mBufferingAttempts = 0;

    /** The listeners that will receive updates of stream changes */
    private final List<StreamListener> mListeners = new LinkedList<>();

    /** Whether or not the stream will autoswitch between channels */
    private boolean mAutoswitchEnabled;
    /** The index of the current autoswitch stream service */
    private int mCurrentAutoswitchIndex = -1;
    /** The task used to retry autoswitch services */
    private ScheduledFuture<?> mAutoswitchDelay = null;

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
            mAutoswitchDelay = Executor.schedule(() -> {
                mStream.start();
                mAutoswitchDelay = null;
            }, Stream.RETRY_DELAY * 1000);

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
        updateState(StreamState.INACTIVE);
        stopStreamConsole();
    }

    /**
     * Stops the console of the stream, if it was opened.
     */
    private synchronized void stopStreamConsole() {
        if (mAutoswitchDelay != null) {
            mAutoswitchDelay.cancel(false);
            mAutoswitchDelay = null;
        }

        if (mStream != null) {
            mStream.stop();
            mStream = null;
        }
    }

    /**
     * {@inheritDoc}
     *
     * Handles messages from Livestreamer.
     */
    @Override
    public synchronized void onConsoleOutput(final ProcessId processId, final String output) {
        // Only listen to output of the current stream
        if (!isCurrentStream(processId)) {
            return;
        }

        // Starting to buffer
        if (output.contains("Opening stream")) {
            updateState(StreamState.BUFFERING);
            startBufferingTimeout();
            resetAutoswitch();

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
        } else if (output.contains("404 Client Error: Not Found")) {
            mListeners.forEach(l -> l.onInvalidChannel(mStream));
            stopStream();

            // Invalid quality for chosen channel
        } else if (output.contains("error: The specified stream(s) '")) {
            mListeners.forEach(l -> l.onInvalidQuality(mStream));
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
            mListeners.forEach(l -> l.onInvalidMediaPlayer(mStream));

            // Livestreamer is outdated
        } else if (output.contains("livestreamer: error: unrecognized arguments")) {
            stopStream();
            mListeners.forEach(l -> l.onInvalidLivestreamer(mStream));

            // Livestreamer isn't found
        } else if (output.contains(" is not recognized as an internal or external command")) {
            stopStream();
            mListeners.forEach(l -> l.onLivestreamerNotFound(mStream));

            // RTMPDump isn't found
        } else if (output.contains("Unable to find") && output.contains("rtmpdump")) {
            stopStream();
            mListeners.forEach(l -> l.onRtmpDumpNotFound(mStream));
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
                if (isCurrentStream(processId)) {
                    System.out.println("Restarting stream");
                    restartLastStream();
                }
                break;

            default:
                throw new IllegalStateException("Unknown state: " + mStreamState);
        }
    }

    /**
     * Checks if a given process ID belongs to the currently active stream.
     *
     * @param processId
     *            The process ID to check
     *
     * @return True if there is a stream active and its process ID matches
     */
    private synchronized boolean isCurrentStream(final ProcessId processId) {
        return mStream != null && processId != null && processId.equals(mStream.getProcessId());
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

        mListeners.forEach(l -> l.onStateUpdated(this, oldState, newState));
    }

    /**
     * Starts the timeout for buffering. Will try to restart the stream after the timeout.
     */
    private synchronized void startBufferingTimeout() {
        mBufferingTimeout = Executor.schedule(() -> {
            stopStreamConsole();
            restartLastStream();
        }, ++mBufferingAttempts * BUFFERING_TIMEOUT);
    }

    /**
     * Stops the buffering timeout, preventing it from restarting the stream.
     */
    private synchronized void stopBufferingTimeout() {
        if (mBufferingTimeout != null) {
            mBufferingTimeout.cancel(false);
            mBufferingTimeout = null;
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

    /**
     * This is a singleton.
     */
    private StreamManager() {}

}
