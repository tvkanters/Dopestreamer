package com.dopelives.dopestreamer.streams.services;

import javafx.scene.image.Image;

import com.dopelives.dopestreamer.Environment;

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

}