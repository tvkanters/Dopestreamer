package com.dopelives.dopestreamer;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.HashSet;
import java.util.Set;

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
     * Checks if there the current version of Dopestreamer is outdated, based on the latest version on Github.
     *
     * @return True iff there is a newer version
     */
    public static boolean isOutdated() {
        final String latestVersion = getLatestVersion();
        return latestVersion != null && !latestVersion.equals(Environment.VERSION);
    }

    /**
     * Updates Dopestreamer based on the latest release available on Github. This method closes the class loader, so
     * after calling it, no more classes can be loaded.
     *
     * @return True iff the update was successful
     */
    public static boolean downloadAndInstallUpdate() {
        final String result = HttpHelper
                .getContent("https://api.github.com/repos/tvkanters/Dopestreamer/releases?per_page=1");
        if (result == null) {
            return false;
        }

        final JSONObject json = new JSONArray(result).getJSONObject(0);
        final Set<String> updatedFiles = new HashSet<>();

        // Check each available asset
        final JSONArray assets = json.getJSONArray("assets");
        for (int i = 0; i < assets.length(); ++i) {
            final JSONObject asset = assets.getJSONObject(i);
            final String fileName = asset.getString("name");
            final String filePath = Environment.EXE_DIR + fileName;

            // Only update files that were originally there
            if (!new File(filePath).exists()) {
                continue;
            }

            // Download the update to a temp file
            final String tempPath = filePath + ".tmp";
            final String downloadUrl = asset.getString("browser_download_url");
            try {
                // Clear old temp file if it existed
                new File(tempPath).delete();

                // Download new update
                Files.copy(new URL(downloadUrl).openStream(), FileSystems.getDefault().getPath(tempPath),
                        StandardCopyOption.REPLACE_EXISTING);

                // Make note of the successful download
                updatedFiles.add(filePath);
                System.out.println("Downloaded " + fileName);

            } catch (final IOException ex) {
                // Download failed, remove temp file
                ex.printStackTrace();
                new File(tempPath).delete();
                return false;
            }
        }

        // Close the class loader so that the JAR may be deleted
        try {
            ((URLClassLoader) ClassLoader.getSystemClassLoader()).close();
        } catch (final IOException ex) {
            ex.printStackTrace();
            return false;
        }

        // All updates have been downloaded, replace files
        for (final String updateFile : updatedFiles) {
            System.out.println("Replacing " + updateFile);
            new File(updateFile).delete();
            new File(updateFile + ".tmp").renameTo(new File(updateFile));
        }

        return true;
    }

    /**
     * This is a static-only class.
     */
    private Updater() {}
}
