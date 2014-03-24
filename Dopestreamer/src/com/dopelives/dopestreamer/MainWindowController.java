package com.dopelives.dopestreamer;

import java.net.URL;
import java.security.InvalidParameterException;
import java.util.ResourceBundle;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.util.Callback;

import com.dopelives.dopestreamer.streamservices.StreamService;
import com.dopelives.dopestreamer.streamservices.StreamServiceManager;

public class MainWindowController implements Initializable {

    @FXML
    private RadioButton channelCustom;
    @FXML
    private TextField channelCustomInput;
    @FXML
    private ComboBox<StreamService> streamServiceSelection;
    @FXML
    private TextField streamQuality;

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

        // Add stream services to the combo box and select the first
        streamServiceSelection.getItems().addAll(StreamServiceManager.getStreamServices());
        streamServiceSelection.getSelectionModel().select(0);

        // Make the stream services look nice within the combo box
        streamServiceSelection.setButtonCell(new StreamServiceCell());
        streamServiceSelection.setCellFactory(new Callback<ListView<StreamService>, ListCell<StreamService>>() {
            @Override
            public ListCell<StreamService> call(final ListView<StreamService> param) {
                return new StreamServiceCell();
            }
        });

    }

    @FXML
    protected void onLiveClicked(final ActionEvent event) {
        // Start stream
        final StreamService selectedStreamService = streamServiceSelection.getValue();
        if (channelCustom.isSelected()) {
            try {
                StreamServiceManager.startStream(selectedStreamService, channelCustomInput.getText());
            } catch (final InvalidParameterException ex) {
                // TODO: Tell user that a channel must be provided
            }
        } else {
            StreamServiceManager.startStream(selectedStreamService);
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

            setText(item.getLabel());
            setGraphic(new ImageView(item.getIcon()));
        }
    }

}
