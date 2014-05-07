package com.dopelives.dopestreamer.streams.services;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

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
        register(new Ustream());
        register(new Xphome());
        register(new Restream());
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
     * Finds the stream service with the given key.
     *
     * @param key
     *            The key to search for
     *
     * @return The stream service with matching key or null if it wasn't found
     */
    public static StreamService getStreamServiceByKey(final String key) {
        for (final StreamService streamService : sStreamServices) {
            if (streamService.getKey().equals(key)) {
                return streamService;
            }
        }
        return null;
    }

    /**
     * This class is static-only.
     */
    private StreamServiceManager() {}

}
