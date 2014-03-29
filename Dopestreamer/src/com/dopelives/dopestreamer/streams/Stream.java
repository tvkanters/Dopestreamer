package com.dopelives.dopestreamer.streams;

import java.security.InvalidParameterException;

import com.dopelives.dopestreamer.shell.Console;
import com.dopelives.dopestreamer.shell.ConsoleListener;
import com.dopelives.dopestreamer.shell.Shell;

/**
 * A Console wrapper specifically for streams.
 */
public class Stream {

    /** The delay in seconds at which the stream service should be polled */
    private static final int RETRY_DELAY = 2;

    /** The console that the stream runs in */
    private final Console mConsole;

    /**
     * Starts a stream for the given channel at the service.
     *
     * @param streamService
     *            The service to start
     * @param channel
     *            The channel to start on the service
     * @param quality
     *            The quality to show the stream in
     *
     * @throws InvalidParameterException
     *             Thrown when the provided channel isn't valid
     */
    public Stream(final StreamService streamService, final String channel, final Quality quality)
            throws InvalidParameterException {
        if (channel == null || channel.length() == 0) {
            throw new InvalidParameterException("Channel cannot be empty");
        }

        mConsole = Shell.getInstance().createConsole(
                "livestreamer -l debug --retry-streams " + RETRY_DELAY + " " + streamService.getUrl() + channel + " "
                        + quality.getCommand());
    }

    /**
     * Starts a stream for the default channel at the given service.
     *
     * @param streamService
     *            The service to start
     * @param quality
     *            The quality to show the stream in
     */
    public Stream(final StreamService streamService, final Quality quality) {
        this(streamService, streamService.getDefaultChannel(), quality);
    }

    /**
     * Starts the stream through Livestreamer. May only be called once.
     */
    public void start() {
        mConsole.start();
    }

    /**
     * Stops Livestreamer and its child processes.
     */
    public void stop() {
        mConsole.stop();
    }

    /**
     * Adds a listener to this stream that will receive call-backs on console events such as output and stop.
     *
     * @param listener
     *            The listener that will receive the call-backs
     */
    public void addListener(final ConsoleListener listener) {
        mConsole.addListener(listener);
    }

}