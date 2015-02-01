package com.dopelives.dopestreamer.util;

import java.util.prefs.Preferences;

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
    LAST_CHANNEL("lastchannel", ""),
    /** The key of the last stream service used */
    LAST_STREAM_SERVICE("laststreamservice", "hitbox"),
    /** The enum constant of the last quality used */
    LAST_QUALITY("lastquality", "BEST"),
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
    /** Whether or not streams automatically switch to live Dopelives channels */
    AUTOSWITCH("autoswitch", true),
    /** The Vacker server to use */
    VACKER_SERVER("vackerserver", "");

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

    private Pref(final String key, final Boolean defaultValue) {
        mKey = key;
        mDefaultBoolean = defaultValue;
        mDefaultInt = null;
        mDefaultString = null;
    }

    private Pref(final String key, final Integer defaultValue) {
        mKey = key;
        mDefaultBoolean = null;
        mDefaultInt = defaultValue;
        mDefaultString = null;
    }

    private Pref(final String key, final String defaultValue) {
        mKey = key;
        mDefaultBoolean = null;
        mDefaultInt = null;
        mDefaultString = defaultValue;
    }

    /**
     * Saves the preference value persistently for the user.
     *
     * @param value
     *            The value to save for this preference
     */
    public void put(final boolean value) {
        sPreferences.putBoolean(mKey, value);
    }

    /**
     * Saves the preference value persistently for the user.
     *
     * @param value
     *            The value to save for this preference
     */
    public void put(final int value) {
        sPreferences.putInt(mKey, value);
    }

    /**
     * Saves the preference value persistently for the user.
     *
     * @param value
     *            The value to save for this preference
     */
    public void put(final String value) {
        sPreferences.put(mKey, value);
    }

    /**
     * Retrieves a saved preference value.
     *
     * @return The saved value or a default one if it has not been put
     */
    public boolean getBoolean() {
        return sPreferences.getBoolean(mKey, mDefaultBoolean);
    }

    /**
     * Retrieves a saved preference value.
     *
     * @return The saved value or a default one if it has not been put
     */
    public int getInteger() {
        return sPreferences.getInt(mKey, mDefaultInt);
    }

    /**
     * Retrieves a saved preference value.
     *
     * @return The saved value or a default one if it has not been put
     */
    public String getString() {
        return sPreferences.get(mKey, mDefaultString);
    }

    /**
     * Retrieves a default preference value.
     *
     * @return The default value
     */
    public boolean getDefaultBoolean() {
        return mDefaultBoolean;
    }

    /**
     * Retrieves a default preference value.
     *
     * @return The default value
     */
    public int getDefaultInt() {
        return mDefaultInt;
    }

    /**
     * Retrieves a default preference value.
     *
     * @return The default value
     */
    public String getDefaultString() {
        return mDefaultString;
    }

    /**
     * Resets the value to the default.
     */
    public void reset() {
        sPreferences.remove(mKey);
    }

}
