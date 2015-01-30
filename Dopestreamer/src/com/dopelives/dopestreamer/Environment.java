package com.dopelives.dopestreamer;

import java.io.File;
import java.io.PrintStream;

import javafx.application.Application;

import com.dopelives.dopestreamer.gui.StageManager;
import com.dopelives.dopestreamer.shell.Shell;
import com.dopelives.dopestreamer.streams.StreamManager;
import com.dopelives.dopestreamer.util.OutputSpy;
import com.dopelives.dopestreamer.util.Pref;

/**
 * The initialiser and general environment manager.
 */
public class Environment {

    /** The current version without prefix */
    public static final String VERSION = "1.7.2";

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
    /** The directory of the executable file running Dopestreamer */
    public static final String EXE_DIR = new File(Environment.class.getProtectionDomain().getCodeSource().getLocation()
            .getPath()).getParentFile().toString()
            + File.separator;

    /** Remembers all output for logging purposes */
    private static final OutputSpy sOutputSpy = new OutputSpy(System.out);

    public static void main(final String[] args) {
        System.setOut(new PrintStream(sOutputSpy));
        System.setErr(new PrintStream(sOutputSpy));

        TrayManager.getInstance().show();

        if (Pref.AUTO_START.getBoolean()) {
            StreamManager.getInstance().restartLastStream();
        }

        Application.launch(StageManager.class, args);
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
