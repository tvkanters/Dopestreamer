package com.dopelives.dopestreamer.gui.controllers;

import java.net.URL;
import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

import com.dopelives.dopestreamer.Environment;
import com.dopelives.dopestreamer.gui.Screen;
import com.dopelives.dopestreamer.gui.StreamState;
import com.dopelives.dopestreamer.gui.combobox.ComboBoxCell;
import com.dopelives.dopestreamer.streams.FavouriteStream;
import com.dopelives.dopestreamer.streams.Quality;
import com.dopelives.dopestreamer.streams.Stream;
import com.dopelives.dopestreamer.streams.StreamInfo;
import com.dopelives.dopestreamer.streams.StreamInfo.StreamInfoListener;
import com.dopelives.dopestreamer.streams.StreamListener;
import com.dopelives.dopestreamer.streams.StreamManager;
import com.dopelives.dopestreamer.streams.services.StreamService;
import com.dopelives.dopestreamer.streams.services.StreamServiceManager;
import com.dopelives.dopestreamer.util.Pref;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.text.Text;
import javafx.util.StringConverter;

/**
 * The controller for the streams screen.
 */
public class Streams implements Initializable, StreamListener, StreamInfoListener, Controller {

    @FXML
    private Node root;
    @FXML
    private RadioButton channelFavourite;
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
    private Text viewerInfo;
    @FXML
    private CheckBox favouriteStreamToggle;
    @FXML
    private Button favouriteStreamEdit;
    @FXML
    private Button favouriteStreamDelete;
    @FXML
    private ComboBox<FavouriteStream> favouriteStreamSelection;

    /** The stream service that was last selected */
    private StreamService mLastSelected = null;
    /** Whether or not the last selected stream service can be updated */
    private boolean mLockStreamServiceSelection = false;
    /** Used to ignore changes to the current favourite stream for mid-updating purposes */
    private boolean mIgnoreFavouriteStreamActions = false;

    /** The change listener used to store info and revert the UI after editing a favourite stream */
    private final ChangeListener<Boolean> mFavouriteStreamBlurAction = (observable, oldValue, newValue) -> {
        if (!newValue) {
            onFavouriteStreamEditStop();
        }
    };
    /** The showing listener used to detect when the combo box is opened while editing a favourite stream */
    private final ChangeListener<Boolean> mFavouriteStreamOpenAction = (observable, oldValue, newValue) -> {
        onFavouriteStreamEditStop();
    };

