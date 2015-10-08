package com.dopelives.dopestreamer.gui.controllers;

import java.net.URL;
import java.security.InvalidParameterException;
import java.util.LinkedList;
import java.util.List;
import java.util.ResourceBundle;

import org.json.JSONArray;
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
import javafx.util.StringConverter;

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
    /**
     * Temporary storage of selected favorite stream service. This adds a currently disabled stream service to the
     * dropdown list temporarily and is later removed from the list (unless the stream service is enabled again)
     */
    private StreamService mFavChanTemp = null;
    /** Temporarily lock favorites so we can modify fields through code */
    private boolean mLockFavTemp = false;

    @Override
    public synchronized void initialize(final URL location, final ResourceBundle resources) {

        // Oh boy
        favoriteChannelList.setConverter(new StringConverter<FavoriteStream>() {

            // I am still not sure why or how it needs this but it does
            // At some point JavaFX calls toString and sets the value of the editable combo box favoriteChanneList to
            // whatever toString returns meaning we need to return the string we want to be there, which is the label.
            // Also
            // means we can't set this value manually
            public String toString(FavoriteStream object) {
                if (object != null) {
                    return object.getLabel();
                } else {
                    return null;
                }
            }

            // fromString also just expects you to return the right object from just one single String. Luckily in this
            // case we can do it based on other environment values but I pray to god I never have to work with JavaFX
            // ever again
            // -Boomer
            // So because this editable box is the label we just return a new FavoriteStream with the other values
            // already put in place. This means that the editbox has been changed manually by the user and that in turn
            // means they want to change the name of a stored favorite
            public FavoriteStream fromString(String string) {
                return new FavoriteStream(new JSONObject().put("label", string)
                        .put("streamServiceKey", streamServiceSelection.getValue().getKey())
                        .put("channelName", channelCustomInput.getText()));
            }
        });

        // No need to set the button as this is an editable combo box
        favoriteChannelList
                .setCellFactory((final ListView<FavoriteStream> param) -> new ComboBoxCell<FavoriteStream>());
        updateFavoriteList();

        // We only need to check for favorites on these events
        channelDefault.setOnAction((final ActionEvent event) -> {
            checkFavorites();
        });
        channelCustom.setOnAction((final ActionEvent event) -> {
            checkFavorites();
        });
        channelCustomInput.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                checkFavorites();
            }
        });

        favoriteChannel.selectedProperty().addListener(new ChangeListener<Boolean>() {
            public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                if (mLockFavTemp) {
                    mLockFavTemp = false;
                    return;
                }

                // On checking/unchecking the Favorite checkbox we get all the data we need
                JSONObject favorite = new JSONObject();
                String favName = favoriteChannelList.getEditor().getText();
                if (favName.equals("")) favName = channelCustomInput.getText().toUpperCase();
                favorite.put("label", favName);
                favorite.put("streamServiceKey", streamServiceSelection.getValue().getKey());
                favorite.put("channelName", channelDefault.isSelected() ? "" : channelCustomInput.getText());

                // Get the stored favorites
                JSONArray jsonArray = new JSONArray();
                jsonArray = new JSONArray(Pref.FAVORITED_STREAMS.getString());

                boolean contains = false;
                int containIndex = 0;
                // And loop through them to see if we can find a match
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject tempJSON;
                    tempJSON = jsonArray.getJSONObject(i);

                    // No, .equals won't work between JSON objects because I dunno
                    // Whatever, you don't need to favorite the same channel
                    // under different labels anyway
                    String chan = tempJSON.getString("channelName");
                    String ky = tempJSON.getString("streamServiceKey");
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
                        jsonArray.put(favorite);
                        Pref.FAVORITED_STREAMS.put(jsonArray.toString());
                        Platform.runLater(() -> {
                            favoriteChannelList.getEditor().setText(favorite.getString("label"));
                        });
                    }
                } else {
                    if (contains) {
                        jsonArray.remove(containIndex);
                        Pref.FAVORITED_STREAMS.put(jsonArray.toString());
                    }
                }
                updateFavoriteList();
            }
        });

        // Whenever we select another favorite from the list
        favoriteChannelList.setOnAction((final ActionEvent event) -> {
            // Man JavaFX you are great
            FavoriteStream favoriteStream = favoriteChannelList.getValue();
            // If you enable editable combo box then this ^ can actually return a String unless you set a converter with
            // fromString and toString
            // I'm impressed they managed to return entirely the wrong kind of object though, that takes some doing
            if (favoriteStream == null) {
                return;
            }

            // This should only be true when the user has manually edited the combo box input, meaning they either want
            // to store a new favorite or change the name of an already stored favorite in which case we just get,
            // change, rebuild and store again.
            if (favoriteStream.GetKey().equals(streamServiceSelection.getValue().getKey())
                    && favoriteStream.GetChannelName().equals(channelCustomInput.getText())) {
                String newLabel = favoriteStream.getLabel();
                JSONArray jsonArray;
                jsonArray = new JSONArray(Pref.FAVORITED_STREAMS.getString());
                boolean found = false;
                int foundIndex = 0;
                JSONObject foundJSON = new JSONObject();
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject tempJSON = jsonArray.getJSONObject(i);
                    if (tempJSON.getString("streamServiceKey").equals(streamServiceSelection.getValue().getKey())
                            && tempJSON.getString("channelName").equals(channelCustomInput.getText())) {
                        found = true;
                        foundIndex = i;
                        foundJSON = tempJSON;
                        break;
                    }
                }
                if (found) {
                    foundJSON.put("label", newLabel);
                    jsonArray.put(foundIndex, foundJSON);
                    Pref.FAVORITED_STREAMS.put(jsonArray.toString());
                    updateFavoriteList();
                } else {
                    // If it's not already in there, put it there. Just change the value of favoriteChannel and it will
                    // do all the work for us
                    favoriteChannel.setSelected(true);
                }
                return;
            }

            // If it's not the textbox then they seleceted a favorite from the list
            final StreamService strSrvc = StreamServiceManager.getStreamServiceByKey(favoriteStream.GetKey());

            updateTemporaryStreamService(strSrvc);

            final String nNewLabel = favoriteStream.getLabel();
            final String nChannelName = favoriteStream.GetChannelName();
            Platform.runLater(() -> {

                streamServiceSelection.getSelectionModel().select(strSrvc);

                favoriteChannelList.getSelectionModel().clearSelection();
                favoriteChannelList.getEditor().setText(nNewLabel);
                channelCustomInput.setText(nChannelName);

                if (nChannelName.equals("") && strSrvc.hasDefaultChannel()) channelDefault.setSelected(true);
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

            // Remove any temporarily added channel
            if (mFavChanTemp != null && !mFavChanTemp.isEnabled()
                    && streamServiceSelection.getSelectionModel().getSelectedItem() != mFavChanTemp) {
                final StreamService runLaterTempChan = mFavChanTemp;
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

    public StreamService getSelectedStreamService() {
        return streamServiceSelection.getValue();
    }

    /**
     * You should probably call this from a Platform.runLater
     * 
     * @param selectStreamService
     *            The new StreamService you want to select
     */
    public void setSelectedStreamService(StreamService selectStreamService) {
        streamServiceSelection.getSelectionModel().select(selectStreamService);
    }

    /**
     * Feed this function whatever stream you're trying to set the streamServiceSelection selection to before you change
     * it. It will make sure that you can select that stream and remove the temporary StreamService as approriate
     * 
     * @param newPotentialTemporaryStream
     *            The StreamService you want to switch to
     */
    public void updateTemporaryStreamService(StreamService newPotentialTemporaryStream) {
        if (mFavChanTemp != null) {
            if (!mFavChanTemp.isEnabled()) streamServiceSelection.getItems().remove(mFavChanTemp);
            mFavChanTemp = null;
        }
        if (!streamServiceSelection.getItems().contains(newPotentialTemporaryStream)) {
            streamServiceSelection.getItems().add(newPotentialTemporaryStream);
            mFavChanTemp = newPotentialTemporaryStream;
        }
    }

    /**
     * This needs to be called any time you modify the value of Pref.FAVORITED_STREAMS
     */
    private void updateFavoriteList() {
        String favString = Pref.FAVORITED_STREAMS.getString();
        JSONArray jsonArray = new JSONArray(favString);
        List<FavoriteStream> favorites = new LinkedList<FavoriteStream>();
        for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject tempJSON;
            tempJSON = jsonArray.getJSONObject(i);
            favorites.add(new FavoriteStream(tempJSON));
        }
        Platform.runLater(() -> {
            favoriteChannelList.getItems().setAll(favorites);
        });
    }

    /**
     * This needs to be called any time streamServiceSelection, channelCustomInput, channelDefault or channelCustom
     * changes values. It checks if any of the inputs match an already stored favorite and automatically checks/unchecks
     * the favorite button as appropriate
     */
    private void checkFavorites() {
        JSONArray jsonArray = new JSONArray(Pref.FAVORITED_STREAMS.getString());

        String sel = streamServiceSelection.getValue().getKey();
        String editBoxValue = channelCustomInput.getText();
        boolean foundFav = false;
        String foundFavName = "";
        for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject tempJSON = jsonArray.getJSONObject(i);
            String jsel = tempJSON.getString("streamServiceKey");
            String jchanname = tempJSON.getString("channelName");
            if ((editBoxValue.equals(jchanname) || (jchanname.equals("") && channelDefault.isSelected()))
                    && (sel.equals(jsel) || sel.equals("none"))) {
                // We recognize "none" as a key here because it lets removed streamservices
                // be removed from favorites, so you're not stuck with dead channels.
                // We also check if favorited channel name is empty and default button is ticked,
                // in which case only the stream service key needs to match a favorite
                // to support having different defaults saved on different services
                foundFav = true;
                foundFavName = tempJSON.getString("label");
                break;
            }
        }

        final boolean ffound = foundFav;
        final String fffavname = foundFavName;
        Platform.runLater(() -> {
            if (!ffound) {
                mLockFavTemp = favoriteChannel.isSelected();
                favoriteChannel.setSelected(false);
                favoriteChannelList.getEditor().setText("");
            } else {
                mLockFavTemp = !favoriteChannel.isSelected();
                if (channelDefault.isSelected()) channelCustomInput.setText("");
                favoriteChannelList.getEditor().setText(fffavname);
                favoriteChannel.setSelected(true);
            }
            favoriteChannelList.getSelectionModel().clearSelection();
        });
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
            updateFavoriteList();
        });
    }

}
