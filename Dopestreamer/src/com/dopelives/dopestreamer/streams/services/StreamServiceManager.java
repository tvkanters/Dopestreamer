package com.dopelives.dopestreamer.streams.services;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

/**
 * A manager for all available stream services.
 */
public class StreamServiceManager {

    /** A placeholder stream service for when a service is disabled */
    public static final StreamService DISABLED = new DisabledStreamService();
    /** A placeholder stream service for when no service is enabled */
    public static final StreamService NONE = new NoStreamService();

    /** The list of registered stream services */
    private static final List<StreamService> sStreamServices = new LinkedList<>();

    static {
        register(new Afreeca());
        register(new BeamPro());
        register(new Hitbox());
        register(new LivestreamNew());
        register(new LivestreamOld());
        register(new Twitch());
        register(new Ustream());
        register(new Vacker());
    }

    /**
     * Registers a stream service for global use.
     *
     * @param streamService
     *            The stream service to register
     */
    private static void register(final StreamService streamService) {
        sStreamServices.add(streamService);
    }

    /**
     * @return An unmodifiable list of stream services, excluding disabled ones
     */
    public static List<StreamService> getEnabledStreamServices() {
        final List<StreamService> enabledStreamServices = new LinkedList<>();

        for (final StreamService streamService : sStreamServices) {
            if (streamService.isEnabled()) {
                enabledStreamServices.add(streamService);
            }
        }

        if (enabledStreamServices.isEmpty()) {
            enabledStreamServices.add(NONE);
        }

        return Collections.unmodifiableList(enabledStreamServices);
    }

    /**
     * @return An unmodifiable list of stream services, including disabled ones
     */
    public static List<StreamService> getAllStreamServices() {
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
        final Optional<StreamService> match = sStreamServices.stream().filter(s -> s.getKey().equals(key)).findAny();
        return (match.isPresent() ? match.get() : null);
    }

    /**
     * This class is static-only.
     */
    private StreamServiceManager() {}

}
