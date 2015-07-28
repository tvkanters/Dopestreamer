package com.dopelives.dopestreamer.gui.controllers;

import java.net.URL;
import java.security.InvalidParameterException;
import java.util.List;
import java.util.ResourceBundle;

import javafx.application.Platform;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.text.Text;

import com.dopelives.dopestreamer.Environment;
import com.dopelives.dopestreamer.gui.Screen;
import com.dopelives.dopestreamer.gui.StreamState;
import com.dopelives.dopestreamer.gui.combobox.ComboBoxCell;
import com.dopelives.dopestreamer.streams.Quality;
import com.dopelives.dopestreamer.streams.Stream;
import com.dopelives.dopestreamer.streams.StreamInfo;
import com.dopelives.dopestreamer.streams.StreamInfo.StreamInfoListener;
import com.dopelives.dopestreamer.streams.StreamListener;
import com.dopelives.dopestreamer.streams.StreamManager;
import com.dopelives.dopestreamer.streams.services.StreamService;
import com.dopelives.dopestreamer.streams.services.StreamServiceManager;
import com.dopelives.dopestreamer.util.Pref;

/**
 * The controller for the streams screen.
 */
public class Streams implements Initializable, StreamListener, StreamInfoListener, Controller {

    @FXML
    private Node root;
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
    private Text streamTypeInfo;
    @FXML
    private Label gameInfo;
    @FXML
    private CheckBox gameModeToggle;
    @FXML
    private CheckBox autoswitchToggle;
    @FXML
    private Text viewerInfo;

    /** Whether or not autoswitch is currently active */
    private boolean mAutoswitchEnabled;

