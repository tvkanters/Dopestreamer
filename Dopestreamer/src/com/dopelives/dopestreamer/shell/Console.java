package com.dopelives.dopestreamer.shell;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;

/**
 * A console to execute commands in.
 */
public class Console {

    /** The number counting the amount of consoles opened */
    private static int sConsoleCount = 0;

    /** The listeners to receive updates of this console */
    private final Collection<ConsoleListener> mListeners = new HashSet<>();

    /** The prefix to use for printing */
    final private String mPrefix;

    /** The builder that can start the process */
    private final ProcessBuilder mBuilder;
    /** The process in which the command is executed */
    private Process mProcess;
    /** The PID of the console process */
    private ProcessId mProcessId;

    /** Whether or not the console is currently starting the process */
    private boolean mStarting = false;
    /** Whether or not the console is currently running */
    private boolean mRunning = false;
    /** Whether or not the console has ran and stopped */
    private boolean mStopped = false;

    /**
     * Starts a new console while executing the specified command within a shell.
     *
     * @param command
     *            The command to execute
     */
    /* default */Console(final ProcessBuilder builder) {
        // Keep track of which console we're using
        mPrefix = "[" + ++sConsoleCount + "] ";

        mBuilder = builder;
        mBuilder.redirectErrorStream(true);
    }

    /**
     * Executes the given command. May not be called while the process is already active.
     */
    public void start() {
        if (mStarting || mRunning || mStopped) {
            throw new IllegalStateException("Console already executed");
        }
        mStarting = true;
        System.out.println(mPrefix + "START");

        // Execute the command
        final Thread mainThread = Thread.currentThread();
        new Thread(new ProcessRunner(mainThread)).start();

        // Wait for the process to be started (doesn't require command to be finish)
        synchronized (mainThread) {
            if (mStarting) {
                try {
                    mainThread.wait();
                } catch (final InterruptedException ex) {
                    ex.printStackTrace();
                }
            }
        }
    }

    /**
     * Forcefully stops the running process.
     */
    public void stop() {
        if (mProcessId != null) {
            Shell.getInstance().killProcessTree(mProcessId);
        }
    }

    /**
     * The runnable containing the process to run in parallel.
     */
    private class ProcessRunner implements Runnable {

        /** The parent thread for this process */
        final Thread mParentThread;
        /** The thread that reads the process's output */
        final Thread mOutputThread;
        /** The stream from which to get the results */
        private BufferedReader mOutput;

        /**
         * Prepares a new thread that a process can run in.
         *
         * @param parent
         *            The thread starting this one
         */
        public ProcessRunner(final Thread parent) {
            mParentThread = parent;
            mOutputThread = new Thread(new OutputReader());
        }

        @Override
        public void run() {
            try {
                // Start process and retrieve streams
                mProcess = mBuilder.start();
                mProcessId = Shell.getInstance().getProcessId(mProcess);
                mOutput = new BufferedReader(new InputStreamReader(mProcess.getInputStream()));

                // Keep track of the output
                mRunning = true;
                mOutputThread.start();

                // Notify that the process has started
                synchronized (mParentThread) {
                    mStarting = false;
                    mParentThread.notify();
                }

                // Wait for the process to finish
                try {
                    mProcess.waitFor();
                } catch (final InterruptedException ex) {
                    ex.printStackTrace();
                }

                // Shut down the console
                mRunning = false;
                mStopped = true;
                mOutput.close();

                // Inform the listeners that the process has stopped
                for (final ConsoleListener listener : mListeners) {
                    listener.onConsoleStop(mProcessId);
                }

                System.out.println(mPrefix + "STOP");

            } catch (final IOException ex) {
                ex.printStackTrace();
            }
        }

        /**
         * Reads the output from the process and prints them.
         */
        private class OutputReader implements Runnable {
            @Override
            public void run() {
                while (mRunning) {
                    try {
                        final String line = mOutput.readLine();
                        if (line != null) {
                            System.out.println(mPrefix + line);
                            for (final ConsoleListener listener : mListeners) {
                                listener.onConsoleOutput(mProcessId, line);
                            }
                        }
                    } catch (final IOException ex) {
                        ex.printStackTrace();
                    }
                }
            }
        }
    }

    /**
     * Adds a listener to this console that will receive call-backs on console events such as output and stop.
     *
     * @param listener
     *            The listener that will receive the call-backs
     */
    public void addListener(final ConsoleListener listener) {
        mListeners.add(listener);
    }

    /**
     * @return The listeners that will receive the call-backs
     */
    public Collection<ConsoleListener> getListeners() {
        return Collections.unmodifiableCollection(mListeners);
    }

    /**
     * @return The process ID of this console
     */
    public ProcessId getProcessId() {
        return mProcessId;
    }

    /**
     * @return True iff the console process is running
     */
    public boolean isRunning() {
        return mRunning;
    }
}
