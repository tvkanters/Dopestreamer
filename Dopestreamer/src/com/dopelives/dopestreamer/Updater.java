package com.dopelives.dopestreamer;

import org.json.JSONArray;
import org.json.JSONObject;

import com.dopelives.dopestreamer.util.HttpHelper;

/**
 * A class related to Dopestreamer updates.
 */
public class Updater {

    /** The latest Dopestreamer version's code in x.x.x format */
    private static String sLatestVersion = null;

    /**
     * Queries Github to check what the latest version is.
     *
     * @return The latest version's code in x.x.x format
     */
    public static synchronized String updateLatestVersion() {
        final String result = HttpHelper
                .getContent("https://api.github.com/repos/tvkanters/Dopestreamer/releases?per_page=1");
        if (result == null) {
            return null;
        }

        final JSONObject json = new JSONArray(result).getJSONObject(0);
        sLatestVersion = json.getString("tag_name").substring(1);

        return sLatestVersion;
    }

    /**
     * Returns the version that was latest last time it was checked. If it wasn't checked before, Github will be queried
     * to check what the latest version is.
     *
     * @return The latest version's code code in x.x.x format
     */
    public static synchronized String getLatestVersion() {
        if (sLatestVersion == null) {
            updateLatestVersion();
        }

        return sLatestVersion;
    }

    /**
     * This is a static-only class.
     */
    private Updater() {}

    /**
     * The interface for listeners that want to receive updater information.
     */
    public interface UpdaterListener {

        /**
         * Called when a new update is available.
         *
         * @param version
         *            The new update's version
         */
        void onNewUpdateAvailable(String version);

    }
}
