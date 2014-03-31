package com.dopelives.dopestreamer;

import javafx.application.Application;

import com.dopelives.dopestreamer.gui.StageManager;
import com.dopelives.dopestreamer.shell.Shell;

/**
 * The initialiser and general environment manager.
 */
public class Environment {

    /** The absolute path to the folder containing the GUI resources */
    public static final String RESOURCE_FOLDER = "/com/dopelives/dopestreamer/res/";
    /** The absolute path to the folder containing the GUI image resources */
    public static final String IMAGE_FOLDER = RESOURCE_FOLDER + "images/";
    /** The application's title */
    public static final String TITLE = "Dopestreamer";

    /** The current version without prefix */
    public static final String VERSION = "0.3";

    public static void main(final String[] args) {
        TrayManager.show();

        Application.launch(StageManager.class, args);
    }

    /**
     * Closes the JVM and its child processes.
     */
    public static void exit() {
        TrayManager.hide();

        final Shell shell = Shell.getInstance();
        shell.killProcessTree(shell.getJvmProcessId());
    }

    /**
     * This is a static-only class.
     */
    private Environment() {}

}
