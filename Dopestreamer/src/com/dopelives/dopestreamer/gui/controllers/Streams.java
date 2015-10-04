package com.dopelives.dopestreamer.gui.controllers;

import java.net.URL;
import java.security.InvalidParameterException;
import java.util.LinkedList;
import java.util.List;
import java.util.ResourceBundle;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

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
import com.dopelives.dopestreamer.streams.services.FavoriteStream;
import com.dopelives.dopestreamer.streams.services.StreamService;
import com.dopelives.dopestreamer.streams.services.StreamServiceManager;
import com.dopelives.dopestreamer.util.Pref;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
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
    @FXML
    private ComboBox<FavoriteStream> favoriteChannelList;
    @FXML
    private CheckBox favoriteChannel;

    /** Whether or not autoswitch is currently active */
    private boolean mAutoswitchEnabled;
    /** The stream service that was last selected */
    private StreamService mLastSelected = null;
    /** Whether or not the last selected stream service can be updated */
    private boolean mLockStreamServiceSelection = false;

    boolean foundFav = false;
    String foundFavName = "";
    StreamService favChanTemp = null;

    @Override
    public synchronized void initialize(final URL location, final ResourceBundle resources) {

        favoriteChannelList.setEditable(true);
        updateFavoriteList();

        channelDefault.setOnAction((final ActionEvent event) -> {
            checkFavorites();
        });
        channelCustom.setOnAction((final ActionEvent event) -> {
            checkFavorites();
        });

        favoriteChannel.setOnAction((final ActionEvent event) -> {
            // On checking/unchecking the Favorite checkbox we get all the data we need
            JSONObject favorite = new JSONObject();
            String favName = favoriteChannelList.getEditor().textProperty().getValue();
            if (favName.equals("")) favName = channelCustomInput.getText().toUpperCase();
            favorite.put("label", favName);
            favorite.put("streamServiceKey", streamServiceSelection.getValue().getKey());
            favorite.put("channelName", channelCustomInput.getText());

            // Get the stored favorites
            JSONArray jsons = new JSONArray();
            jsons = new JSONArray(Pref.FAVORITED_STREAMS.getString());

            boolean contains = false;
            int containIndex = 0;
            // And loop through them to see if we can find a match
            for (int i = 0; i < jsons.length(); i++) {
                JSONObject jason;
                try {
                    jason = jsons.getJSONObject(i);
                } catch (final JSONException ex) {
                    continue; // ¯\_(ツ)_/¯
                }

                // No, .equals won't work between JSON objects because I dunno
                // Whatever, you don't need to favorite the same channel
                // under different labels anyway
                String chan = jason.getString("channelName");
                String ky = jason.getString("streamServiceKey");
                String fachan = favorite.getString("channelName");
                String faky = favorite.getString("streamServiceKey");
                if (chan.equals(fachan) && (ky.equals(faky) || faky.equals("none"))) {
                    // We recognize "none" as a key here because it lets removed streamservices
                    // be removed from favorites, so you're not stuck with dead channels.
                    contains = true;
                    containIndex = i;
                    break;
                }
            }
            if (favoriteChannel.isSelected()) {
                if (!contains) {
                    jsons.put(favorite);
                    Pref.FAVORITED_STREAMS.put(jsons.toString());
                    Platform.runLater(() -> {
                        favoriteChannelList.getEditor().textProperty().setValue(favorite.getString("label"));
                    });
                }
            } else {
                if (contains) {
                    jsons.remove(containIndex);
                    Pref.FAVORITED_STREAMS.put(jsons.toString());
                }
            }
            updateFavoriteList();
        });

        channelCustomInput.textProperty().addListener(new ChangeListener<String>() {

            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                checkFavorites();
            }

        });

        favoriteChannelList.setOnAction((final ActionEvent event) -> {
            final FavoriteStream favoriteStream;
            try {
                favoriteStream = favoriteChannelList.getValue();
            } catch (final ClassCastException ex) {
                String newLabel = favoriteChannelList.getEditor().textProperty().getValue();
                JSONArray jasons;
                try {
                    jasons = new JSONArray(Pref.FAVORITED_STREAMS.getString());
                } catch (final JSONException ex2) {
                    return;
                }
                boolean found = false;
                int foundIndex = 0;
                JSONObject foundJason = new JSONObject();
                for (int i = 0; i < jasons.length(); i++) {
                    JSONObject s;
                    try {
                        s = jasons.getJSONObject(i);
                    } catch (final JSONException ex3) {
                        continue;
                    }
                    if (s.getString("streamServiceKey").equals(streamServiceSelection.getValue().getKey())
                            && s.getString("channelName").equals(channelCustomInput.getText())) {
                        found = true;
                        foundIndex = i;
                        foundJason = s;
                        break;
                    }
                }
                if (found) {
                    foundJason.put("label", newLabel);
                    jasons.put(foundIndex, foundJason);
                    Pref.FAVORITED_STREAMS.put(jasons.toString());
                    updateFavoriteList();
                }
                return;
            }
            if (favoriteStream == null) return;

            Platform.runLater(() -> {
                final StreamService strSrvc = StreamServiceManager.getStreamServiceByKey(favoriteStream.GetKey());

                if (favChanTemp != null) {
                    if (!favChanTemp.isEnabled()) streamServiceSelection.getItems().remove(favChanTemp);
                    favChanTemp = null;
                }
                if (!streamServiceSelection.getItems().contains(strSrvc)) {
                    streamServiceSelection.getItems().add(strSrvc);
                    favChanTemp = strSrvc;
                }

                streamServiceSelection.getSelectionModel().select(strSrvc);

                favoriteChannelList.getSelectionModel().clearSelection();
                favoriteChannelList.getEditor().textProperty().set(favoriteStream.getLabel());
                channelCustomInput.setText(favoriteStream.GetChannelName());

                if (favoriteStream.GetChannelName().equals("") && strSrvc.hasDefaultChannel())
                    channelDefault.setSelected(true);
                else channelCustom.setSelected(true);
            });

        });

        // Select the custom channel radio button upon focusing the text field next to it
        channelCustomInput.focusedProperty().addListener((final ObservableValue<? extends Boolean> observable,
                final Boolean oldValue, final Boolean newValue) -> {
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
            Platform.runLater(() -> {
                channelCustom.setSelected(true);
                channelCustomInput.setText(lastChannel);
            });
        }

        // Add stream services to the combo box
        final List<StreamService> streamServices = StreamServiceManager.getEnabledStreamServices();
        streamServiceSelection.getItems().addAll(streamServices);

        // Indicate unavailable streams and update quality options
        streamServiceSelection.setOnAction((final ActionEvent event) -> {
            mLockStreamServiceSelection = false;

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

            if (favChanTemp != null && !favChanTemp.isEnabled()
                    && streamServiceSelection.getSelectionModel().getSelectedItem() != favChanTemp) {
                final StreamService runLaterTempChan = favChanTemp;
                Platform.runLater(() -> {
                    streamServiceSelection.getItems().remove(runLaterTempChan);
                });
            }

            updateQualityOptions();
            checkFavorites();

            autoswitchToggle.setDisable(!StreamServiceManager.getAutoswitchServices().contains(streamService));
        });

        // Select the stored last stream service
        final StreamService selectedService = StreamServiceManager
                .getStreamServiceByKey(Pref.LAST_STREAM_SERVICE.getString());
        if (streamServices.contains(selectedService)) {
            streamServiceSelection.getSelectionModel().select(selectedService);
        }

        // Make sure a value is selected
        if (streamServiceSelection.getValue() == null) {
            final StreamService defaultService = StreamServiceManager
                    .getStreamServiceByKey(Pref.LAST_STREAM_SERVICE.getDefaultString());
            if (streamServices.contains(defaultService)) {
                streamServiceSelection.getSelectionModel().select(defaultService);
            } else {
                streamServiceSelection.getSelectionModel().select(0);
            }
        }
        streamServiceSelection.getOnAction().handle(null);

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
        if (Environment.ALLOW_AUTOSWITCH) {
            autoswitchToggle.setSelected(Pref.AUTOSWITCH.getBoolean());
            autoswitchToggle.setDisable(!StreamServiceManager.getAutoswitchServices().contains(selectedService));
        } else {
            autoswitchToggle.setVisible(false);
        }

        // Update to the right stream state
        final StreamManager streamManager = StreamManager.getInstance();
        streamManager.addListener(this);
        onStateUpdated(streamManager, null, streamManager.getStreamState());

        // Update the stream info
        StreamInfo.addListener(this);
        StreamInfo.startRequestInterval();
    }

    private void updateFavoriteList() {
        List<FavoriteStream> favorites = new LinkedList<FavoriteStream>();
        String favString = Pref.FAVORITED_STREAMS.getString();
        JSONArray jsons;
        try {
            jsons = new JSONArray(favString);
        } catch (final JSONException ex) {
            return;
        }
        for (int i = 0; i < jsons.length(); i++) {
            JSONObject jason;
            try {
                jason = jsons.getJSONObject(i);
            } catch (final JSONException ex) {
                continue;
            }
            favorites.add(new FavoriteStream(jason));
        }

        favoriteChannelList.getItems().setAll(favorites);
        favoriteChannelList.setButtonCell(new ComboBoxCell<FavoriteStream>());
        favoriteChannelList
                .setCellFactory((final ListView<FavoriteStream> param) -> new ComboBoxCell<FavoriteStream>());
    }

    private void checkFavorites() {
        String favsString = Pref.FAVORITED_STREAMS.getString();
        JSONArray jsons;
        try {
            jsons = new JSONArray(favsString);

        } catch (final JSONException ex) {
            return;
        }
        for (int i = 0; i < jsons.length(); i++) {
            JSONObject jason;
            try {
                jason = jsons.getJSONObject(i);
            } catch (final JSONException ex) {
                continue;
            }
            String sel = streamServiceSelection.getValue().getKey();
            String jsel = jason.getString("streamServiceKey");
            String jchanname = jason.getString("channelName");
            String editBoxValue = channelCustomInput.getText();
            if ((editBoxValue.equals(jchanname) || (jchanname.equals("") && channelDefault.isSelected()))
                    && (sel.equals(jsel) || sel.equals("none"))) {
                // We recognize "none" as a key here because it lets removed streamservices
                // be removed from favorites, so you're not stuck with dead channels.
                // We also check if favorited channel name is empty and default button is ticked,
                // in which case only the stream service key needs to match a favorite
                // to support having different defaults saved on different services
                foundFav = true;
                foundFavName = jason.getString("label");
                break;
            }
        }
        final boolean ffound = foundFav;
        final String fffavname = foundFavName;
        Platform.runLater(() -> {
            if (!ffound) {
                favoriteChannel.setSelected(false);
                favoriteChannelList.getEditor().textProperty().setValue("");
            } else {
                if (channelDefault.isSelected()) channelCustomInput.setText("");
                favoriteChannelList.getEditor().textProperty().setValue(fffavname);
                favoriteChannel.setSelected(true);
            }
            favoriteChannelList.getSelectionModel().clearSelection();
        });
        foundFav = false;
        foundFavName = "";
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
    @SuppressWarnings("unused")
    private void startStream() {
        mAutoswitchEnabled = false;
        final StreamManager streamManager = StreamManager.getInstance();
        final StreamService selectedStreamService = streamServiceSelection.getValue();
        final Quality quality = qualitySelection.getValue();

        // Pick a default or custom channel
        if (channelDefault.isSelected() && selectedStreamService.hasDefaultChannel()) {
            if (Environment.ALLOW_AUTOSWITCH && Pref.AUTOSWITCH.getBoolean()
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
    public void onStateUpdated(final StreamManager streamManager, final StreamState oldState,
            final StreamState newState) {
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

    /**
     * Updates the last of stream services. To be called after stream services are enabled or disabled.
     */
    public void updateStreamServices() {
        Platform.runLater(() -> {
            final List<StreamService> streamServices = StreamServiceManager.getEnabledStreamServices();

            // Keep track of which stream service the user selected manually
            final StreamService selected = streamServiceSelection.getValue();
            if (selected != null && !mLockStreamServiceSelection) {
                mLastSelected = selected;
            }

            // Update the stream services
            streamServiceSelection.getItems().setAll(streamServices);

            // Select the right stream service
            if (streamServiceSelection.getItems().contains(mLastSelected) && mLockStreamServiceSelection) {
                streamServiceSelection.getSelectionModel().select(mLastSelected);
                mLockStreamServiceSelection = false;

            } else if (streamServiceSelection.getItems().contains(selected)) {
                streamServiceSelection.getSelectionModel().select(selected);
                mLockStreamServiceSelection = true;

            } else if (streamServices.size() > 0) {
                streamServiceSelection.getSelectionModel().select(0);
                mLockStreamServiceSelection = true;
            }
        });
    }

}
