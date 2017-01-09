package com.dopelives.dopestreamer.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.prefs.Preferences;

import org.json.JSONArray;
import org.json.JSONException;

import com.dopelives.dopestreamer.streams.FavouriteStream;
import com.dopelives.dopestreamer.streams.Quality;
import com.dopelives.dopestreamer.streams.services.StreamServiceManager;

/**
 * An enum used to manage Dopestreamer preferences.
 */
public enum Pref {

    /** Boolean type indication whether or not the stream should start upon opening */
    AUTO_START("autostart", false),
    /** Boolean type indication whether or not Dopestreamer should start minimise to the tray */
    SHOW_IN_TRAY("minimisetotray", false),
    /** Integer type representation of the window's x-coordinate */
    WINDOW_X("windowx", -32000),
    /** Integer type representation of the window's y-coordinate */
    WINDOW_Y("windowy", -32000),
    /** The last channel someone streamed, empty string for default Dopelives channel */
    LAST_CHANNEL("lastchannel", "live"),
    /** The key of the last stream service used */
    LAST_STREAM_SERVICE("laststreamservice", "xphome"),
    /** The enum constant of the last quality used */
    LAST_QUALITY("lastquality", Quality.BEST.name()),
    /** The key of the selected default media player or an empty string for a custom media player */
    DEFAULT_PLAYER("defaultplayer", ""),
    /** The location of the media player to use */
    PLAYER_LOCATION("playerlocation", ""),
    /** Boolean type indication whether or not the window should start minimised */
    START_MINIMISED("startminimised", false),
    /** Whether or not streams should be restarted after dying */
    GAME_MODE("gamemode", false),
    /** Whether or not topic changes that indicate a starting stream should notify the user */
    NOTIFICATIONS("notifications", false),
    /** Whether or not topic changes should use ding dong as sound */
    NOTIFICATION_DINGDONG("notificationdingdong", false),
    /** The Vacker server to use */
    VACKER_SERVER("vackerserver", ""),
    /** Whether or not Dopestreamer should check if updates of Livestreamer are available */
    LIVESTREAMER_UPDATE_CHECK("livestreamerupdatecheck", false),
    /** Whether or not the buffering of HLS should be done quicker */
    HLS_QUICK_BUFFER("hlsquickbuffer", true),
    /** Stream services that the user disabled and shouldn't be offered */
    DISABLED_STREAM_SERVICES("disabledstreamservices", new String[] { "afreeca", "bambuser", "beampro", "livestreamold",
            "livestream" }),
    /** The user's favourite streams, stored for easy access */
    FAVOURITE_STREAMS("favouritestreams", new String[] {
            new FavouriteStream("Dopelives", StreamServiceManager.getStreamServiceByKey("xphome"), "live").toJson() });

    /** Java's preferences manager */
    private static final Preferences sPreferences = Preferences.userRoot().node(Pref.class.getName());

    /** The key for the preference */
    private final String mKey;
    /** The default boolean value for the preference */
    private final Boolean mDefaultBoolean;
    /** The default integer value for the preference */
    private final Integer mDefaultInt;
    /** The default string value for the preference */
    private final String mDefaultString;
    /** The default list for the preference */
    private final List<String> mDefaultList;

    /** The list in which the values for the preference are stored */
    private List<String> mList;

    private Pref(final String key, final Boolean defaultValue) {
        mKey = key;
        mDefaultBoolean = defaultValue;
        mDefaultInt = null;
        mDefaultString = null;
        mDefaultList = null;
        mList = null;
    }

    private Pref(final String key, final Integer defaultValue) {
        mKey = key;
        mDefaultBoolean = null;
        mDefaultInt = defaultValue;
        mDefaultString = null;
        mDefaultList = null;
        mList = null;
    }

    private Pref(final String key, final String defaultValue) {
        mKey = key;
        mDefaultBoolean = null;
        mDefaultInt = null;
        mDefaultString = defaultValue;
        mDefaultList = null;
        mList = null;
    }

    private Pref(final String key, final String[] defaultValue) {
        mKey = key;
        mDefaultBoolean = null;
        mDefaultInt = null;
        mDefaultString = null;
        mDefaultList = new ArrayList<>(Arrays.asList(defaultValue));

        final String storedList = Preferences.userRoot().node(Pref.class.getName()).get(mKey, null);
        if (storedList != null) {
            mList = decodeList(storedList);
        } else {
            mList = new ArrayList<>(mDefaultList);
        }
    }

    /**
     * Saves the preference boolean value persistently for the user.
     *
     * @param value
     *            The value to save for this preference
     */
    public void put(final boolean value) {
        enforceBoolean();
        sPreferences.putBoolean(mKey, value);
    }

    /**
     * Saves the preference integer value persistently for the user.
     *
     * @param value
     *            The value to save for this preference
     */
    public void put(final int value) {
        enforceInteger();
        sPreferences.putInt(mKey, value);
    }

