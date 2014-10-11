package com.dopelives.dopestreamer;

import java.io.BufferedInputStream;
import java.io.InputStream;

import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.Line;
import javax.sound.sampled.LineEvent;
import javax.sound.sampled.LineListener;

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
            clip.addLineListener(new LineListener() {
                @Override
                public void update(final LineEvent event) {
                    if (event.getType() == LineEvent.Type.STOP) {
                        clip.close();
                    }
                }
            });

            // Add buffer for mark/reset support
            final InputStream bufferedAudioStream = new BufferedInputStream(
                    Audio.class.getResourceAsStream(Environment.AUDIO_FOLDER + resourceName));

            // Open and play the audio stream
            clip.open(AudioSystem.getAudioInputStream(bufferedAudioStream));
            clip.start();
        } catch (final Exception exc) {
            exc.printStackTrace(System.out);
        }
    }
}
