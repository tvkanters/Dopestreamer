package com.dopelives.dopestreamer.gui.controllers;

import java.net.URL;
import java.security.InvalidParameterException;
import java.util.List;
import java.util.ResourceBundle;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.util.Callback;

import com.dopelives.dopestreamer.Pref;
import com.dopelives.dopestreamer.gui.StreamState;
import com.dopelives.dopestreamer.gui.combobox.QualityCell;
import com.dopelives.dopestreamer.gui.combobox.StreamServiceCell;
import com.dopelives.dopestreamer.streams.Quality;
import com.dopelives.dopestreamer.streams.Stream;
import com.dopelives.dopestreamer.streams.StreamInfo;
import com.dopelives.dopestreamer.streams.StreamInfo.StreamInfoListener;
import com.dopelives.dopestreamer.streams.services.StreamService;
import com.dopelives.dopestreamer.streams.services.StreamServiceManager;
import com.dopelives.dopestreamer.streams.StreamListener;
import com.dopelives.dopestreamer.streams.StreamManager;

/**
 * The controller for the streams screen.
 */
public class Streams implements Initializable, StreamListener, StreamInfoListener {

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
    @FXML
    private Node topicActive;
    @FXML
    private Node topicInactive;
    @FXML
    private Label streamerInfo;
    @FXML
    private Label gameInfo;

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
                ControllerHelper.setCssClass(channelDefault, "unavailable", !streamServiceSelection.getValue()
                        .hasDefaultChannel());
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

        // Update to the right stream state
        final StreamManager streamManager = StreamManager.getInstance();
        streamManager.addListener(this);
        onStateUpdated(streamManager, null, streamManager.getStreamState());

        // Update the stream info
        StreamInfo.addListener(this);
        StreamInfo.startRequestInterval();
    }

    @FXML
    protected synchronized void onStreamButtonClicked(final ActionEvent event) {
        final StreamManager streamManager = StreamManager.getInstance();

        switch (streamManager.getStreamState()) {
            case INACTIVE:
                final StreamService selectedStreamService = streamServiceSelection.getValue();
                final Quality quality = qualitySelection.getValue();

                // Pick a default or custom channel
                if (channelDefault.isSelected() && selectedStreamService.hasDefaultChannel()) {
                    streamManager.startStream(selectedStreamService, quality);
                } else {
                    try {
                        streamManager.startStream(selectedStreamService, channelCustomInput.getText(), quality);
                    } catch (final InvalidParameterException ex) {
                        setCustomChannelValid(false);
                    }
                }
                break;

            case CONNECTING:
            case WAITING:
            case BUFFERING:
            case ACTIVE:
                StreamInfo.requestRefresh();
                streamManager.stopStream();
                break;

            default:
                throw new IllegalStateException("Unknown state: " + streamManager.getStreamState());
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onStateUpdated(final StreamManager streamManager, final StreamState oldState, final StreamState newState) {
        switch (newState) {
            case CONNECTING:
                setCustomChannelValid(true);
                StreamInfo.requestRefresh();
                break;

            default:
        }

        // Run in UI thread
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                streamButton.setText(newState.getLabel());
            }
        });

        if (oldState != null) {
            ControllerHelper.setCssClass(streamButton, oldState.getCssClass(), false);
        }
        ControllerHelper.setCssClass(streamButton, newState.getCssClass(), true);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onInvalidChannel(final Stream stream) {
        if (channelCustom.isSelected()) {
            setCustomChannelValid(false);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onInvalidMediaPlayer(final Stream stream) {
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                streamButton.setText("Invalid media player");
            }
        });
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onInvalidLivestreamer(final Stream stream) {
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                streamButton.setText("Livestreamer outdated");
            }
        });
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onStreamInfoUpdated(final String streamer, final String game) {
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                streamerInfo.setText(streamer);
                gameInfo.setText(game);

                topicActive.setVisible(true);
                topicActive.setManaged(true);
                topicInactive.setVisible(false);
                topicInactive.setManaged(false);
            }
        });
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onStreamInfoRemoved() {
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                topicActive.setVisible(false);
                topicActive.setManaged(false);
                topicInactive.setVisible(true);
                topicInactive.setManaged(true);
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
        ControllerHelper.setCssClass(channelCustomInput, "invalid", !valid);
    }

}
