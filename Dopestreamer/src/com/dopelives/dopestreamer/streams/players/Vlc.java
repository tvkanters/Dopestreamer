package com.dopelives.dopestreamer.streams.players;

import java.util.LinkedList;
import java.util.List;

/**
 * The VLC media player.
 */
public class Vlc extends MediaPlayer {

    /**
     * {@inheritDoc}
     */
    @Override
    public String getKey() {
        return "vlc";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getLabel() {
        return "VLC";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String getIconUrl() {
        return "vlc.png";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected List<String> getPaths() {
        final List<String> paths = new LinkedList<>();
        paths.add("C:\\Program Files (x86)\\VideoLAN\\VLC\\vlc.exe");
        paths.add("C:\\Program Files\\VideoLAN\\VLC\\vlc.exe");
        paths.add("/usr/bin/vlc");
        paths.add("/usr/local/bin/vlc");
        paths.add("/etc/vlc");
        paths.add("/usr/lib/vlc");
        paths.add("/usr/bin/X11/vlc");
        paths.add("/usr/share/vlc");
        return paths;
    }

}
