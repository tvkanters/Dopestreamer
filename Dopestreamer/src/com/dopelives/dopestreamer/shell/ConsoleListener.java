package com.dopelives.dopestreamer.shell;

/**
 * The interface for an object that will be notified of console changes.
 */
public interface ConsoleListener {

    /**
     * Called when the console retrieves output from the executed command. Called for each line.
     *
     * @param processId
     *            The PID of the stopped process
     * @param output
     *            The console output
     */
    void onConsoleOutput(ProcessId processId, String output);

    /**
     * Called when the process of the console has stopped, either forcefully or through normal execution.
     *
     * @param processId
     *            The PID of the stopped process
     */
    void onConsoleStop(ProcessId processId);
}
