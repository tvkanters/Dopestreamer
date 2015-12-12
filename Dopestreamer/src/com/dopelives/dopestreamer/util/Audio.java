package com.dopelives.dopestreamer.util;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.Line;
import javax.sound.sampled.LineEvent;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;

import com.dopelives.dopestreamer.Environment;

/**
 * A class that manages everything audio related.
 */
public class Audio {

    /**
     * Plays a notification sound.
     */
    public static void playNotification() {
        playSound(Pref.NOTIFICATION_DINGDONG.getBoolean() ? "dingdong.wav" : "notification.wav");
    }

    /**
     * Plays the sound matching the given name in the resource folder.
     *
     * @param resourceName
     *            The audio file to play
     */
    public static void playSound(final String resourceName) {
        try {
            final Clip clip = (Clip) AudioSystem.getLine(new Line.Info(Clip.class));

            // Make the the clip is freed after playing
            clip.addLineListener(event -> {
                if (event.getType() == LineEvent.Type.STOP) {
                    clip.close();
                }
            });

            // Add buffer for mark/reset support
            final InputStream bufferedAudioStream = new BufferedInputStream(
                    Audio.class.getResourceAsStream(Environment.AUDIO_FOLDER + resourceName));

            // Open and play the audio stream
            clip.open(AudioSystem.getAudioInputStream(bufferedAudioStream));
            clip.start();

        } catch (final RuntimeException | LineUnavailableException | IOException | UnsupportedAudioFileException ex) {
            // Audio could not be played, not big enough of a deal to do anything about it
            ex.printStackTrace(System.out);
        }
    }
}
