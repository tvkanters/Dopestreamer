package com.dopelives.dopestreamer.gui.controllers;

import java.io.File;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Timer;
import java.util.TimerTask;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.VBox;
import javafx.util.Callback;

import com.dopelives.dopestreamer.Environment;
import com.dopelives.dopestreamer.TrayManager;
import com.dopelives.dopestreamer.gui.Screen;
import com.dopelives.dopestreamer.gui.combobox.MediaPlayerCell;
import com.dopelives.dopestreamer.streams.players.MediaPlayer;
import com.dopelives.dopestreamer.streams.players.MediaPlayerManager;
import com.dopelives.dopestreamer.util.Pref;

public class Settings implements Initializable {

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
    private ComboBox<MediaPlayer> mediaPlayerSelection;
    @FXML
    private VBox mediaPlayerLocationWrapper;
    @FXML
    private TextField mediaPlayerLocation;
    @FXML
    private Button saveOutputButton;

    @Override
    public synchronized void initialize(final URL location, final ResourceBundle resources) {

        // Set the checkbox preferences
        autoStartToggle.setSelected(Pref.AUTO_START.getBoolean());
        showInTrayToggle.setSelected(Pref.SHOW_IN_TRAY.getBoolean());
        startMinimisedToggle.setSelected(Pref.START_MINIMISED.getBoolean());
        notificationToggle.setSelected(Pref.NOTIFICATIONS.getBoolean());
        notificationDingdongToggle.setSelected(Pref.NOTIFICATION_DINGDONG.getBoolean());

        // Add media players to the combo box
        final List<MediaPlayer> mediaPlayers = MediaPlayerManager.getMediaPlayers();
        mediaPlayerSelection.getItems().addAll(mediaPlayers);

        // Update the custom media player input field based on the selected media player
        mediaPlayerSelection.valueProperty().addListener(new ChangeListener<MediaPlayer>() {
            @Override
            public void changed(final ObservableValue<? extends MediaPlayer> observable, final MediaPlayer oldValue,
                    final MediaPlayer newValue) {
                final boolean customPlayer = newValue.getKey().equals("");
                mediaPlayerLocationWrapper.setVisible(customPlayer);
                mediaPlayerLocationWrapper.setManaged(customPlayer);

                Pref.DEFAULT_PLAYER.put(newValue.getKey());
            }
        });

        // Select the stored last media player
        final String selectedStreamServiceKey = Pref.DEFAULT_PLAYER.getString();
        for (int i = 0; i < mediaPlayers.size(); ++i) {
            if (mediaPlayers.get(i).getKey().equals(selectedStreamServiceKey)) {
                mediaPlayerSelection.getSelectionModel().select(i);
                break;
            }
        }

        // Make the media players look nice within the combo box
        mediaPlayerSelection.setButtonCell(new MediaPlayerCell());
        mediaPlayerSelection.setCellFactory(new Callback<ListView<MediaPlayer>, ListCell<MediaPlayer>>() {
            @Override
            public ListCell<MediaPlayer> call(final ListView<MediaPlayer> param) {
                return new MediaPlayerCell();
            }
        });

        // Set text of player location field
        mediaPlayerLocation.setText(Pref.PLAYER_LOCATION.getString());
    }

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
    public void onSaveOutput() {
        saveOutputButton.setDisable(true);

        final String filename = Environment.EXE_DIR
                + new SimpleDateFormat("'dopelog-'yyyyMMddhhmmss'.txt'").format(new Date());
        Environment.getOutputSpy().writeToFile(filename);

        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        saveOutputButton.setDisable(false);
                    }
                });
            }
        }, 1000);
    }

    @FXML
    public void onPlayerLocationChanged(final KeyEvent keyEvent) {
        if (keyEvent.getCode() == KeyCode.ENTER) {
            mediaPlayerLocation.getParent().requestFocus();
            return;
        }

        final String input = mediaPlayerLocation.getText();
        if (input.equals("420")) {
            for (final Screen screen : Screen.values()) {
                screen.getNode().getStylesheets().add(Environment.STYLE_FOLDER + "420.css");
            }
            return;
        }

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

}
