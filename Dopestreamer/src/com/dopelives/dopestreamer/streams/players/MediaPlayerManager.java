package com.dopelives.dopestreamer.streams.players;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

/**
 * A manager for all available media players.
 */
public class MediaPlayerManager {

    /** The list of registered media players */
    private static final List<MediaPlayer> sMediaPlayers = new LinkedList<>();

    static {
        sMediaPlayers.add(new Custom());
        register(new MpcHc());
        register(new Mplayer());
        register(new Mpv());
        register(new Potplayer());
        register(new Vlc());
    }

    /**
     * Registers a media player for global use. Will only succeed if the media player is found in the user's device.
     *
     * @param mediaPlayer
     *            The media player to register
     */
    public static void register(final MediaPlayer mediaPlayer) {
        if (mediaPlayer.isFound()) {
            sMediaPlayers.add(mediaPlayer);
        }
    }

    /**
     * @return An unmodifiable list of stream services
     */
    public static List<MediaPlayer> getMediaPlayers() {
        return Collections.unmodifiableList(sMediaPlayers);
    }

    /**
     * Finds the media player with the given key.
     *
     * @param key
     *            The key to search for
     *
     * @return The media player with matching key or null if it wasn't found
     */
    public static MediaPlayer getMediaPlayerByKey(final String key) {
        final Optional<MediaPlayer> match = sMediaPlayers.stream().filter(mp -> mp.getKey().equals(key)).findAny();

        return match.isPresent() ? match.get() : null;
    }

    /**
     * This class is static-only.
     */
    private MediaPlayerManager() {}

}
