package com.dopelives.dopestreamer.streamservices;

import java.io.IOException;
import java.security.InvalidParameterException;
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
		register(new Cast3d());
		register(new Hitbox());
		register(new Twitch());
		register(new Movie());
	}

	/**
	 * Starts a stream for the default channel at the given service.
	 * 
	 * @param streamService
	 *            The service to start
	 * @param quality
	 *            The quality to show the stream in
	 */
	public static void startStream(final StreamService streamService,
			final Quality quality) {
		startStream(streamService, streamService.getDefaultChannel(), quality);
	}

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
	public static void startStream(final StreamService streamService,
			final String channel, final Quality quality)
			throws InvalidParameterException {
		if (channel == null || channel.length() == 0) {
			throw new InvalidParameterException("Channel cannot be empty");
		}

		try {
			Runtime.getRuntime().exec(
					"livestreamer -l debug " + streamService.getUrl() + channel
							+ " " + quality.getCommand());
		} catch (final IOException ex) {
			ex.printStackTrace();
		}
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
	private StreamServiceManager() {
	}

}
