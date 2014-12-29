package com.dopelives.dopestreamer.streams.services;

import java.util.LinkedList;
import java.util.List;

import javafx.scene.image.Image;

import com.dopelives.dopestreamer.Environment;
import com.dopelives.dopestreamer.streams.Quality;

/**
 * The class for a stream service that can be selected and started to provide streams.
 */
public abstract class StreamService {

    /**
     * @return The key for this service, shouldn't be changed during refactoring and must be unique
     */
    public abstract String getKey();

    /**
     * @return The label to show for this service
     */
    public abstract String getLabel();

    /**
     * @return The icon to show next to this service's label
     */
    public Image getIcon() {
        return new Image(Environment.IMAGE_FOLDER + getIconUrl());
    }

    /**
     * @return The URL for the icon to show next to this service's label, relative to the image path
     */
    protected abstract String getIconUrl();

    /**
     * The URL that Livestreamer use to choose the correct stream service plug-in.
     *
     * @return The URL in format <domain>.<tld>/(<channelpath>)?
     */
    public abstract String getUrl();

    /**
     * Retrieves the details of a stream service, channel and quality to connect through Livestreamer.
     *
     * @param channel
     *            The channel to connect to
     * @param quality
     *            The quality to show the stream in
     *
     * @return The details to connect through Livestreamer in the format {url}/{channel} {quality}
     */
    public String getConnectionDetails(final String channel, final Quality quality) {
        return getUrl() + channel + " " + quality;
    }

    /**
     * @return True iff the service has a default (Dopelives) channel
     */
    public boolean hasDefaultChannel() {
        return getDefaultChannel() != null;
    }

    /**
     * @return The default channel to start for this service when a channel isn't provided or null if there isn't any
     */
    public String getDefaultChannel() {
        return null;
    }

    /**
     * @return The qualities available for this channel
     */
    public List<Quality> getQualities() {
        final List<Quality> qualities = new LinkedList<>();
        qualities.add(Quality.BEST);
        qualities.addAll(getServiceSpecificQualities());
        qualities.add(Quality.WORST);
        return qualities;
    }

    /**
     * @return The qualities specific to this channel order from best to worst
     */
    protected List<Quality> getServiceSpecificQualities() {
        return new LinkedList<>();
    }

    /**
     * Checks if it's worth attempting to connect to the stream service. Returns true by default but can be overridden
     * to add conditional returns.
     *
     * @param channel
     *            The channel to connect to
     *
     * @return True if a connection attempt to the service can be made
     */
    public boolean isConnectPossible(final String channel) {
        return true;
    }

    /**
     * Checks if a channel may exist on to the stream service. Returns true by default but can be overridden to add
     * conditional returns.
     *
     * @param channel
     *            The channel to connect to
     *
     * @return False if the channel does not exist
     */
    public boolean isChannelPossible(final String channel) {
        return true;
    }

    /**
     * Checks if a custom channel can be requested. Returns true by default but can be overridden to add conditional
     * returns.
     *
     * @return True iff the user may enter a custom channel
     */
    public boolean allowsCustomChannels() {
        return true;
    }

}
