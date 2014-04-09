package com.dopelives.dopestreamer.gui.combobox;

import javafx.scene.image.ImageView;

import com.dopelives.dopestreamer.streams.players.MediaPlayer;

/**
 * A combo box cell that shows the label and icons of stream services.
 */
public class MediaPlayerCell extends ComboBoxCell<MediaPlayer> {

    @Override
    public void updateItem(final MediaPlayer item, final boolean empty) {
        super.updateItem(item, empty);

        if (item == null) {
            return;
        }

        setText(item.getLabel());
        setGraphic(new ImageView(item.getIcon()));
    }
}