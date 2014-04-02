package com.dopelives.dopestreamer;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.LinkedList;
import java.util.List;

/**
 * Remembers all output for logging purposes.
 */
public class OutputSpy extends OutputStream {

    /** The original output stream to redirect output to */
    private final OutputStream mOutputStream;

    /** The output that has been seen so far as a list of bytes */
    private final List<Integer> mSavedOutput = new LinkedList<>();

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
        mSavedOutput.add(b);
        mOutputStream.write(b);
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
            out.write(getSavedOutputAsBytes());
            out.close();

        } catch (final IOException ex) {
            ex.printStackTrace();
        }
    }

    /**
     * @return The saved output converted to a byte array
     */
    private byte[] getSavedOutputAsBytes() {
        final byte[] bytes = new byte[mSavedOutput.size()];
        int i = 0;
        for (final Integer byteInt : mSavedOutput) {
            bytes[i++] = byteInt.byteValue();
        }
        return bytes;
    }
}
