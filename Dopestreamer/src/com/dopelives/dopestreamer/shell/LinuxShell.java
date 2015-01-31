package com.dopelives.dopestreamer.shell;

import java.io.File;
import java.lang.reflect.Field;

import com.dopelives.dopestreamer.Environment;

/**
 * A class for Linux-specific shell functionality.
 */
public class LinuxShell extends Shell {

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
        String path = "";

        // Check if there's a bundled Livestreamer installation
        File livestreamerCheck = new File(Environment.EXE_DIR + "livestreamer");
        if (livestreamerCheck.exists() && !livestreamerCheck.isDirectory()) {
            path = "\"" + Environment.EXE_DIR + "livestreamer\"";
        } else {
            livestreamerCheck = new File(Environment.EXE_DIR + "livestreamer");
            if (livestreamerCheck.exists() && !livestreamerCheck.isDirectory()) {
                path = "\"" + Environment.EXE_DIR + "livestreamer\"";
            } else {
                path = "livestreamer";
            }
        }

        return path;
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

}
