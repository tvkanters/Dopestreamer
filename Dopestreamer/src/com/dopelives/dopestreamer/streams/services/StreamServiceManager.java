package com.dopelives.dopestreamer.streams.services;

import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/**
 * A manager for all available stream services.
 */
public class StreamServiceManager {

    /** The list of registered stream services */
    private static final List<StreamService> sStreamServices = new LinkedList<>();

    /** The list of stream services to autoswitch to */
    private static final Set<StreamService> sAutoswitchServices = new HashSet<>();

    /** The autoswitch service */
    private static final Autoswitch sAutoswitch = new Autoswitch();

    static {
        register(new Afreeca());
        register(new Bambuser());
        register(new Hitbox(), true);
        register(new LivestreamNew());
        register(new LivestreamOld());
        register(new Restream());
        register(new Twitch(), true);
        register(new Ustream());
        register(new Xphome(), true);
    }

    /**
     * Registers a stream service for global use.
     *
     * @param streamService
     *            The stream service to register
     */
    private static void register(final StreamService streamService) {
        register(streamService, false);
    }

    /**
     * Registers a stream service for global use.
     *
     * @param streamService
     *            The stream service to register
     * @param autoswitch
     *            Whether or not this server can be autoswitched to
     */
    private static void register(final StreamService streamService, final boolean autoswitch) {
        sStreamServices.add(streamService);
        if (autoswitch) {
            sAutoswitchServices.add(streamService);
        }
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
        if (sAutoswitch.getKey().equals(key)) {
            return sAutoswitch;
        }

        for (final StreamService streamService : sStreamServices) {
            if (streamService.getKey().equals(key)) {
                return streamService;
            }
        }

        return null;
    }

    /**
     * @return The autoswitch stream service
     */
    public static Autoswitch getAutoswitch() {
        return sAutoswitch;
    }

    /**
     * @return The autoswitch stream services
     */
    public static Set<StreamService> getAutoswitchServices() {
        return sAutoswitchServices;
    }

    /**
     * This class is static-only.
     */
    private StreamServiceManager() {}

}
