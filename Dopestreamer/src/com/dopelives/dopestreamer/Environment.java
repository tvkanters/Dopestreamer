package com.dopelives.dopestreamer;

import java.io.File;
import java.io.PrintStream;

import javafx.application.Application;

import com.dopelives.dopestreamer.gui.StageManager;
import com.dopelives.dopestreamer.shell.Console;
import com.dopelives.dopestreamer.shell.Shell;
import com.dopelives.dopestreamer.streams.Stream;
import com.dopelives.dopestreamer.streams.StreamManager;
import com.dopelives.dopestreamer.util.OutputSpy;
import com.dopelives.dopestreamer.util.Pref;

/**
 * The initialiser and general environment manager.
 */
public class Environment {

    /** The current version without prefix */
    public static final String VERSION = "1.8.0";

    /** The absolute path to the folder containing the GUI resources */
    public static final String RESOURCE_FOLDER = "/com/dopelives/dopestreamer/res/";
    /** The absolute path to the folder containing the GUI layout resources */
    public static final String LAYOUT_FOLDER = RESOURCE_FOLDER + "layout/";
    /** The absolute path to the folder containing the GUI image resources */
    public static final String IMAGE_FOLDER = RESOURCE_FOLDER + "images/";
    /** The absolute path to the folder containing the GUI CSS resources */
    public static final String STYLE_FOLDER = RESOURCE_FOLDER + "style/";
    /** The absolute path to the folder containing the audio resources */
    public static final String AUDIO_FOLDER = RESOURCE_FOLDER + "audio/";
    /** The application's title */
    public static final String TITLE = "Dopestreamer";
    /** The location of the executable file running Dopestreamer */
    public static final File EXE_FILE = new File(Environment.class.getProtectionDomain().getCodeSource().getLocation()
            .getPath());
    /** The directory of the executable file running Dopestreamer */
    public static final String EXE_DIR = EXE_FILE.getParentFile().toString() + File.separator;

    /** Remembers all output for logging purposes */
    private static final OutputSpy sOutputSpy = new OutputSpy(System.out);

    public static void main(final String[] args) {
        System.setOut(new PrintStream(sOutputSpy));
        System.setErr(new PrintStream(sOutputSpy));

        // If arguments were provided, launch Livestreamer and exit
        if (args.length > 0) {
            handleArguments(args);
            return;
        }

        // Boot the GUI and start the stream if needed
        TrayManager.getInstance().show();

        if (Pref.AUTO_START.getBoolean()) {
            StreamManager.getInstance().restartLastStream();
        }

        Application.launch(StageManager.class, args);
    }

    /**
     * Handles arguments if they were provided. Currently only supports launching Livestreamer as a protocol middle man.
     *
     * @param args
     *            The command line arguments provided
     */
    private static void handleArguments(final String[] args) {
        final Shell shell = Shell.getInstance();

        // Construct the Livestreamer command
        String command = shell.getLivestreamerPath();

        // Add OS specific arguments
        final String additionalArguments = shell.getAdditionalLivestreamerArguments();
        if (!additionalArguments.equals("")) {
            command += " " + additionalArguments.trim();
        }

        // Add a custom player location, if any
        command += Stream.getMediaPlayerArgument();

        // Concatenate arguments into one string
        String livestreamerArgs = "";
        for (int i = 0; i < args.length; i++) {
            livestreamerArgs += args[i] + " ";
        }

        // Strip the Livestreamer protocol
        command += " " + livestreamerArgs.replaceAll("(?i)^livestreamer:(//)?", "").trim();

        // Start Livestreamer with the provided arguments
        final Console console = shell.createConsole(command);
        console.start();
    }

    /**
     * Closes the JVM and its child processes.
     */
    public static void exit() {
        TrayManager.getInstance().hide();

        final Shell shell = Shell.getInstance();
        shell.killProcessTree(shell.getJvmProcessId());
    }

    /**
     * @return The reader that remembers all output for logging purposes
     */
    public static OutputSpy getOutputSpy() {
        return sOutputSpy;
    }

    /**
     * This is a static-only class.
     */
    private Environment() {}

}
