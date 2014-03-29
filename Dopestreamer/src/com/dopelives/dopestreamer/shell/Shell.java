package com.dopelives.dopestreamer.shell;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.management.ManagementFactory;
import java.util.Collection;

/**
 * A base class for OS-specific shell functionality.
 */
public abstract class Shell implements ConsoleListener {

    /** The OS specific shell */
    private final static Shell sInstance;

    static {
        // Instantiate an OS specific implementation
        final String OS = System.getProperty("os.name", "generic").toLowerCase();
        if ((OS.indexOf("mac") >= 0) || (OS.indexOf("darwin") >= 0)) {
            sInstance = null;
        } else if (OS.indexOf("win") >= 0) {
            sInstance = new WindowsShell();
        } else if (OS.indexOf("nux") >= 0) {
            sInstance = null;
        } else {
            sInstance = null;
        }

        if (sInstance == null) {
            throw new UnsupportedOperationException("Your operating system is not supported");
        }
    }

    /**
     * @return The singleton instance of the OS specific shell
     */
    public static Shell getInstance() {
        return sInstance;
    }

    /**
     * @return The PID of the running process
     */
    public ProcessId getJvmProcessId() {
        final String jvmName = ManagementFactory.getRuntimeMXBean().getName();
        final int end = jvmName.indexOf('@');

        if (end == -1) {
            return new ProcessId(jvmName);
        }

        return new ProcessId(jvmName.substring(0, end));
    }

    /**
     * Retrieves the PID from the given process.
     *
     * @param process
     *            The process to find the PID for
     *
     * @return The PID
     */
    public abstract ProcessId getProcessId(final Process process);

    /**
     * Executes the shell command given and waits for it to finish so that it may return the result. Lines are separated
     * by \n character. The returned value does not end with a \n char.
     *
     * @param command
     *            The command to execute.
     *
     * @return The output of the shell command
     */
    public String executeCommandForResult(final String command) {
        try {
            final ProcessBuilder builder = getProcessBuilder(command);
            builder.redirectErrorStream(true);
            final Process process = builder.start();
            final BufferedReader in = new BufferedReader(new InputStreamReader(process.getInputStream()));

            final StringBuilder output = new StringBuilder();
            String line;
            while ((line = in.readLine()) != null) {
                output.append(line + "\n");
            }
            process.waitFor();
            in.close();

            return output.toString().trim();

        } catch (final IOException | InterruptedException ex) {
            throw new RuntimeException("Couldn't execute command", ex);
        }
    }

    /**
     * Creates a console that can execute shell commands in the current OS.
     *
     * @param command
     *            The command to execute
     *
     * @return The console that can execute the command
     */
    public Console createConsole(final String command) {
        final Console console = new Console(getProcessBuilder(command));
        console.addListener(this);
        return console;
    }

    /**
     * Creates a process builder for the given command.
     *
     * @param command
     *            The command that can be executed within a console
     *
     * @return The process builder to execute the command
     */
    protected abstract ProcessBuilder getProcessBuilder(String command);

    /**
     * {@inheritDoc}
     */
    @Override
    public void onConsoleOutput(final ProcessId processId, final String output) {}

    /**
     * {@inheritDoc}
     *
     * Recursively stops child processes of the stopped console.
     */
    @Override
    public void onConsoleStop(final ProcessId processId) {
        for (final ProcessId child : getChildProcessIds(processId)) {
            stopProcess(child);
            onConsoleStop(child);
        }
    }

    /**
     * Retrieves the child processes for the process with the given PID.
     *
     * @param processId
     *            The process ID so seek children from
     *
     * @return The PIDs with the given PID as parent
     */
    public abstract Collection<ProcessId> getChildProcessIds(ProcessId processId);

    /**
     * Forcefully stops the process with the given PID.
     *
     * @param processId
     *            The PID of the process to stop
     */
    public abstract void stopProcess(ProcessId processId);
}
