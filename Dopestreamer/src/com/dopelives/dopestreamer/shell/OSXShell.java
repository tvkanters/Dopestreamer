package com.dopelives.dopestreamer.shell;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;

/**
 * A class for OSX-specific shell functionality.
 */
public class OSXShell extends Shell {

    protected OSXShell() {
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
            int id = f.getInt(process);
            return new ProcessId(id);

        } catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException ex) {
            throw new RuntimeException("Could not retrieve PID", ex);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected ProcessBuilder getProcessBuilder(final String command) {
        String[] commands = command.split(" +");
        return new ProcessBuilder(commands);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void killProcessTree(final ProcessId processId) {
        // Get LSer process
        String child = executeCommandForResult("pgrep -P " + processId);
        if (!child.equals("")) {
            final String[] childIds = child.trim().split("\\s+");
            for (final String childId : childIds) {
                killProcessTree(new ProcessId(childId));
            }
        }
        executeCommand("kill " + processId);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getAdditionalLivestreamerArguments() {
        String additionalArguments = "";

        // Add the rtfmdump argument if the file is found next to the JAR
        final File rtmpdumpCheck = new File("rtmpdump.exe");
        if (rtmpdumpCheck.exists() && !rtmpdumpCheck.isDirectory()) {
            additionalArguments += "-r ./rtmpdump.exe";
        }

        return additionalArguments;
    }

}
