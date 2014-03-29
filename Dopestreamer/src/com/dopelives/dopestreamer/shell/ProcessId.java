package com.dopelives.dopestreamer.shell;

/**
 * A primitive wrapper for process IDs according to Object Calisthenics rules.
 */
public class ProcessId {

    /** The PID value */
    public final int value;

    public ProcessId(final String value) {
        this.value = Integer.parseInt(value);
    }

    public ProcessId(final int value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return Integer.toString(value);
    }

    @Override
    public boolean equals(final Object obj) {
        if (!(obj instanceof ProcessId)) {
            return false;
        }
        return value == ((ProcessId) obj).value;
    }

    @Override
    public int hashCode() {
        return value;
    }
}
