package com.dopelives.dopestreamer.streams;

import java.util.Objects;

import org.json.JSONObject;

import com.dopelives.dopestreamer.gui.combobox.ComboBoxItem;
import com.dopelives.dopestreamer.streams.services.StreamService;
import com.dopelives.dopestreamer.streams.services.StreamServiceManager;

/**
 * A stream setting consisting of a service and channel that the user has favourited for easy access.
 */
public class FavouriteStream implements ComboBoxItem {

    /** The label the user entered for this favourite */
    private final String mLabel;
    /** The stream service that the favourite stream is on */
    private final StreamService mStreamService;
    /** The channel within the stream service */
    private final String mChannel;

    /**
     * Creates a new favourite stream based on the given JSON object.
     *
     * @param label
     *            The label the user entered for this favourite
     * @param streamService
     *            The stream service that the favourite stream is on
     * @param channel
     *            The channel within the stream service
     */
    public FavouriteStream(final String label, final StreamService streamService, final String channel) {
        mLabel = label;
        mStreamService = streamService;
        mChannel = channel;
    }

    /**
     * Creates a new favourite stream based on the given JSON object string.
     *
     * @param json
     *            The JSON object string to import
     */
    public FavouriteStream(final String json) {
        this(new JSONObject(json));
    }

    /**
     * Creates a new favourite stream based on the given JSON object.
     *
     * @param json
     *            The JSON object to import
     */
    public FavouriteStream(final JSONObject json) {
        mLabel = json.getString("label");

        final StreamService streamService = StreamServiceManager.getStreamServiceByKey(json.getString("streamService"));
        mStreamService = (streamService != null ? streamService : StreamServiceManager.DISABLED);

        mChannel = json.getString("channel");
    }

    /**
     * Exports this favourite stream to a JSON object, used to store it as a string.
     *
     * @return This favourite stream's JSON representation
     */
    public String toJson() {
        final JSONObject json = new JSONObject();
        json.put("label", mLabel);
        json.put("streamService", mStreamService.getKey());
        json.put("channel", mChannel);
        return json.toString();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getLabel() {
        return mLabel;
    }

    /**
     * @return The stream service that the favourite stream is on
     */
    public StreamService getStreamService() {
        return mStreamService;
    }

    /**
     * @return The channel within the stream service
     */
    public String getChannel() {
        return mChannel;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getIconUrl() {
        return (mStreamService != null ? mStreamService.getIconUrl() : null);
    }

    /**
     * @return A favourite stream hash based on the contents.
     */
    @Override
    public int hashCode() {
        return mLabel.hashCode() + mStreamService.hashCode() + mChannel.hashCode();
    }

    /**
     * @return True iff the stream service and (case-insensitive) channel are equal.
     */
    public boolean equalsLoosely(final Object obj) {
        if (!(obj instanceof FavouriteStream)) {
            return false;
        }
        final FavouriteStream other = (FavouriteStream) obj;
        return (Objects.equals(mStreamService, other.mStreamService)
                && ((mChannel != null && mChannel.equalsIgnoreCase(other.mChannel))
                        || (mChannel == null && other.mChannel == null)));
    }

    /**
     * @return True iff all members are equal.
     */
    @Override
    public boolean equals(final Object obj) {
        if (!(obj instanceof FavouriteStream)) {
            return false;
        }
        final FavouriteStream other = (FavouriteStream) obj;
        return (Objects.equals(mLabel, other.mLabel) && Objects.equals(mStreamService, other.mStreamService)
                && Objects.equals(mChannel, other.mChannel));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return toJson();
    }

}
