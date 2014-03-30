package com.dopelives.dopestreamer;

import java.util.prefs.Preferences;

/**
 * An enum used to manage Dopestreamer preferences.
 */
public enum Pref {

    /** Boolean type indication whether or not the stream should start upon opening */
    AUTO_START("autostart", false),
    /** Boolean type indication whether or not Dopestreamer should start minimise to the tray */
    MINIMISE_TO_TRAY("minimisetotray", false),
    /** Integer type representation of the window's x-coordinate */
    WINDOW_X("windowx", -32000),
    /** Integer type representation of the window's y-coordinate */
    WINDOW_Y("windowy", -32000);

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
     * Resets the value to the default.
     */
    public void reset() {
        sPreferences.remove(mKey);
    }

}
