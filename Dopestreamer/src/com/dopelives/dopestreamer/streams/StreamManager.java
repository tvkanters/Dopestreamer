package com.dopelives.dopestreamer.streams;

import java.security.InvalidParameterException;
import java.util.LinkedList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import com.dopelives.dopestreamer.Pref;
import com.dopelives.dopestreamer.gui.StreamState;
import com.dopelives.dopestreamer.shell.ConsoleListener;
import com.dopelives.dopestreamer.shell.ProcessId;

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

    /** The listeners that will receive updates of stream changes */
    private final List<StreamListener> mListeners = new LinkedList<>();

    /**
     * @return The base stream manager to use for global streams
     */
    public static StreamManager getInstance() {
        return sInstance;
    }

    /**
     * Starts a stream based on the given parameters.
     *
     * @param stream
     *            The stream to start
     *
     * @throws InvalidParameterException
     *             Thrown when the channel is invalid
     */
    private synchronized void startStream(final Stream stream) throws InvalidParameterException {

        // Clean up
        stopStreamConsole();
        mStream = stream;

        // Start the stream
        updateState(StreamState.CONNECTING);

        mStream.addListener(this);
        mStream.start();

        // Save the stream settings
        final StreamService streamService = mStream.getStreamService();
        String channel = mStream.getChannel();
        if (streamService.getDefaultChannel().equals(channel)) {
            channel = "";
        }
        Pref.LAST_STREAM_SERVICE.put(streamService.getKey());
        Pref.LAST_CHANNEL.put(channel);
        Pref.LAST_QUALITY.put(mStream.getQuality().toString());
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
        startStream(new Stream(streamService, channel, quality));
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
        startStream(new Stream(streamService, quality));
    }

    /**
     * Starts the stream based on the user preferences. Will restart the stream if it was already running.
     */
    public synchronized void restartLastStream() {
        final StreamService streamService = StreamServiceManager.getStreamServiceByKey(Pref.LAST_STREAM_SERVICE
                .getString());
        final String channel = Pref.LAST_CHANNEL.getString();
        final Quality quality = Quality.valueOf(Pref.LAST_QUALITY.getString());

        if (channel.equals("")) {
            startStream(streamService, quality);
        } else {
            startStream(streamService, channel, quality);
        }
    }

    /**
     * Stops the active stream and transitions to the inactive state.
     */
    public synchronized void stopStream() {
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
        if (output.contains("Opening stream")) {
            updateState(StreamState.BUFFERING);
            startBufferingTimeout();

        } else if (output.contains("Waiting for streams")) {
            updateState(StreamState.WAITING);

        } else if (output.contains("Unable to open URL")) {
            for (final StreamListener listener : mListeners) {
                listener.onInvalidChannel(mStream);
            }
            stopStream();

        } else if (output.contains("Failed to start player")
                || output.contains("The default player (VLC) does not seem to be installed.")) {
            stopStream();
            for (final StreamListener listener : mListeners) {
                listener.onInvalidMediaPlayer(mStream);
            }

        } else if (output.contains("Starting player")) {
            stopBufferingTimeout();

        } else if (output.contains("Writing stream")) {
            updateState(StreamState.ACTIVE);
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

            case CONNECTING:
            case WAITING:
            case BUFFERING:
            case ACTIVE:
                // The user didn't cancel streaming, so try starting the stream again
                if (mStream != null && processId.equals(mStream.getProcessId())) {
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
                // Only stop the stream, it will be automatically restarted
                stopStreamConsole();
                restartLastStream();
            }
        };
        mBufferingTimer.schedule(mBufferingTimeout, BUFFERING_TIMEOUT);
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

}
