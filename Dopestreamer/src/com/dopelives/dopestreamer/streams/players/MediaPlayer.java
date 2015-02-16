package com.dopelives.dopestreamer.streams.players;

import java.io.File;
import java.util.List;

import com.dopelives.dopestreamer.gui.combobox.ComboBoxItem;

/**
 * The class for a media player that will host the streams by being started by Livestreamer.
 */
public abstract class MediaPlayer implements ComboBoxItem {

    /**
     * @return The key for this player, shouldn't be changed during refactoring and must be unique
     */
    public abstract String getKey();

    /**
     * @return The path where this player's executable is located or null if it wasn't found
     */
    public String getPath() {
        for (final String path : getPaths()) {
            final File file = new File(path);
            if (file.exists() && !file.isDirectory()) {
                return path;
            }
        }

        return null;
    }

    /**
     * @return All standard paths where this player may be located
     */
    protected abstract List<String> getPaths();

    /**
     * @return True iff the player is found on the user's device
     */
    public boolean isFound() {
        return getPath() != null;
    }

}