    @Override
    public synchronized void initialize(final URL location, final ResourceBundle resources) {

        /* FAVOURITE STREAMS UI */

        // Set up a converter for the editable combo box
        favouriteStreamSelection.setConverter(new StringConverter<FavouriteStream>() {
            @Override
            public String toString(final FavouriteStream object) {
                return (object != null ? object.getLabel() : null);
            }

            @Override
            public FavouriteStream fromString(final String string) {
                final FavouriteStream selectedFavouriteStream = favouriteStreamSelection.getValue();
                return new FavouriteStream(string, selectedFavouriteStream.getStreamService(),
                        selectedFavouriteStream.getChannel());
            }
        });

        // Make the favourite streams look nice within the combo box
        favouriteStreamSelection.setButtonCell(new ComboBoxCell<>());
        favouriteStreamSelection.setCellFactory(param -> new ComboBoxCell<>());
        updateFavouriteStreams();

        // Update other UI elements upon selecting a different favourite stream
        favouriteStreamSelection.setOnAction(event -> {
            if (!mIgnoreFavouriteStreamActions) {
                channelFavourite.setSelected(true);
                onSelectFavouriteStream();
            }
        });

        // Catch ENTER events on the favourite stream's input field to finish editing (and prevent exceptions)
        favouriteStreamSelection.addEventFilter(KeyEvent.KEY_PRESSED, event -> {
            if (event.getCode() == KeyCode.ENTER) {
                onFavouriteStreamEditStop();
                event.consume();
            }
        });

        // Update UI when a different channel mode is selected
        channelFavourite.setOnAction(event -> {
            onSelectFavouriteStream();
        });
        channelCustom.setOnAction(event -> {
            updateStreamServices();
        });

        /* STREAM SERVICE UI */

        // Add stream services to the combo box
        final List<StreamService> streamServices = StreamServiceManager.getEnabledStreamServices();
        streamServiceSelection.getItems().addAll(streamServices);

        // Indicate unavailable streams and update quality options
        streamServiceSelection.setOnAction(event -> {
            mLockStreamServiceSelection = false;

            final StreamService streamService = streamServiceSelection.getValue();
            final StreamService favouriteStreamService = favouriteStreamSelection.getValue().getStreamService();
            if (!favouriteStreamService.equals(streamService)
                    && (favouriteStreamService.isEnabled() || !streamService.equals(StreamServiceManager.DISABLED))) {
                channelCustom.setSelected(true);
                channelCustomInput.requestFocus();
            }

            updateQualityOptions();
            updateFavouriteStreamToggle();
        });

        // Select the stored last stream service
        final StreamService selectedService = StreamServiceManager
                .getStreamServiceByKey(Pref.LAST_STREAM_SERVICE.getString());
        if (streamServices.contains(selectedService)) {
            streamServiceSelection.setValue(selectedService);
        }

        // Make sure a value is selected
        if (streamServiceSelection.getValue() == null) {
            final StreamService defaultService = StreamServiceManager
                    .getStreamServiceByKey(Pref.LAST_STREAM_SERVICE.getDefaultString());
            if (streamServices.contains(defaultService)) {
                streamServiceSelection.setValue(defaultService);
            } else {
                streamServiceSelection.getSelectionModel().select(0);
            }
        }
        streamServiceSelection.getOnAction().handle(null);

        // Make the stream services look nice within the combo box
        streamServiceSelection.setButtonCell(new ComboBoxCell<>());
        streamServiceSelection.setCellFactory(param -> new ComboBoxCell<>());

        /* CUSTOM CHANNEL UI */

        // Select the custom channel radio button upon focusing the text field next to it
        channelCustomInput.focusedProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue) {
                channelCustom.setSelected(true);
                channelCustom.getOnAction().handle(null);
            }
        });

        // Start stream when pressing ENTER in the channel input box
        channelCustomInput.setOnKeyPressed(event -> {
            if (event.getCode().equals(KeyCode.ENTER)) {
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
        // Update favourite checkbox and add dank Easter egg
        channelCustomInput.textProperty().addListener((observable, oldValue, newValue) -> {
            updateFavouriteStreamToggle();
            if (newValue.equals("420")) {
                for (final Screen screen : Screen.values()) {
                    screen.getNode().getStylesheets().add(Environment.STYLE_FOLDER + "420.css");
                }
            }
        });

        // Set the last used stream if provided
        final String lastChannel = Pref.LAST_CHANNEL.getString();
        final Optional<FavouriteStream> lastFavouriteStream = findFavouriteStream(streamServiceSelection.getValue(),
                lastChannel);
        if (lastFavouriteStream.isPresent() || lastChannel.equals("")) {
            channelFavourite.setSelected(true);
            if (lastFavouriteStream.isPresent()) {
                favouriteStreamSelection.setValue(lastFavouriteStream.get());
            }
        } else {
            channelCustom.setSelected(true);
            channelCustomInput.setText(lastChannel);
        }

        /* QUALITY UI */

        // Prepare the quality combo box
        updateQualityOptions();
        qualitySelection.setButtonCell(new ComboBoxCell<>());
        qualitySelection.setCellFactory(param -> new ComboBoxCell<>());

        /* GAME MODE UI */

        // Set checkbox preferences
        gameModeToggle.setSelected(Pref.GAME_MODE.getBoolean());

        /* UPDATE UI BASED ON STREAM STATE */

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
    private void clearFocus() {
        root.requestFocus();
    }

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
        final StreamManager streamManager = StreamManager.getInstance();
        final Quality quality = qualitySelection.getValue();

        // Pick a default or custom channel
        if (channelFavourite.isSelected()) {
            final FavouriteStream favouriteStream = favouriteStreamSelection.getValue();
            try {
                streamManager.startStream(favouriteStream.getStreamService(), favouriteStream.getChannel(), quality);
            } catch (final InvalidParameterException ex) {
                setFavouriteStreamValid(false);
            }
        } else {
            try {
                streamManager.startStream(streamServiceSelection.getValue(), channelCustomInput.getText(), quality);
            } catch (final InvalidParameterException ex) {
                setCustomChannelValid(false);
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onStateUpdated(final StreamManager streamManager, final StreamState oldState,
            final StreamState newState) {
        switch (newState) {
            case CONNECTING:
                // When the stream starts connecting, remove errors and update the stream info
                setCustomChannelValid(true);
                setFavouriteStreamValid(true);
                setQualityValid(true);
                StreamInfo.requestRefresh();
                break;

            default:
                break;
        }

        // Update the stream button
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
        } else {
            setFavouriteStreamValid(false);
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
     * Defines whether or not the selected favourite stream is valid or not. Will update the GUI appropriately.
     *
     * @param valid
     *            True iff the input value is valid
     */
    private synchronized void setFavouriteStreamValid(final boolean valid) {
        ControllerHelper.setCssClass(favouriteStreamSelection, "invalid", !valid);
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

    @FXML
    private void onFavouriteStreamToggle() {
        if (favouriteStreamToggle.isSelected()) {
            // The current entered stream is favourited, so store it and update the list
            final String channel = channelCustomInput.getText();
            if (channel.equals("")) {
                favouriteStreamToggle.setSelected(false);
            } else {
                final FavouriteStream favouriteStream = new FavouriteStream(channel, streamServiceSelection.getValue(),
                        channel);
                Pref.FAVOURITE_STREAMS.add(favouriteStream.toJson());
                updateFavouriteStreams();
                favouriteStreamSelection.setValue(favouriteStream);
            }

        } else {
            // The currently selected favourite stream has been unfavourited, so remove it
            final Optional<FavouriteStream> targetFavouriteStream = findFavouriteStream(
                    streamServiceSelection.getValue(), channelCustomInput.getText());
            if (targetFavouriteStream.isPresent()) {
                deleteFavouriteStream(targetFavouriteStream.get());
            }
        }
    }

    @FXML
    private void onFavouriteStreamEditStart() {
        final FavouriteStream favouriteStream = favouriteStreamSelection.getValue();

        favouriteStreamSelection.setEditable(true);
        favouriteStreamSelection.setValue(favouriteStream);
        favouriteStreamSelection.requestFocus();
        favouriteStreamSelection.focusedProperty().addListener(mFavouriteStreamBlurAction);
        favouriteStreamSelection.showingProperty().addListener(mFavouriteStreamOpenAction);

        favouriteStreamEdit.setVisible(false);
        favouriteStreamEdit.setManaged(false);
        favouriteStreamDelete.setVisible(true);
        favouriteStreamDelete.setManaged(true);
    }

    /**
     * Called when the favourite stream editor is blurred and the edits should be stored.
     */
    private void onFavouriteStreamEditStop() {
        favouriteStreamSelection.focusedProperty().removeListener(mFavouriteStreamBlurAction);
        favouriteStreamSelection.showingProperty().removeListener(mFavouriteStreamOpenAction);
        favouriteStreamEdit.setVisible(true);
        favouriteStreamEdit.setManaged(true);
        favouriteStreamDelete.setVisible(false);
        favouriteStreamDelete.setManaged(false);

        if (favouriteStreamDelete.isFocused()) {
            // If the delete button got focus, delete the favourite stream that was being edited
            final FavouriteStream targetFavouriteStream = favouriteStreamSelection.getValue();
            favouriteStreamSelection.setEditable(false);
            deleteFavouriteStream(targetFavouriteStream);

        } else {
            // If anything other than the delete button got focus, store the edits made

            // Get the current and updated favourite stream
            final FavouriteStream favouriteStream = favouriteStreamSelection.getValue();
            final FavouriteStream updatedFavouriteStream = new FavouriteStream(
                    favouriteStreamSelection.getEditor().getText(), favouriteStream.getStreamService(),
                    favouriteStream.getChannel());

            // Store the edit
            final List<String> favouriteStreams = new ArrayList<>(Pref.FAVOURITE_STREAMS.getList());
            favouriteStreams.set(favouriteStreams.indexOf(favouriteStream.toJson()), updatedFavouriteStream.toJson());
            Pref.FAVOURITE_STREAMS.put(favouriteStreams);

            // Update the UI
            mIgnoreFavouriteStreamActions = true;
            favouriteStreamSelection.setEditable(false);
            updateFavouriteStreams();
            favouriteStreamSelection.setValue(updatedFavouriteStream);
            mIgnoreFavouriteStreamActions = false;
        }
    }

    /**
     * Called when a favourite stream has been selected. Updates the rest of the UI appropriately.
     */
    private void onSelectFavouriteStream() {
        final FavouriteStream favouriteStream = favouriteStreamSelection.getValue();
        if (favouriteStream != null) {
            updateStreamServices();
            Platform.runLater(() -> {
                if (streamServiceSelection.getItems().contains(favouriteStream.getStreamService())) {
                    streamServiceSelection.setValue(favouriteStream.getStreamService());
                } else {
                    if (streamServiceSelection.getItems().contains(StreamServiceManager.DISABLED)) {
                        streamServiceSelection.setValue(StreamServiceManager.DISABLED);
                    }
                }
            });
        }
    }

    /**
     * Enables or disables selection favourite streams.
     *
     * @param enabled
     *            True iff the user should be able to select favourite streams
     */
    public void setFavouriteSelectionEnabled(final boolean enabled) {
        if (!enabled) {
            channelCustom.setSelected(true);
        }

        channelFavourite.setDisable(!enabled);
        favouriteStreamSelection.setDisable(!enabled);
        favouriteStreamEdit.setDisable(!enabled);
    }

    /**
     * Deleted a favourite stream from persistent storage and the UI.
     *
     * @param favouriteStream
     *            The favourite stream to delete
     */
    private void deleteFavouriteStream(final FavouriteStream favouriteStream) {
        // Remove favourite stream from storage
        Pref.FAVOURITE_STREAMS.remove(favouriteStream.toJson());

        // Update the UI
        updateFavouriteStreams();
        channelCustom.setSelected(true);
    }

    /**
     * Updates the last of stream services. To be called after stream services are enabled or disabled.
     */
    public void updateStreamServices() {
        Platform.runLater(() -> {
            final List<StreamService> streamServices = new ArrayList<>(StreamServiceManager.getEnabledStreamServices());

            // Add a disabled stream service if it's selected as favourite stream
            if (channelFavourite.isSelected() && !favouriteStreamSelection.getValue().getStreamService().isEnabled()) {
                streamServices.add(StreamServiceManager.DISABLED);
            }

            // Keep track of which stream service the user selected manually
            final StreamService selected = streamServiceSelection.getValue();
            if (selected != null && !mLockStreamServiceSelection) {
                mLastSelected = selected;
            }

            // Update the stream services
            streamServiceSelection.getItems().setAll(streamServices);

            // Select the right stream service
            if (streamServiceSelection.getItems().contains(mLastSelected) && mLockStreamServiceSelection) {
                streamServiceSelection.setValue(mLastSelected);
                mLockStreamServiceSelection = false;

            } else if (streamServiceSelection.getItems().contains(selected)) {
                streamServiceSelection.setValue(selected);
                mLockStreamServiceSelection = true;

            } else if (streamServices.size() > 0) {
                streamServiceSelection.getSelectionModel().select(0);
                mLockStreamServiceSelection = true;
            }
        });
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
            qualitySelection.setValue(selectedQuality);
        } else {
            // Select the first option if the selected one isn't available for this service
            qualitySelection.getSelectionModel().select(0);
        }
    }

    /**
     * Updates the favourites combo box. Should be called after modifying the stored favourite streams.
     */
    public void updateFavouriteStreams() {
        final List<FavouriteStream> favouriteStreams = new LinkedList<FavouriteStream>();

        // Collect stored favourite streams
        final List<String> favouriteStreamJsons = Pref.FAVOURITE_STREAMS.getList();
        for (final String favouriteStreamJson : favouriteStreamJsons) {
            favouriteStreams.add(new FavouriteStream(favouriteStreamJson));
        }

        // Check if there are any favourite streams and if not, handle it appropriately
        if (favouriteStreams.isEmpty()) {
            favouriteStreams.add(new FavouriteStream("No stream favourited", StreamServiceManager.NONE, ""));
            setFavouriteSelectionEnabled(false);
        } else {
            setFavouriteSelectionEnabled(true);
        }

        // Add favourite streams
        favouriteStreamSelection.getItems().setAll(favouriteStreams);

        // Make sure a favourite stream is selected
        if (favouriteStreamSelection.getValue() == null
                || !favouriteStreams.contains(favouriteStreamSelection.getValue())) {
            favouriteStreamSelection.setValue(favouriteStreams.get(0));
        }

        updateFavouriteStreamToggle();
    }

    /**
     * Updates the favourite stream toggle based on the entered info.
     */
    public void updateFavouriteStreamToggle() {
        final StreamService currentStreamService = streamServiceSelection.getValue();
        final String currentChannel = channelCustomInput.getText();

        if (StreamServiceManager.NONE.equals(currentStreamService) || currentChannel.equals("")) {
            favouriteStreamToggle.setDisable(true);
            favouriteStreamToggle.setSelected(false);
        } else {
            favouriteStreamToggle.setDisable(false);
            favouriteStreamToggle.setSelected(findFavouriteStream(currentStreamService, currentChannel).isPresent());
        }
    }

    /**
     * Tries to find a favourite stream with the given properties in the list of available favourite streams.
     *
     * @param streamService
     *            The stream service for the favourite stream
     * @param channel
     *            The case-insensitive channel for the favourite stream
     *
     * @return An optional matching favourite stream
     */
    public Optional<FavouriteStream> findFavouriteStream(final StreamService streamService, final String channel) {
        final String channelPruned = channel.toLowerCase();
        return favouriteStreamSelection.getItems().stream().filter(
                f -> f.getStreamService().equals(streamService) && f.getChannel().toLowerCase().equals(channelPruned))
                .findAny();
    }

}
