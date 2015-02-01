package com.dopelives.dopestreamer.streams.players;

import java.util.LinkedList;
import java.util.List;

/**
 * The mpv media player.
 */
public class Mpv extends MediaPlayer {

    /**
     * {@inheritDoc}
     */
    @Override
    public String getKey() {
        return "mpv";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getLabel() {
        return "mpv";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getIconUrl() {
        return "players/mpv.png";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected List<String> getPaths() {
        final List<String> paths = new LinkedList<>();
        paths.add("C:\\Program Files\\mpv\\mpv.exe");
        paths.add("C:\\Program Files (x86)\\mpv\\mpv.exe");
        paths.add("C:\\mplayer2\\mplayer2.exe");
        paths.add("/usr/bin/mpv");
        paths.add("/usr/local/bin/mpv");
        paths.add("/etc/mpv");
        paths.add("/usr/lib/mpv");
        paths.add("/usr/bin/X11/mpv");
        paths.add("/usr/share/mpv");
        paths.add("/usr/bin/mplayer2");
        paths.add("/usr/local/bin/mplayer2");
        paths.add("/etc/mplayer2");
        paths.add("/usr/lib/mplayer2");
        paths.add("/usr/bin/X11/mplayer2");
        paths.add("/usr/share/mplayer2");
        return paths;
    }

}
