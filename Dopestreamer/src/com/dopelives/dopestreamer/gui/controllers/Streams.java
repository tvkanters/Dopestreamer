package com.dopelives.dopestreamer.gui.controllers;

import java.net.URL;
import java.security.InvalidParameterException;
import java.util.List;
import java.util.ResourceBundle;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.util.Callback;

import com.dopelives.dopestreamer.Pref;
import com.dopelives.dopestreamer.gui.StreamState;
import com.dopelives.dopestreamer.gui.combobox.QualityCell;
import com.dopelives.dopestreamer.gui.combobox.StreamServiceCell;
import com.dopelives.dopestreamer.shell.ConsoleListener;
import com.dopelives.dopestreamer.shell.ProcessId;
import com.dopelives.dopestreamer.streams.Quality;
import com.dopelives.dopestreamer.streams.Stream;
import com.dopelives.dopestreamer.streams.StreamService;
import com.dopelives.dopestreamer.streams.StreamServiceManager;

/**
 * The controller for the streams screen.
 */
public class Streams implements Initializable, ConsoleListener {

    @FXML
    private RadioButton channelDefault;
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

    /** The current state of the main stream */
    private StreamState mStreamState;
    /** The currently active stream */
    private Stream mStream;

    @Override
    public synchronized void initialize(final URL location, final ResourceBundle resources) {
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

        // Set the last used channel if provided
        final String lastChannel = Pref.LAST_CHANNEL.getString();
        if (!lastChannel.equals("")) {
            channelCustom.setSelected(true);
            channelCustomInput.setText(lastChannel);
        }

        // Add stream services to the combo box
        final List<StreamService> streamServices = StreamServiceManager.getStreamServices();
        streamServiceSelection.getItems().addAll(streamServices);

        // Indicate unavailable streams
        streamServiceSelection.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(final ActionEvent event) {
                setCssClass(channelDefault, "unavailable", !streamServiceSelection.getValue().hasDefaultChannel());
            }
        });

        // Select the stored last stream service
        final String selectedStreamServiceKey = Pref.LAST_STREAM_SERVICE.getString();
        for (int i = 0; i < streamServices.size(); ++i) {
            if (streamServices.get(i).getKey().equals(selectedStreamServiceKey)) {
                streamServiceSelection.getSelectionModel().select(i);
                break;
            }
        }

        // Make the stream services look nice within the combo box
        streamServiceSelection.setButtonCell(new StreamServiceCell());
        streamServiceSelection.setCellFactory(new Callback<ListView<StreamService>, ListCell<StreamService>>() {
            @Override
            public ListCell<StreamService> call(final ListView<StreamService> param) {
                return new StreamServiceCell();
            }
        });

        // Add qualities to the combo box
        final Quality[] qualities = Quality.values();
        qualitySelection.getItems().addAll(qualities);

        // Select the stored last quality
        final String selectedQualityKey = Pref.LAST_QUALITY.getString();
        for (int i = 0; i < qualities.length; ++i) {
            if (qualities[i].toString().equals(selectedQualityKey)) {
                qualitySelection.getSelectionModel().select(i);
                break;
            }
        }

        // Make the qualities look nice within the combo box
        qualitySelection.setButtonCell(new QualityCell());
        qualitySelection.setCellFactory(new Callback<ListView<Quality>, ListCell<Quality>>() {
            @Override
            public ListCell<Quality> call(final ListView<Quality> param) {
                return new QualityCell();
            }
        });

        // Check the auto-start preference
        if (Pref.AUTO_START.getBoolean()) {
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

            case CONNECTING:
            case WAITING:
            case BUFFERING:
            case ACTIVE:
                stopStream();
                break;

            default:
                throw new IllegalStateException("Unknown state: " + mStreamState);
        }
    }

    @Override
    public synchronized void onConsoleOutput(final ProcessId processId, final String output) {
        if (output.contains("Opening stream")) {
            updateState(StreamState.BUFFERING);

        } else if (output.contains("Waiting for streams")) {
            updateState(StreamState.WAITING);

        } else if (output.contains("Unable to open URL")) {
            if (channelCustom.isSelected()) {
                setCustomChannelValid(false);
            }
            stopStream();

        } else if (output.contains("Writing stream to output")) {
            updateState(StreamState.ACTIVE);
        }
    }

    @Override
    public synchronized void onConsoleStop(final ProcessId processId) {
        switch (mStreamState) {
            case INACTIVE:
                break;

            case CONNECTING:
            case WAITING:
            case BUFFERING:
            case ACTIVE:
                // The user didn't cancel streaming, so try starting the stream again
                startStream();
                break;

            default:
                throw new IllegalStateException("Unknown state: " + mStreamState);
        }
    }

    /**
     * Starts the stream based on the user preferences and transitions to the connecting state.
     */
    private synchronized void startStream() {
        final StreamService selectedStreamService = streamServiceSelection.getValue();
        final String channel;
        final Quality quality = qualitySelection.getValue();

        // Clean up if needed
        if (mStream != null) {
            mStream.stop();
            mStream = null;
        }

        // Pick a default or custom channel
        if (channelDefault.isSelected() && selectedStreamService.hasDefaultChannel()) {
            channel = "";
            mStream = new Stream(selectedStreamService, quality);
        } else {
            channel = channelCustomInput.getText();
            try {
                mStream = new Stream(selectedStreamService, channel, quality);
            } catch (final InvalidParameterException ex) {
                setCustomChannelValid(false);
            }
        }

        if (mStream != null) {
            // Start the stream
            setCustomChannelValid(true);
            updateState(StreamState.CONNECTING);

            mStream.addListener(this);
            mStream.start();

            // Save the stream settings
            Pref.LAST_CHANNEL.put(channel);
            Pref.LAST_STREAM_SERVICE.put(selectedStreamService.getKey());
            Pref.LAST_QUALITY.put(quality.toString());
        }
    }

    /**
     * Stops the active stream and transitions to the inactive state.
     */
    private synchronized void stopStream() {
        updateState(StreamState.INACTIVE);
        mStream.stop();
        mStream = null;
    }

    /**
     * Transitions to the given state and updates GUI components.
     *
     * @param newState
     *            The stream state to transition to
     */
    public synchronized void updateState(final StreamState newState) {
        final String oldCssClass = (mStreamState != null ? mStreamState.getCssClass() : null);

        mStreamState = newState;

        // Run in UI thread
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                streamButton.setText(mStreamState.getLabel());

                final String newCssClass = newState.getCssClass();
                if (!newCssClass.equals(oldCssClass)) {
                    setCssClass(streamButton, oldCssClass, false);
                    setCssClass(streamButton, newCssClass, true);
                }
            }
        });
    }

    /**
     * Defines whether or not the inserted custom channel is valid or not. Will update the GUI appropriately.
     *
     * @param valid
     *            True iff the input value is valid
     */
    private synchronized void setCustomChannelValid(final boolean valid) {
        // Run in UI thread
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                setCssClass(channelCustomInput, "invalid", !valid);
            }
        });
    }

    /**
     * Adds or removes a CSS class on an element.
     *
     * @param element
     *            The element to change the CSS class of
     * @param cssClass
     *            The CSS class to add or remove
     * @param enabled
     *            True if the CSS class should be added, false for removal
     */
    private void setCssClass(final Node element, final String cssClass, final boolean enabled) {
        final ObservableList<String> cssClasses = element.getStyleClass();
        if (enabled) {
            if (!cssClasses.contains(cssClass)) {
                cssClasses.add(cssClass);
            }
        } else {
            cssClasses.remove(cssClass);
        }
    }

}
