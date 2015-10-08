package com.dopelives.dopestreamer.streams.services;

import java.util.LinkedList;
import java.util.List;

import com.dopelives.dopestreamer.gui.combobox.ComboBoxItem;
import com.dopelives.dopestreamer.streams.Quality;
import com.dopelives.dopestreamer.util.Pref;

/**
 * The class for a stream service that can be selected and started to provide streams.
 */
public abstract class StreamService implements ComboBoxItem {

    /**
     * @return The key for this service, shouldn't be changed during refactoring and must be unique
     */
    public abstract String getKey();

    /**
     * The URL that Livestreamer use to choose the correct stream service plug-in.
     *
     * @return The URL in format {domain}.{tld}/({channelpath}/)?
     */
    public abstract String getUrl();

    /**
     * @return The URL for the icon to show next to the label, relative to the image path, or null if there is none
     */
    protected abstract String getStreamServiceIconUrl();

    /**
     * {@inheritDoc}
     */
    @Override
    public String getIconUrl() {
        return "services/" + (isEnabled() ? getStreamServiceIconUrl() : "disabled.png");
    }

    /**
     * @return True iff the service has not been disabled
     */
    public final boolean isEnabled() {
        return getKey().equals("none") ? false : !Pref.DISABLED_STREAM_SERVICES.contains(getKey());
    }

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
        return getUrl() + channel + " " + quality.getCommand();
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
     * conditional returns. Implementations of this function may use synchronised HTTP request so it's worth considering
     * calling this method in a separate thread.
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
