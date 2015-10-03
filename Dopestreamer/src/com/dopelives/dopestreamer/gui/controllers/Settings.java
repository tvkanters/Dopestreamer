package com.dopelives.dopestreamer.gui.controllers;

import java.io.File;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.ResourceBundle;

import javafx.application.Platform;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.VBox;

import com.dopelives.dopestreamer.Environment;
import com.dopelives.dopestreamer.TrayManager;
import com.dopelives.dopestreamer.gui.Screen;
import com.dopelives.dopestreamer.gui.combobox.ComboBoxCell;
import com.dopelives.dopestreamer.gui.combobox.ComboBoxTextCell;
import com.dopelives.dopestreamer.shell.Shell;
import com.dopelives.dopestreamer.streams.players.MediaPlayer;
import com.dopelives.dopestreamer.streams.players.MediaPlayerManager;
import com.dopelives.dopestreamer.streams.services.StreamService;
import com.dopelives.dopestreamer.streams.services.StreamServiceManager;
import com.dopelives.dopestreamer.streams.services.Vacker;
import com.dopelives.dopestreamer.util.Executor;
import com.dopelives.dopestreamer.util.Pref;

public class Settings extends ScrollableController {

    @FXML
    private CheckBox startOnBootToggle;
    @FXML
    private VBox startOnBootToggleWrapper;
    @FXML
    private CheckBox autoStartToggle;
    @FXML
    private CheckBox showInTrayToggle;
    @FXML
    private CheckBox startMinimisedToggle;
    @FXML
    private CheckBox notificationToggle;
    @FXML
    private CheckBox notificationDingdongToggle;
    @FXML
    private CheckBox livestreamerUpdateCheckToggle;
    @FXML
    private CheckBox hlsQuickBufferToggle;
    @FXML
    private CheckBox protocolToggle;
    @FXML
    private VBox protocolToggleWrapper;
    @FXML
    private ComboBox<MediaPlayer> mediaPlayerSelection;
    @FXML
    private VBox mediaPlayerLocationWrapper;
    @FXML
    private TextField mediaPlayerLocation;
    @FXML
    private ComboBox<Vacker.Server> vackerServerSelection;
    @FXML
    private Button saveOutputButton;
    @FXML
    private ComboBox<StreamService> streamingServicesDisabled;

    @Override
    public synchronized void initialize(final URL location, final ResourceBundle resources) {
        super.initialize(location, resources);

        final Shell shell = Shell.getInstance();

        // Set the checkbox preferences
        autoStartToggle.setSelected(Pref.AUTO_START.getBoolean());
        startMinimisedToggle.setSelected(Pref.START_MINIMISED.getBoolean());
        showInTrayToggle.setSelected(Pref.SHOW_IN_TRAY.getBoolean());
        notificationToggle.setSelected(Pref.NOTIFICATIONS.getBoolean());
        notificationDingdongToggle.setSelected(Pref.NOTIFICATION_DINGDONG.getBoolean());
        livestreamerUpdateCheckToggle.setSelected(Pref.LIVESTREAMER_UPDATE_CHECK.getBoolean());
        hlsQuickBufferToggle.setSelected(Pref.HLS_QUICK_BUFFER.getBoolean());

        // Set the start on boot checkbox
        if (shell.isStartOnBootSupported()) {
            if (shell.isStartOnBootRegistered()) {
                startOnBootToggle.setSelected(true);
            }
        } else {
            startOnBootToggleWrapper.setVisible(false);
            startOnBootToggleWrapper.setManaged(false);
        }

        // Set the protocol registration checkbox
        if (shell.isCustomProtocolSupported()) {
            if (shell.isCustomProtocolRegistered()) {
                protocolToggle.setSelected(true);
            }
        } else {
            protocolToggleWrapper.setVisible(false);
            protocolToggleWrapper.setManaged(false);
        }

        // Add stream services to the combo box
        final List<StreamService> streamServices = StreamServiceManager.getAllStreamServices();
        streamingServicesDisabled.getItems().setAll(streamServices);

        // Make the stream services look nice within the combo box
        streamingServicesDisabled.setButtonCell(new ComboBoxTextCell<>("Enable or disable services"));
        streamingServicesDisabled
                .setCellFactory((final ListView<StreamService> param) -> new ComboBoxCell<StreamService>());

        streamingServicesDisabled.setOnAction((final ActionEvent event) -> {
            final StreamService streamService = streamingServicesDisabled.getValue();
            if (streamService == null) {
                return;
            }

            final String disabledStreamServices = Pref.DISABLED_STREAM_SERVICES.getString();
            if (streamService.isEnabled()) {
                // Disable the stream service
                Pref.DISABLED_STREAM_SERVICES.put((!disabledStreamServices.equals("") ? disabledStreamServices + ","
                        : "") + streamService.getKey());
            } else {
                // Enable the stream service
                final List<String> disabledStreamServicesSplit = new ArrayList<String>(Arrays
                        .asList(disabledStreamServices.split(",")));
                disabledStreamServicesSplit.remove(streamService.getKey());
                Pref.DISABLED_STREAM_SERVICES.put(String.join(",", disabledStreamServicesSplit));
            }

            Platform.runLater(() -> {
                streamingServicesDisabled.getSelectionModel().clearSelection();
                streamingServicesDisabled.getItems().setAll(streamServices);
            });

            ((Streams) Screen.STREAMS.getController()).updateStreamServices();
        });

        // Add media players to the combo box
        final List<MediaPlayer> mediaPlayers = MediaPlayerManager.getMediaPlayers();
        mediaPlayerSelection.getItems().addAll(mediaPlayers);

        // Update the custom media player input field based on the selected media player
        mediaPlayerSelection.valueProperty().addListener((final ObservableValue<? extends MediaPlayer> observable,
                final MediaPlayer oldValue, final MediaPlayer newValue) -> {
            final boolean customPlayer = newValue.getKey().equals("");
            mediaPlayerLocationWrapper.setVisible(customPlayer);
            mediaPlayerLocationWrapper.setManaged(customPlayer);

            Pref.DEFAULT_PLAYER.put(newValue.getKey());
        });

        // Select the stored last media player
        MediaPlayer selectedMediaPlayer = MediaPlayerManager.getMediaPlayerByKey(Pref.DEFAULT_PLAYER.getString());
        if (selectedMediaPlayer == null) {
            selectedMediaPlayer = MediaPlayerManager.getMediaPlayerByKey(Pref.DEFAULT_PLAYER.getDefaultString());
        }
        mediaPlayerSelection.getSelectionModel().select(selectedMediaPlayer);

        // Make the media players look nice within the combo box
        mediaPlayerSelection.setButtonCell(new ComboBoxCell<MediaPlayer>());
        mediaPlayerSelection.setCellFactory((final ListView<MediaPlayer> param) -> new ComboBoxCell<MediaPlayer>());

        // Set text of player location field
        mediaPlayerLocation.setText(Pref.PLAYER_LOCATION.getString());

        // Add Vacker servers to the combo box
        final List<Vacker.Server> vackerServers = Arrays.asList(Vacker.Server.values());
        vackerServerSelection.getItems().addAll(vackerServers);

        // Update the Vacker server based on the selection
        vackerServerSelection.valueProperty().addListener((final ObservableValue<? extends Vacker.Server> observable,
                final Vacker.Server oldValue, final Vacker.Server newValue) -> {
            Pref.VACKER_SERVER.put(newValue.getKey());
        });

        // Select the preferred Vacker servers
        vackerServerSelection.getSelectionModel().select(Vacker.Server.getSelected());

        // Make the Vacker servers look nice within the combo box
        vackerServerSelection.setButtonCell(new ComboBoxCell<Vacker.Server>());
        vackerServerSelection
                .setCellFactory((final ListView<Vacker.Server> param) -> new ComboBoxCell<Vacker.Server>());
    }

