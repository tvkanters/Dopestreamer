package com.dopelives.dopestreamer.streams;

import com.dopelives.dopestreamer.gui.StreamState;

/**
 * The interface for receiving updates of stream related changes.
 */
public interface StreamListener {

    /**
     * Called when the state of the manager is updated.
     *
     * @param streamManager
     *            The calling manager
     * @param oldState
     *            The old state
     * @param newState
     *            The new state
     */
    void onStateUpdated(final StreamManager streamManager, final StreamState oldState, final StreamState newState);

    /**
     * Called when the given channel for a stream was not found at the stream service.
     *
     * @param stream
     *            The stream that has the invalid channel
     */
    void onInvalidChannel(final Stream stream);

    /**
     * Called when the chosen quality for a stream was not available for the given channel.
     *
     * @param stream
     *            The stream that has the invalid quality
     */
    void onInvalidQuality(final Stream stream);

    /**
     * Called when the given (or default) media player could not be used by Livestreamer.
     *
     * @param stream
     *            The stream that encountered the media player error
     */
    void onInvalidMediaPlayer(final Stream stream);

    /**
     * Called when Livestreamer appears to be outdated.
     *
     * @param stream
     *            The stream that encountered the error
     */
    void onInvalidLivestreamer(final Stream stream);

    /**
     * Called when Livestreamer can't be found.
     *
     * @param stream
     *            The stream that encountered the error
     */
    void onLivestreamerNotFound(final Stream stream);

}