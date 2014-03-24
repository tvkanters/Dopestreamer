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
import javafx.util.Callback;

import com.dopelives.dopestreamer.combobox.QualityCell;
import com.dopelives.dopestreamer.combobox.StreamServiceCell;
import com.dopelives.dopestreamer.streamservices.Quality;
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
    private ComboBox<Quality> qualitySelection;

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

        // Add qualities to the combo box and select the first
        qualitySelection.getItems().addAll(Quality.values());
        qualitySelection.getSelectionModel().select(0);

        // Make the qualities look nice within the combo box
        qualitySelection.setButtonCell(new QualityCell());
        qualitySelection.setCellFactory(new Callback<ListView<Quality>, ListCell<Quality>>() {
            @Override
            public ListCell<Quality> call(final ListView<Quality> param) {
                return new QualityCell();
            }
        });
    }

    @FXML
    protected void onLiveClicked(final ActionEvent event) {
        // Start stream
        final StreamService selectedStreamService = streamServiceSelection.getValue();
        final Quality quality = qualitySelection.getValue();

        if (channelCustom.isSelected()) {
            try {
                StreamServiceManager.startStream(selectedStreamService, channelCustomInput.getText(), quality);
            } catch (final InvalidParameterException ex) {
                // TODO: Tell user that a channel must be provided
            }
        } else {
            StreamServiceManager.startStream(selectedStreamService, quality);
        }
    }

}
