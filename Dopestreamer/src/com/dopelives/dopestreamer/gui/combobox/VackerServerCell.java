package com.dopelives.dopestreamer.gui.combobox;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import com.dopelives.dopestreamer.Environment;
import com.dopelives.dopestreamer.streams.services.Vacker;

/**
 * A combo box cell that shows the label and icons of stream services.
 */
public class VackerServerCell extends ComboBoxCell<Vacker.Server> {

    @Override
    public void updateItem(final Vacker.Server item, final boolean empty) {
        super.updateItem(item, empty);

        if (item == null) {
            return;
        }

        setText(item.getLabel());
        setGraphic(new ImageView(new Image(Environment.IMAGE_FOLDER + item.getIconUrl())));
    }
}