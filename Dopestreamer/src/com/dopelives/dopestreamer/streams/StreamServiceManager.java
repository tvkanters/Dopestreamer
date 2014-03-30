package com.dopelives.dopestreamer.streams;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import com.dopelives.dopestreamer.streamservices.Afreeca;
import com.dopelives.dopestreamer.streamservices.Bambuser;
import com.dopelives.dopestreamer.streamservices.Hitbox;
import com.dopelives.dopestreamer.streamservices.Livestream;
import com.dopelives.dopestreamer.streamservices.Movie;
import com.dopelives.dopestreamer.streamservices.Twitch;

/**
 * A manager for all available stream services.
 */
public class StreamServiceManager {

    /** The list of registered stream services */
    private static final List<StreamService> sStreamServices = new LinkedList<>();

    static {
        register(new Afreeca());
        register(new Bambuser());
        register(new Hitbox());
        register(new Livestream());
        register(new Twitch());
        register(new Movie());
    }

    /**
     * Registers a stream service for global use.
     *
     * @param streamService
     *            The stream service to register
     */
    public static void register(final StreamService streamService) {
        sStreamServices.add(streamService);
    }

    /**
     * @return An unmodifiable list of stream services
     */
    public static List<StreamService> getStreamServices() {
        return Collections.unmodifiableList(sStreamServices);
    }

    /**
     * This class is static-only.
     */
    private StreamServiceManager() {}

}
