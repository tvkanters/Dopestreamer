package com.dopelives.dopestreamer.util;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;

/**
 * Remembers all output for logging purposes.
 */
public class OutputSpy extends OutputStream {

    /** The maximum amount of lines to store */
    private static final int NUM_LINES = 25;
    /** The formatter for time prefixes per line */
    private static final SimpleDateFormat sTimeFormatter = new SimpleDateFormat("[HH:mm:ss] ");

    /** The original output stream to redirect output to */
    private final OutputStream mOutputStream;

    /** The output that has been seen so far as a list of bytes */
    private final List<Integer> mTempOutput = new LinkedList<>();
    /** The last seen lines with up to NUM_LINES entries */
    private final List<Integer[]> mSavedOutput = new ArrayList<>(NUM_LINES);

    /**
     * Remembers all output for logging purposes. Will still send the output to the original stream.
     *
     * @param outputStream
     *            The original output stream
     */
    public OutputSpy(final OutputStream outputStream) {
        mOutputStream = outputStream;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void write(final int b) throws IOException {
        // Print to console
        mOutputStream.write(b);

        // Split sentences on newlines
        if (b == 10 || b == 13) {
            if (mTempOutput.size() > 0) {
                // Make sure the output doesn't exceed the size limit
                if (mSavedOutput.size() == NUM_LINES) {
                    mSavedOutput.remove(0);
                }

                // Add time prefix
                final int[] time = sTimeFormatter.format(Calendar.getInstance().getTime()).chars().toArray();
                for (int i = 0; i < time.length; ++i) {
                    mTempOutput.add(i, time[i]);
                }

                // Store line
                mSavedOutput.add(mTempOutput.toArray(new Integer[mTempOutput.size()]));

                // Prepare for the next line
                mTempOutput.clear();
            }
        } else {
            // Add temp output for processing later
            mTempOutput.add(b);
        }
    }

    /**
     * Writes all saved output to a file.
     *
     * @param filename
     *            The relative filename of the file that should contain the output
     */
    public void writeToFile(final String filename) {
        try {
            final FileOutputStream out = new FileOutputStream(filename);
            for (final Integer[] line : mSavedOutput) {
                for (final Integer character : line) {
                    out.write(character);
                }
                out.write('\n');
            }
            out.close();

        } catch (final IOException ex) {
            ex.printStackTrace();
        }
    }
}
