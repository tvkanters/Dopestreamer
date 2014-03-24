package com.dopelives.dopestreamer.combobox;

import javafx.scene.image.ImageView;

import com.dopelives.dopestreamer.streamservices.StreamService;

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
        setGraphic(new ImageView(item.getIcon()));
    }
}