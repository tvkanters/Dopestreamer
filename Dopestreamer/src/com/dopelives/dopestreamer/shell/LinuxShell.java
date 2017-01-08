package com.dopelives.dopestreamer.shell;

import java.io.File;
import java.lang.reflect.Field;

import com.dopelives.dopestreamer.Environment;

/**
 * A class for Linux-specific shell functionality.
 */
public class LinuxShell extends Shell {

    private static final String[] LIVESTREAMER_PATHS = { Environment.EXE_DIR + "streamlink",
            Environment.EXE_DIR + "livestreamer" };

    protected LinuxShell() {
        super();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ProcessId getProcessId(final Process process) {
        try {
            final Field f = process.getClass().getDeclaredField("pid");
            f.setAccessible(true);
            return new ProcessId(f.getInt(process));

        } catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException ex) {
            throw new RuntimeException("Could not retrieve PID", ex);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean executeAsAdministrator(final String command, final String args) {
        throw new UnsupportedOperationException("Executing as administrator is not supported on *nix systems");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected ProcessBuilder getProcessBuilder(final String command) {
        return new ProcessBuilder(command.split(" +"));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void killProcessTree(final ProcessId processId) {
        // Get child processes
        final String child = executeCommandForResult("pgrep -P " + processId);
        if (!child.equals("")) {
            // Close each child process
            final String[] childIds = child.trim().split("\\s+");
            for (final String childId : childIds) {
                killProcessTree(new ProcessId(childId));
            }
        }

        // Close current process
        executeCommand("kill " + processId);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getLivestreamerPath() {
        for (final String path : LIVESTREAMER_PATHS) {
            final File file = new File(path);
            if (file.exists() && !file.isDirectory()) {
                return "\"" + path + "\"";
            }
        }

        return "streamlink";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getAdditionalLivestreamerArguments() {
        String additionalArguments = "";

        // Add the RTMPdump argument if the file is found next to the JAR
        File rtmpdumpCheck = new File("rtmpdump");
        if (rtmpdumpCheck.exists() && !rtmpdumpCheck.isDirectory()) {
            additionalArguments += " -r ./rtmpdump";
        } else {
            rtmpdumpCheck = new File(Environment.EXE_DIR + "rtmpdump");
            if (rtmpdumpCheck.exists() && !rtmpdumpCheck.isDirectory()) {
                additionalArguments += " -r \"" + Environment.EXE_DIR + "rtmpdump\"";
            }
        }

        return additionalArguments;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isCustomProtocolSupported() {
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isCustomProtocolRegistered() {
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean registerCustomProtocol() {
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean unregisterCustomProtocol() {
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isStartOnBootSupported() {
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isStartOnBootRegistered() {
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean registerStartOnBoot() {
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean unregisterStartOnBoot() {
        return false;
    }

}
