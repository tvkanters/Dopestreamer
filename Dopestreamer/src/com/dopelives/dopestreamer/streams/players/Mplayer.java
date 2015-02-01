package com.dopelives.dopestreamer.streams.players;

import java.util.LinkedList;
import java.util.List;

/**
 * The MPlayer media player.
 */
public class Mplayer extends MediaPlayer {

    /**
     * {@inheritDoc}
     */
    @Override
    public String getKey() {
        return "mplayer";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getLabel() {
        return "MPlayer";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getIconUrl() {
        return "players/mplayer.png";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected List<String> getPaths() {
        final List<String> paths = new LinkedList<>();
        paths.add("C:\\Program Files\\MPlayer\\mplayer.exe");
        paths.add("C:\\Program Files (x86)\\MPlayer\\mplayer.exe");
        paths.add("C:\\mplayer\\mplayer.exe");
        paths.add("/usr/bin/mplayer");
        paths.add("/usr/local/bin/mplayer");
        paths.add("/etc/mplayer");
        paths.add("/usr/lib/mplayer");
        paths.add("/usr/bin/X11/mplayer");
        paths.add("/usr/share/mplayer");
        return paths;
    }

}