    /**
     * Saves the preference string value persistently for the user.
     *
     * @param value
     *            The value to save for this preference
     */
    public void put(final String value) {
        enforceString();
        sPreferences.put(mKey, value);
    }

    /**
     * Save the preference list persistently for the user.
     *
     * @param value
     *            The value to save for this preference
     */
    public void put(final List<String> value) {
        enforceList();
        mList = new ArrayList<String>(value);
        sPreferences.put(mKey, encodeList(mList));
    }

    /**
     * Adds the preference list value persistently for the user.
     *
     * @param value
     *            The value to add for this preference
     */
    public void add(final String value) {
        enforceList();
        mList.add(value);
        sPreferences.put(mKey, encodeList(mList));
    }

    /**
     * Removes a preference list value persistently for the user.
     *
     * @param value
     *            The value to remove for this preference
     */
    public void remove(final String value) {
        enforceList();
        mList.remove(value);
        sPreferences.put(mKey, encodeList(mList));
    }

    /**
     * Retrieves a saved preference value.
     *
     * @return The saved value or a default one if it has not been put
     */
    public boolean getBoolean() {
        enforceBoolean();
        return sPreferences.getBoolean(mKey, mDefaultBoolean);
    }

    /**
     * Retrieves a saved preference value.
     *
     * @return The saved value or a default one if it has not been put
     */
    public int getInteger() {
        enforceInteger();
        return sPreferences.getInt(mKey, mDefaultInt);
    }

    /**
     * Retrieves a saved preference value.
     *
     * @return The saved value or a default one if it has not been put
     */
    public String getString() {
        enforceString();
        return sPreferences.get(mKey, mDefaultString);
    }

    /**
     * Retrieves a saved list.
     *
     * @return The saved value or a default one if it has not been put
     */
    public List<String> getList() {
        enforceList();
        return Collections.unmodifiableList(mList);
    }

    /**
     * Retrieves a saved list.
     *
     * @return Whether on not the value exists in the list
     */
    public boolean contains(final String value) {
        enforceList();
        return mList.contains(value);
    }

    /**
     * Retrieves a default preference value.
     *
     * @return The default value
     */
    public boolean getDefaultBoolean() {
        enforceBoolean();
        return mDefaultBoolean;
    }

    /**
     * Retrieves a default preference value.
     *
     * @return The default value
     */
    public int getDefaultInt() {
        enforceInteger();
        return mDefaultInt;
    }

    /**
     * Retrieves a default preference value.
     *
     * @return The default value
     */
    public String getDefaultString() {
        enforceString();
        return mDefaultString;
    }

    /**
     * Retrieves a default preference value.
     *
     * @return The default value
     */
    public List<String> getDefaultList() {
        enforceList();
        return Collections.unmodifiableList(mDefaultList);
    }

    /**
     * Resets the value to the default.
     */
    public void reset() {
        sPreferences.remove(mKey);

        if (mList != null) {
            mList = new ArrayList<>(mDefaultList);
        }
    }

    /**
     * Ensures that the current preference is an integer.
     */
    private void enforceBoolean() {
        if (mDefaultBoolean == null) {
            throw new IllegalArgumentException(this + " does not accept booleans");
        }
    }

    /**
     * Ensures that the current preference is an integer.
     */
    private void enforceInteger() {
        if (mDefaultInt == null) {
            throw new IllegalArgumentException(this + " does not accept integers");
        }
    }

    /**
     * Ensures that the current preference is an integer.
     */
    private void enforceString() {
        if (mDefaultString == null) {
            throw new IllegalArgumentException(this + " does not accept strings");
        }
    }

    /**
     * Ensures that the current preference is a list.
     */
    private void enforceList() {
        if (mDefaultList == null) {
            throw new IllegalArgumentException(this + " does not accept list items");
        }
    }

    /**
     * Parses the string representation of a stored list to a list format.
     *
     * @param storedList
     *            The string representation of the list
     *
     * @return The list in actual list format
     */
    private List<String> decodeList(final String storedList) {
        try {
            final JSONArray json = new JSONArray(storedList);

            final List<String> list = new ArrayList<>();
            for (int i = 0; i < json.length(); ++i) {
                list.add(json.getString(i));
            }
            return list;

        } catch (final JSONException ex) {
            // Support for legacy stored lists
            return new ArrayList<>(Arrays.asList(storedList.split(",")));
        }
    }

    /**
     * Converts a list into a storable JSON representation.
     *
     * @param list
     *            The list to store
     *
     * @return The JSON representation of the list to store as string
     */
    private String encodeList(final List<String> list) {
        final JSONArray json = new JSONArray();
        for (final String listItem : list) {
            json.put(listItem);
        }
        return json.toString();
    }

}
