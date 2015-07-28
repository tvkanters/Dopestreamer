package com.dopelives.dopestreamer.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.Charset;

/**
 * A helper class for HTTP requests.
 */
public class HttpHelper {

    /** The HTTP timeout in milliseconds */
    private static int HTTP_TIMEOUT = 5000;

    static {
        // Allows requests to timeout
        System.setProperty("http.keepAlive", "false");
    }

    /**
     * Retrieves the content from a URL.
     *
     * @param url
     *            The URL to request
     *
     * @return The contents or null if the URL couldn't be read
     */
    public static String getContent(final String url) {
        final StringBuilder result = new StringBuilder();
        try {
            final URLConnection connection = new URL(url).openConnection();
            connection.setRequestProperty("Connection", "close");
            connection.setConnectTimeout(HTTP_TIMEOUT);
            connection.setReadTimeout(HTTP_TIMEOUT);
            final BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream(),
                    Charset.forName("UTF-8")));
            String line;
            while ((line = reader.readLine()) != null) {
                result.append(line + "\n");
            }
            reader.close();
        } catch (final SocketTimeoutException ex) {
            System.out.println("Timeout while loading URL " + url + ": " + ex.getClass());
            return null;
        } catch (final IOException ex) {
            System.out.println("Couldn't load URL " + url + ": " + ex.getClass());
            return null;
        }

        return result.toString();
    }
}
