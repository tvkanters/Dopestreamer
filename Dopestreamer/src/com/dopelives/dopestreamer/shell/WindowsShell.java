package com.dopelives.dopestreamer.shell;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.HashSet;

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
    public Collection<ProcessId> getChildProcessIds(final ProcessId processId) {
        final Collection<ProcessId> processIds = new HashSet<>();
        final String result = executeCommandForResult("wmic process get processid,parentprocessid | find \""
                + processId + "\"");

        if (!result.equals("")) {
            final String[] children = result.split("\n+");

            // Search through the process results, but skip the header line
            for (int i = 0; i < children.length; ++i) {
                final ProcessId childId = new ProcessId(children[i].trim().split(" +")[1]);

                // The queried process ID can also be in the list if it's still open, so check for that
                if (!childId.equals(processId)) {
                    processIds.add(childId);
                }
            }
        }

        return processIds;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void stopProcess(final ProcessId processId) {
        executeCommand("taskkill /f /pid " + processId);
    }

}
