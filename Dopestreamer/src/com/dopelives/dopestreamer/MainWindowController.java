package com.dopelives.dopestreamer;

import java.net.URL;
import java.util.ResourceBundle;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.util.Callback;

public class MainWindowController implements Initializable {

    @FXML
    private RadioButton channelCustom;
    @FXML
    private TextField channelCustomInput;
    @FXML
    private ComboBox<StreamService> streamServiceSelection;

    @Override
    public void initialize(final URL location, final ResourceBundle resources) {
        // Select the custom channel radio button upon focusing the text field next to it
        channelCustomInput.focusedProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(final ObservableValue<? extends Boolean> observable, final Boolean oldValue,
                    final Boolean newValue) {
                if (newValue) {
                    channelCustom.setSelected(true);
                }
            }
        });

        // Add stream services to the combo box
        final ObservableList<StreamService> streamServices = streamServiceSelection.getItems();
        streamServices.add(new StreamService("Hitbox", "hitbox.png"));
        streamServices.add(new StreamService("Twitch", "twitch.png"));

        // Make the stream services look nice within the combo box
        streamServiceSelection.setButtonCell(new StreamServiceCell());
        streamServiceSelection.setCellFactory(new Callback<ListView<StreamService>, ListCell<StreamService>>() {
            @Override
            public ListCell<StreamService> call(final ListView<StreamService> param) {
                return new StreamServiceCell();
            }
        });

        // Select the first stream service by default
        streamServiceSelection.setValue(streamServices.get(0));
    }

    @FXML
    protected void onLiveClicked(final ActionEvent event) {
        // Start stream
    }

    /**
     * The data class for a stream service that can be selected in the combo box.
     */
    private class StreamService {
        /** The label to show in the combo box */
        public final String label;
        /** The icon to show in the combo box */
        public final String icon;

        public StreamService(final String label, final String icon) {
            this.label = label;
            this.icon = icon;
        }
    }

    /**
     * A combo box cell that shows the label and icon of stream services.
     */
    private class StreamServiceCell extends ListCell<StreamService> {
        @Override
        public void updateItem(final StreamService item, final boolean empty) {
            super.updateItem(item, empty);

            if (item == null) {
                return;
            }

            setText(item.label);
            setGraphic(new ImageView(new Image(Initialiser.IMAGE_FOLDER + item.icon)));
        }
    }
}
