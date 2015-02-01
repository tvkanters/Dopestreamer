package com.dopelives.dopestreamer.gui.combobox;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import com.dopelives.dopestreamer.Environment;
import com.dopelives.dopestreamer.streams.services.StreamService;

/**
 * A combo box cell that shows the label and icons of stream services.
 */
public class StreamServiceCell extends ComboBoxCell<StreamService> {

    @Override
    public void updateItem(final StreamService item, final boolean empty) {
        super.updateItem(item, empty);

        if (item == null) {
            return;
        }

        setText(item.getLabel());
        setGraphic(new ImageView(new Image(Environment.IMAGE_FOLDER + item.getIconUrl())));
    }
}