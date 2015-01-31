package com.dopelives.dopestreamer.shell;

import java.io.File;
import java.lang.reflect.Field;

import com.dopelives.dopestreamer.Environment;
import com.sun.jna.Pointer;
import com.sun.jna.Shell32X;
import com.sun.jna.WString;
import com.sun.jna.platform.win32.Kernel32;
import com.sun.jna.platform.win32.Kernel32Util;
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
    public boolean executeAsAdministrator(final String command, final String args) {
        final Shell32X.SHELLEXECUTEINFO execInfo = new Shell32X.SHELLEXECUTEINFO();
        execInfo.lpFile = new WString(command);
        if (args != null) execInfo.lpParameters = new WString(args);
        execInfo.nShow = Shell32X.SW_HIDE;
        execInfo.fMask = Shell32X.SEE_MASK_NOCLOSEPROCESS;
        execInfo.lpVerb = new WString("runas");
        final boolean success = Shell32X.INSTANCE.ShellExecuteEx(execInfo);

        if (!success) {
            final int lastError = Kernel32.INSTANCE.GetLastError();
            final String errorMessage = Kernel32Util.formatMessageFromLastErrorCode(lastError);
            System.err.println("Error performing elevation: " + lastError + ": " + errorMessage + " (apperror="
                    + execInfo.hInstApp + ")");
            return false;
        }

        return true;
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
    public String getLivestreamerPath() {
        String path = "";

        // Check if there's a bundled Livestreamer installation
        File livestreamerCheck = new File(Environment.EXE_DIR + "livestreamer.exe");
        if (livestreamerCheck.exists() && !livestreamerCheck.isDirectory()) {
            path = "\"" + Environment.EXE_DIR + "livestreamer.exe\"";
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
        File rtmpdumpCheck = new File("rtmpdump.exe");
        if (rtmpdumpCheck.exists() && !rtmpdumpCheck.isDirectory()) {
            additionalArguments += " -r ./rtmpdump.exe";
        } else {
            rtmpdumpCheck = new File(Environment.EXE_DIR + "rtmpdump.exe");
            if (rtmpdumpCheck.exists() && !rtmpdumpCheck.isDirectory()) {
                additionalArguments += " -r \"" + Environment.EXE_DIR + "rtmpdump.exe\"";
            }
        }

        return additionalArguments;
    }

    /**
     * @return The command for starting Dopestreamer
     */
    private String getDopestreamerCommand() {
        // Get the Dopestreamer file location
        final File dopestreamerFile = Environment.EXE_FILE;

        // Check if the right Dopestreamer file was found
        if (!dopestreamerFile.exists() || dopestreamerFile.isDirectory()) {
            System.err.println("Dopestreamer path is invalid: " + dopestreamerFile);
            return null;
        }

        // Get the command to start Dopestreamer
        String command = "\"" + dopestreamerFile.getAbsolutePath() + "\"";
        if (command.toLowerCase().endsWith(".jar\"")) {
            // Find the location of javaw.exe
            final String version = Registry.query("HKLM\\Software\\JavaSoft\\Java Runtime Environment",
                    "CurrentVersion");
            final String javaHome = Registry.query("HKLM\\Software\\JavaSoft\\Java Runtime Environment\\" + version,
                    "JavaHome");
            command = "\"" + javaHome + "\\bin\\javaw.exe\" -Dprism.dirtyopts=false -jar " + command;
        }

        return command;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isCustomProtocolSupported() {
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isCustomProtocolRegistered() {
        return Registry.query("HKEY_CLASSES_ROOT\\livestreamer", "") != null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean registerCustomProtocol() {
        final String dopestreamerCommand = getDopestreamerCommand();
        if (dopestreamerCommand == null) {
            return false;
        }

        final String command = dopestreamerCommand + " \"%1 best\"";

        // Register livestreamer://
        if (!Registry.addDefaultString("HKEY_CLASSES_ROOT\\livestreamer", "URL:livestreamer protocol")) return false;
        if (!Registry.addString("HKEY_CLASSES_ROOT\\livestreamer", "URL Protocol", "")) return false;
        if (!Registry.addDefaultString("HKEY_CLASSES_ROOT\\livestreamer\\Shell\\Open\\Command", command)) return false;

        // Register rtmp://
        if (!Registry.addDefaultString("HKEY_CLASSES_ROOT\\rtmp", "URL:livestreamer protocol")) return false;
        if (!Registry.addString("HKEY_CLASSES_ROOT\\rtmp", "URL Protocol", "")) return false;
        if (!Registry.addDefaultString("HKEY_CLASSES_ROOT\\rtmp\\Shell\\Open\\Command", command)) return false;

        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean unregisterCustomProtocol() {
        if (!Registry.delete("HKEY_CLASSES_ROOT\\livestreamer")) return false;
        if (!Registry.delete("HKEY_CLASSES_ROOT\\rtmp")) return false;

        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isStartOnBootSupported() {
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isStartOnBootRegistered() {
        return Registry.query("HKLM\\Software\\Microsoft\\Windows\\CurrentVersion\\Run", "Dopestreamer") != null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean registerStartOnBoot() {
        final String dopestreamerCommand = getDopestreamerCommand();
        if (dopestreamerCommand == null) {
            return false;
        }

        if (!Registry.addString("HKLM\\Software\\Microsoft\\Windows\\CurrentVersion\\Run", "Dopestreamer",
                dopestreamerCommand)) return false;

        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean unregisterStartOnBoot() {
        if (!Registry.delete("HKLM\\Software\\Microsoft\\Windows\\CurrentVersion\\Run", "Dopestreamer")) return false;

        return true;
    }
}
