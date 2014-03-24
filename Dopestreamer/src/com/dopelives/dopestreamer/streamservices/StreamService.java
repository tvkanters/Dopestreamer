package com.dopelives.dopestreamer.streamservices;

import javafx.scene.image.Image;

import com.dopelives.dopestreamer.Initialiser;

/**
 * The class for a stream service that can be selected and started to provide streams.
 */
public abstract class StreamService {

    /** The icon to show next to this service's label */
    private final Image mIcon;

    /**
     * Prepares the stream service that can be selected and started to provide streams.
     */
    protected StreamService() {
        mIcon = new Image(Initialiser.IMAGE_FOLDER + getIconUrl());
    }

    /**
     * @return The label to show for this service
     */
    public abstract String getLabel();

    /**
     * @return The icon to show next to this service's label
     */
    public Image getIcon() {
        return mIcon;
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
     * @return The default channel to start for this service when a channel isn't provided
     */
    public abstract String getDefaultChannel();

}