    @Override
    public void onActived() {}

    @FXML
    public void onAutoStartToggle() {
        Pref.AUTO_START.put(autoStartToggle.isSelected());
    }

    @FXML
    public void onShowInTrayToggle() {
        final boolean showInTray = showInTrayToggle.isSelected();
        Pref.SHOW_IN_TRAY.put(showInTray);

        if (showInTray) {
            TrayManager.getInstance().show();
        } else {
            TrayManager.getInstance().hide();
        }
    }

    @FXML
    public void onStartMinimisedToggle() {
        Pref.START_MINIMISED.put(startMinimisedToggle.isSelected());
    }

    @FXML
    public void onNotificationToggle() {
        Pref.NOTIFICATIONS.put(notificationToggle.isSelected());
    }

    @FXML
    public void onNotificationDingdongToggle() {
        Pref.NOTIFICATION_DINGDONG.put(notificationDingdongToggle.isSelected());
    }

    @FXML
    public void onLivestreamerUpdateCheckToggle() {
        Pref.LIVESTREAMER_UPDATE_CHECK.put(livestreamerUpdateCheckToggle.isSelected());
    }

    @FXML
    public void onHlsQuickBufferToggle() {
        Pref.HLS_QUICK_BUFFER.put(hlsQuickBufferToggle.isSelected());
    }

    @FXML
    public void onProtocolToggle() {
        if (protocolToggle.isSelected()) {
            final boolean success = Shell.getInstance().registerCustomProtocol();
            if (!success) {
                protocolToggle.setSelected(false);
            }
        } else {
            final boolean success = Shell.getInstance().unregisterCustomProtocol();
            if (!success) {
                protocolToggle.setSelected(true);
            }
        }
    }

    @FXML
    public void onStartOnBootToggle() {
        if (startOnBootToggle.isSelected()) {
            final boolean success = Shell.getInstance().registerStartOnBoot();
            if (!success) {
                startOnBootToggle.setSelected(false);
            }
        } else {
            final boolean success = Shell.getInstance().unregisterStartOnBoot();
            if (!success) {
                startOnBootToggle.setSelected(true);
            }
        }
    }

    @FXML
    public void onPlayerLocationChanged(final KeyEvent keyEvent) {
        // Release focus upon pressing enter
        if (keyEvent.getCode() == KeyCode.ENTER) {
            mediaPlayerLocation.getParent().requestFocus();
            return;
        }

        final String input = mediaPlayerLocation.getText();

        // Check input validity
        final boolean inputValid;
        if (input.equals("")) {
            inputValid = true;
        } else {
            final File inputFile = new File(input);
            inputValid = inputFile.exists() && !inputFile.isDirectory();
        }

        ControllerHelper.setCssClass(mediaPlayerLocation, "invalid", !inputValid);
        if (inputValid) {
            Pref.PLAYER_LOCATION.put(input);
        }
    }

    @FXML
    public void onSaveOutput() {
        saveOutputButton.setDisable(true);

        final String filename = Environment.EXE_DIR
                + new SimpleDateFormat("'dopelog-'yyyyMMddhhmmss'.txt'").format(new Date());
        Environment.getOutputSpy().writeToFile(filename);

        Executor.schedule(() -> {
            Platform.runLater(() -> {
                saveOutputButton.setDisable(false);
            });
        }, 1000);
    }
}
