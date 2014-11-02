package com.dopelives.dopestreamer.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.Charset;

/**
 * A helper class for HTTP requests.
 */
public class HttpHelper {

    /**
     * Retrieves the content from a URL.
     * 
     * @param url
     *            The URL to request
     * 
     * @return The contents
     */
    public static String getContent(final String url) {
        final StringBuilder result = new StringBuilder();
        try {
            final URLConnection connection = new URL(url).openConnection();
            final BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream(),
                    Charset.forName("UTF-8")));
            String line;
            while ((line = reader.readLine()) != null) {
                result.append(line + "\n");
            }
            reader.close();
        } catch (final IOException ex) {
            ex.printStackTrace();
            return null;
        }

        return result.toString();
    }
}
