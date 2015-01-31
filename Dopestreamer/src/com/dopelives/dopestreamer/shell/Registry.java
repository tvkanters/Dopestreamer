package com.dopelives.dopestreamer.shell;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A class used for interaction with the Windows registry.
 */
public class Registry {

    /** A regex for parsing the query results */
    private static final Pattern sQueryParser = Pattern.compile("^[^\n\r]*[\n\r] *[\\w()]+ +\\w+ +");

    /**
     * Queries the registry for a value.
     *
     * @param path
     *            The path of the registry key
     * @param key
     *            The key to find the value of or an empty key for the (Default) value
     *
     * @return The value or null if the key did not exist
     */
    public static String query(final String path, final String key) {
        final String result = Shell.getInstance().executeCommandForResult(
                "REG QUERY \"" + path + "\" /v \"" + key + "\"");

        final Matcher matcher = sQueryParser.matcher(result);
        if (!matcher.find()) {
            return null;
        }

        return matcher.replaceAll("");
    }

    /**
     * Adds a (Default) value for the given path.
     *
     * @param path
     *            The path
     * @param value
     *            The string value to add as the (Default) entry
     *
     * @return True iff the operation was successful
     */
    public static boolean addDefaultString(final String path, final String value) {
        final String command = "ADD " + path + " /f /ve /t REG_SZ /d \"" + value.replace("\"", "\\\"") + "\"";

        if (!Shell.getInstance().executeAsAdministrator("reg.exe", command)) {
            System.err.println("Failed to perform registry operation: " + command);
            return false;
        }

        return true;
    }

    /**
     * Adds a key and value for the given path
     *
     * @param path
     *            The path of the key
     * @param key
     *            The key to assign a value
     * @param value
     *            The value to assign
     *
     * @return True iff the operation was successful
     */
    public static boolean addString(final String path, final String key, final String value) {
        final String command = "ADD " + path + " /f /v \"" + key.replace("\"", "\\\"") + "\" /t REG_SZ /d \""
                + value.replace("\"", "\\\"") + "\"";

        if (!Shell.getInstance().executeAsAdministrator("reg.exe", command)) {
            System.err.println("Failed to perform registry operation: " + command);
            return false;
        }

        return true;
    }

    /**
     * Deletes a registry item and all its sub-items. USE WITH EXTREME CAUTION!
     *
     * @param path
     *            The path to delete
     *
     * @return True iff the operation was successful
     */
    public static boolean delete(final String path) {
        final String command = "DELETE " + path + " /f";

        if (!Shell.getInstance().executeAsAdministrator("reg.exe", command)) {
            System.err.println("Failed to perform registry operation: " + command);
            return false;
        }

        return true;
    }

    /**
     * This is a static-only class.
     */
    private Registry() {}
}
