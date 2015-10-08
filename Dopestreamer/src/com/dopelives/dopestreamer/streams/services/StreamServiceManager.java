package com.dopelives.dopestreamer.streams.services;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

/**
 * A manager for all available stream services.
 */
public class StreamServiceManager {

    /** The list of registered stream services */
    private static final List<StreamService> sStreamServices = new LinkedList<>();

    /** The list of stream services to autoswitch to */
    private static final List<StreamService> sAutoswitchServices = new LinkedList<>();

    static {
        final StreamService hitbox = new Hitbox();
        final StreamService vacker = new Vacker();

        register(new Afreeca());
        register(hitbox);
        register(new LivestreamNew());
        register(new LivestreamOld());
        register(new Twitch());
        register(new Ustream());
        register(vacker);

        registerAutoswitch(vacker);
        registerAutoswitch(hitbox);
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
     * Registers a stream service for autoswitch use.
     *
     * @param streamService
     *            The stream service to register
     */
    private static void registerAutoswitch(final StreamService streamService) {
        sAutoswitchServices.add(streamService);
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
            enabledStreamServices.add(new NoStreamService());
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
        if (key.equals("none")) return new DisabledStreamService();

        final Optional<StreamService> match = sStreamServices.stream().filter(s -> s.getKey().equals(key)).findAny();

        return match.isPresent() ? match.get() : null;
    }

    /**
     * @return The autoswitch stream services
     */
    public static List<StreamService> getAutoswitchServices() {
        return sAutoswitchServices;
    }

    /**
     * This class is static-only.
     */
    private StreamServiceManager() {}

}
