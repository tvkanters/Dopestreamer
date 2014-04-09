package com.dopelives.dopestreamer.streams.players;

import java.util.LinkedList;
import java.util.List;

/**
 * The MPC-HC media player.
 */
public class MpcHc extends MediaPlayer {

    /**
     * {@inheritDoc}
     */
    @Override
    public String getKey() {
        return "mpchc";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getLabel() {
        return "MPC-HC";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String getIconUrl() {
        return "mpchc.png";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected List<String> getPaths() {
        final List<String> paths = new LinkedList<>();
        paths.add("C:\\Program Files\\MPC-HC\\mpc-hc.exe");
        paths.add("C:\\Program Files\\MPC-HC\\mpc-hc64.exe");
        paths.add("C:\\Program Files (x86)\\MPC-HC\\mpc-hc.exe");
        return paths;
    }

}
