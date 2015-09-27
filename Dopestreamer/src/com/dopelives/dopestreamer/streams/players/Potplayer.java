package com.dopelives.dopestreamer.streams.players;

import java.util.LinkedList;
import java.util.List;

/**
 * The VLC media player.
 */
public class Potplayer extends MediaPlayer {

    /**
     * {@inheritDoc}
     */
    @Override
    public String getKey() {
        return "potplayer";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getLabel() {
        return "Potplayer";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getIconUrl() {
        return "players/potplayer.png";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected List<String> getPaths() {
        final List<String> paths = new LinkedList<>();
        paths.add("C:\\Program Files\\DAUM\\PotPlayer\\PotPlayerMini64.exe");
        paths.add("C:\\Program Files\\DAUM\\PotPlayer\\PotPlayerMini.exe");
        paths.add("C:\\Program Files\\DAUM\\PotPlayer\\PotPlayerMini32.exe");
        paths.add("C:\\Program Files (x86)\\DAUM\\PotPlayer\\PotPlayerMini.exe");
        paths.add("C:\\Program Files (x86)\\DAUM\\PotPlayer\\PotPlayerMini32.exe");
        return paths;
    }

}
