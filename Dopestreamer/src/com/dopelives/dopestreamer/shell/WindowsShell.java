package com.dopelives.dopestreamer.shell;

import java.io.File;
import java.lang.reflect.Field;

import com.sun.jna.Pointer;
import com.sun.jna.platform.win32.Kernel32;
import com.sun.jna.platform.win32.WinNT;

/**
 * A class for Windows-specific shell functionality.
 */
public class WindowsShell extends Shell {

    protected WindowsShell() {
        super();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ProcessId getProcessId(final Process process) {
        try {
            final Field f = process.getClass().getDeclaredField("handle");
            f.setAccessible(true);
            final long handleId = f.getLong(process);

            final WinNT.HANDLE handle = new WinNT.HANDLE();
            handle.setPointer(Pointer.createConstant(handleId));
            return new ProcessId(Kernel32.INSTANCE.GetProcessId(handle));

        } catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException ex) {
            throw new RuntimeException("Could not retrieve PID", ex);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected ProcessBuilder getProcessBuilder(final String command) {
        return new ProcessBuilder("cmd", "/C", command);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void killProcessTree(final ProcessId processId) {
        executeCommand("taskkill /f /t /pid " + processId);
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
