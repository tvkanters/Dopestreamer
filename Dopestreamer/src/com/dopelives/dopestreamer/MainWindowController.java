package com.dopelives.dopestreamer;

import java.net.URL;
import java.security.InvalidParameterException;
import java.util.ResourceBundle;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.util.Callback;

import com.dopelives.dopestreamer.combobox.QualityCell;
import com.dopelives.dopestreamer.combobox.StreamServiceCell;
import com.dopelives.dopestreamer.shell.ConsoleListener;
import com.dopelives.dopestreamer.shell.ProcessId;
import com.dopelives.dopestreamer.streams.Quality;
import com.dopelives.dopestreamer.streams.Stream;
import com.dopelives.dopestreamer.streams.StreamService;
import com.dopelives.dopestreamer.streams.StreamServiceManager;

public class MainWindowController implements Initializable, ConsoleListener {

    @FXML
    private RadioButton channelCustom;
    @FXML
    private TextField channelCustomInput;
    @FXML
    private ComboBox<StreamService> streamServiceSelection;
    @FXML
    private ComboBox<Quality> qualitySelection;
    @FXML
    private Button streamButton;
    @FXML
    private CheckBox autoStartToggle;

    /** The current state of the main stream */
    private StreamState mStreamState;
    /** The currently active stream */
    private Stream mStream;

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
        streamServiceSelection.getSelectionModel().select(3);

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

        // Set the stream button label

        // Check the auto-start preference
        final boolean autoStart = Pref.AUTO_START.getBoolean();
        autoStartToggle.setSelected(autoStart);
        if (autoStart) {
            startStream();
        } else {
            updateState(StreamState.INACTIVE);
        }
    }

    @FXML
    protected synchronized void onStreamButtonClicked(final ActionEvent event) {
        switch (mStreamState) {
            case INACTIVE:
                startStream();
                break;

            case LOADING:
            case ACTIVE:
                updateState(StreamState.INACTIVE);
                mStream.stop();
                mStream = null;
                break;

            default:
                throw new IllegalStateException("Unknown state: " + mStreamState);
        }
    }

    @Override
    public synchronized void onConsoleOutput(final ProcessId processId, final String output) {
        // Mark the stream as active once Livestreamer says it has started
        if (output.contains("Writing stream to output")) {
            // Run in UI thread
            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                    updateState(StreamState.ACTIVE);
                }
            });
        }
    }

    @Override
    public synchronized void onConsoleStop(final ProcessId processId) {
        switch (mStreamState) {
            case INACTIVE:
                break;

            case LOADING:
            case ACTIVE:
                // The user didn't cancel streaming, so try starting the stream again
                startStream();
                break;

            default:
                throw new IllegalStateException("Unknown state: " + mStreamState);
        }
    }

    /**
     * Starts the stream based on the user preferences and transitions to the loading state.
     */
    private void startStream() {
        final StreamService selectedStreamService = streamServiceSelection.getValue();
        final Quality quality = qualitySelection.getValue();

        if (channelCustom.isSelected()) {
            try {
                mStream = new Stream(selectedStreamService, channelCustomInput.getText(), quality);
            } catch (final InvalidParameterException ex) {
                // TODO: Tell user that a channel must be provided
            }
        } else {
            mStream = new Stream(selectedStreamService, quality);
        }

        if (mStream != null) {
            updateState(StreamState.LOADING);

            mStream.addListener(this);
            mStream.start();
        }
    }

    /**
     * Transitions to the given state and updates GUI components.
     *
     * @param newState
     *            The stream state to transition to
     */
    public void updateState(final StreamState newState) {
        mStreamState = newState;
        streamButton.setText(mStreamState.getLabel());
    }

    @FXML
    public void onAutoStartToggle() {
        Pref.AUTO_START.put(autoStartToggle.isSelected());
    }

}
