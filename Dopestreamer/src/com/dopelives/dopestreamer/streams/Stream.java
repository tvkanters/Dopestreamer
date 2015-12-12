package com.dopelives.dopestreamer.streams;

import java.security.InvalidParameterException;
import java.util.concurrent.ScheduledFuture;
import java.util.regex.Pattern;

import com.dopelives.dopestreamer.shell.Console;
import com.dopelives.dopestreamer.shell.ConsoleListener;
import com.dopelives.dopestreamer.shell.ProcessId;
import com.dopelives.dopestreamer.shell.Shell;
import com.dopelives.dopestreamer.streams.players.MediaPlayer;
import com.dopelives.dopestreamer.streams.players.MediaPlayerManager;
import com.dopelives.dopestreamer.streams.services.StreamService;
import com.dopelives.dopestreamer.util.Executor;
import com.dopelives.dopestreamer.util.Pref;

/**
 * A Console wrapper specifically for streams.
 */
public class Stream {

    /** The delay in seconds at which the stream service should be polled */
    public static final int RETRY_DELAY = 2;

    /** The regex that will match valid channels */
    private static final Pattern sChannelMatcher = Pattern.compile("^[a-zA-Z0-9/_-]+$");

    /** The console that the stream runs in */
    private final Console mConsole;

    /** The service the that is streamed */
    private final StreamService mStreamService;
    /** The channel started on the service */
    private final String mChannel;
    /** The quality the stream is shown in */
    private final Quality mQuality;

    /** The thread handling the connecting process before Livestreamer takes over */
    private ScheduledFuture<?> mConnectTask;
    /** A flag indicating that the stream is stopping */
    private boolean mStopping = false;

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
        if (!sChannelMatcher.matcher(channel).find()) {
            throw new InvalidParameterException("Invalid channel");
        }

        mStreamService = streamService;
        mChannel = channel;
        mQuality = quality;

        // Prepare Livestreamer command
        final Shell shell = Shell.getInstance();
        String command = shell.getLivestreamerPath();

        command += " -l debug --retry-streams " + RETRY_DELAY;

        // Makes HLS buffering quicker is wanted
        if (Pref.HLS_QUICK_BUFFER.getBoolean()) {
            command += " --hls-live-edge=2";
        }

        // Add OS specific arguments
        final String additionalArguments = shell.getAdditionalLivestreamerArguments();
        if (!additionalArguments.equals("")) {
            command += " " + additionalArguments.trim();
        }

        // Add a custom player location, if any
        command += getMediaPlayerArgument();

        // Add channel information
        command += " " + mStreamService.getConnectionDetails(mChannel, mQuality);

        mConsole = shell.createConsole(command);
    }

    /**
     * Checks if a media player argument should be added to the Livestreamer command and returns it if is
     *
     * @return The media player argument prefixed with a space or an empty string if it isn't needed
     */
    public static String getMediaPlayerArgument() {
        String playerLocation = null;
        final MediaPlayer mediaPlayer = MediaPlayerManager.getMediaPlayerByKey(Pref.DEFAULT_PLAYER.getString());
        if (mediaPlayer != null) {
            playerLocation = mediaPlayer.getPath();
        }

        if (playerLocation == null) {
            playerLocation = Pref.PLAYER_LOCATION.getString();
        }

        if (!playerLocation.equals("")) {
            return " -p \"" + playerLocation + "\"";
        }

        return "";
    }

    /**
     * Starts the stream through Livestreamer. May only be called once.
     */
    public synchronized void start() {
        Executor.execute(() -> {
            if (!mStreamService.isChannelPossible(mChannel)) {
                // Channel isn't possible, act as if Livestreamer complained
                for (final ConsoleListener listener : mConsole.getListeners()) {
                    listener.onConsoleOutput(mConsole.getProcessId(), "404 Client Error: Not Found");
                }
                return;
            }

            mConnectTask = Executor.scheduleInterval(() -> {
                if (mStopping) {
                    mConnectTask.cancel(false);
                    mConnectTask = null;
                    return;
                }

                if (!mStreamService.isConnectPossible(mChannel)) {
                    // Act as if Livestreamer is waiting for streams
                    for (final ConsoleListener listener : mConsole.getListeners()) {
                        listener.onConsoleOutput(mConsole.getProcessId(), "Waiting for streams");
                    }

                } else {
                    // Start stream
                    mConsole.start();
                    mConnectTask.cancel(false);
                    mConnectTask = null;
                }
            } , 0, RETRY_DELAY * 1000);
        });
    }

    /**
     * Stops Livestreamer and its child processes.
     */
    public synchronized void stop() {
        mStopping = true;
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

    /**
     * @return The process ID of the stream's console
     */
    public ProcessId getProcessId() {
        return mConsole.getProcessId();
    }

    /**
     * @return The service the that is streamed
     */
    public StreamService getStreamService() {
        return mStreamService;
    }

    /**
     * @return The channel started on the service
     */
    public String getChannel() {
        return mChannel;
    }

    /**
     * @return The quality the stream is shown in
     */
    public Quality getQuality() {
        return mQuality;
    }

}