    @Override
    public synchronized void initialize(final URL location, final ResourceBundle resources) {
        // Select the custom channel radio button upon focusing the text field next to it
        channelCustomInput.focusedProperty()
                .addListener(
                        (final ObservableValue<? extends Boolean> observable, final Boolean oldValue,
                                final Boolean newValue) -> {
                            if (newValue) {
                                channelCustom.setSelected(true);
                            }
                        });

        // Start stream when pressing ENTER in the channel input box
        channelCustomInput.setOnKeyPressed((final KeyEvent key) -> {
            if (key.getCode().equals(KeyCode.ENTER)) {
                final StreamManager streamManager = StreamManager.getInstance();
                switch (streamManager.getStreamState()) {
                    case INACTIVE:
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
                startStream();
            }
        });
        // Dank it up
        channelCustomInput.setOnKeyReleased((final KeyEvent key) -> {
            if (channelCustomInput.getText().equals("420")) {
                for (final Screen screen : Screen.values()) {
                    screen.getNode().getStylesheets().add(Environment.STYLE_FOLDER + "420.css");
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

        // Indicate unavailable streams and update quality options
        streamServiceSelection.setOnAction((final ActionEvent event) -> {
            final StreamService streamService = streamServiceSelection.getValue();
            final boolean disableDefault = !streamService.hasDefaultChannel();
            final boolean disableCustom = !streamService.allowsCustomChannels();

            channelDefault.setDisable(disableDefault);
            channelCustom.setDisable(disableCustom);
            channelCustomInput.setDisable(disableCustom);

            if (disableDefault) {
                channelCustom.setSelected(true);
            } else if (disableCustom) {
                channelDefault.setSelected(true);
            }

            updateQualityOptions();

            autoswitchToggle.setDisable(!StreamServiceManager.getAutoswitchServices().contains(streamService));
        });

        // Select the stored last stream service
        final StreamService selectedService = StreamServiceManager.getStreamServiceByKey(Pref.LAST_STREAM_SERVICE
                .getString());
        if (streamServices.contains(selectedService)) {
            streamServiceSelection.getSelectionModel().select(selectedService);
        }
        // Make sure a value is selected
        if (streamServiceSelection.getValue() == null) {
            final StreamService defaultService = StreamServiceManager.getStreamServiceByKey(Pref.LAST_STREAM_SERVICE
                    .getDefaultString());
            streamServiceSelection.getSelectionModel().select(defaultService);
        }

        // Make the stream services look nice within the combo box
        streamServiceSelection.setButtonCell(new ComboBoxCell<StreamService>());
        streamServiceSelection
                .setCellFactory((final ListView<StreamService> param) -> new ComboBoxCell<StreamService>());

        // Prepare the quality combo box
        updateQualityOptions();
        qualitySelection.setButtonCell(new ComboBoxCell<Quality>());
        qualitySelection.setCellFactory((final ListView<Quality> param) -> new ComboBoxCell<Quality>());

        // Set checkbox preferences
        gameModeToggle.setSelected(Pref.GAME_MODE.getBoolean());
        autoswitchToggle.setSelected(Pref.AUTOSWITCH.getBoolean());
        autoswitchToggle.setDisable(!StreamServiceManager.getAutoswitchServices().contains(selectedService));

        // Update to the right stream state
        final StreamManager streamManager = StreamManager.getInstance();
        streamManager.addListener(this);
        onStateUpdated(streamManager, null, streamManager.getStreamState());

        // Update the stream info
        StreamInfo.addListener(this);
        StreamInfo.startRequestInterval();
    }

    @Override
    public void onActived() {}

    @FXML
    protected synchronized void onStreamButtonClicked(final ActionEvent event) {
        final StreamManager streamManager = StreamManager.getInstance();

        switch (streamManager.getStreamState()) {
            case INACTIVE:
                startStream();
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
     * Uses the GUI input to start a stream.
     */
    private void startStream() {
        mAutoswitchEnabled = false;
        final StreamManager streamManager = StreamManager.getInstance();
        final StreamService selectedStreamService = streamServiceSelection.getValue();
        final Quality quality = qualitySelection.getValue();

        // Pick a default or custom channel
        if (channelDefault.isSelected() && selectedStreamService.hasDefaultChannel()) {
            if (Pref.AUTOSWITCH.getBoolean()
                    && StreamServiceManager.getAutoswitchServices().contains(selectedStreamService)) {
                mAutoswitchEnabled = true;
                streamManager.resetAutoswitch();
                streamManager.startAutoswitch(quality);
            } else {
                streamManager.startStream(selectedStreamService, quality);
            }
        } else {
            try {
                streamManager.startStream(selectedStreamService, channelCustomInput.getText(), quality);
            } catch (final InvalidParameterException ex) {
                setCustomChannelValid(false);
            }
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
                setQualityValid(true);
                StreamInfo.requestRefresh();
                break;

            case BUFFERING:
                if (mAutoswitchEnabled) {
                    Platform.runLater(() -> {
                        streamServiceSelection.setValue(streamManager.getCurrentStreamService());
                    });
                }
                break;

            default:
                break;
        }

        // Run in UI thread
        Platform.runLater(() -> {
            streamButton.setText(newState.getLabel());
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
    public void onInvalidQuality(final Stream stream) {
        setQualityValid(false);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onInvalidMediaPlayer(final Stream stream) {
        Platform.runLater(() -> {
            streamButton.setText("Invalid media player");
        });
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onInvalidLivestreamer(final Stream stream) {
        Platform.runLater(() -> {
            streamButton.setText("Livestreamer outdated");
        });
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onLivestreamerNotFound(final Stream stream) {
        Platform.runLater(() -> {
            streamButton.setText("Livestreamer not found");
        });
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onRtmpDumpNotFound(final Stream stream) {
        Platform.runLater(() -> {
            streamButton.setText("RTMPDump not found");
        });
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onStreamInfoUpdated(final String streamer, final String type, final String game) {
        Platform.runLater(() -> {
            streamerInfo.setText(streamer);
            streamTypeInfo.setText(type);
            gameInfo.setText(game);

            topicActive.setVisible(true);
            topicActive.setManaged(true);
            topicInactive.setVisible(false);
            topicInactive.setManaged(false);
        });
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onStreamInfoRemoved() {
        Platform.runLater(() -> {
            topicActive.setVisible(false);
            topicActive.setManaged(false);
            topicInactive.setVisible(true);
            topicInactive.setManaged(true);
        });
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onViewerCountUpdated(final int viewerCount) {
        Platform.runLater(() -> {
            viewerInfo.setText(Integer.toString(viewerCount));
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

    /**
     * Defines whether or not the quality is valid or not. Will update the GUI appropriately.
     *
     * @param valid
     *            True iff the input quality is valid
     */
    private synchronized void setQualityValid(final boolean valid) {
        ControllerHelper.setCssClass(qualitySelection, "invalid", !valid);
    }

    @FXML
    private void onGameModeToggle() {
        Pref.GAME_MODE.put(gameModeToggle.isSelected());
    }

    /**
     * Updates the shown quality options based on the selected stream service.
     */
    private void updateQualityOptions() {
        // Check what the old selection is
        final Quality selectedQuality;
        if (qualitySelection.getItems().size() > 0) {
            selectedQuality = qualitySelection.getValue();
        } else {
            selectedQuality = Quality.valueOf(Pref.LAST_QUALITY.getString());
        }

        // Add qualities to the combo box
        final List<Quality> qualities = streamServiceSelection.getValue().getQualities();
        qualitySelection.getItems().setAll(qualities);

        // Select the last chosen quality
        if (qualities.contains(selectedQuality)) {
            qualitySelection.getSelectionModel().select(selectedQuality);
        } else {
            // Select the first option if the selected one isn't available for this service
            qualitySelection.getSelectionModel().select(0);
        }
    }

    @FXML
    private void onAutoswitchToggle() {
        Pref.AUTOSWITCH.put(autoswitchToggle.isSelected());
    }

    @FXML
    private void clearFocus() {
        root.requestFocus();
    }

}
