package com.dopelives.dopestreamer.gui.controllers;

import java.io.File;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import javafx.util.Callback;

import com.dopelives.dopestreamer.Environment;
import com.dopelives.dopestreamer.TrayManager;
import com.dopelives.dopestreamer.gui.Screen;
import com.dopelives.dopestreamer.gui.combobox.MediaPlayerCell;
import com.dopelives.dopestreamer.gui.combobox.VackerServerCell;
import com.dopelives.dopestreamer.shell.Shell;
import com.dopelives.dopestreamer.streams.players.MediaPlayer;
import com.dopelives.dopestreamer.streams.players.MediaPlayerManager;
import com.dopelives.dopestreamer.streams.services.Vacker;
import com.dopelives.dopestreamer.util.Pref;

public class Settings implements Initializable {

    /** The factor to multiple the original scroll speed with */
    private static final double SCROLL_SPEED = 3;

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
    private ScrollPane scrollPane;

    /** Whether or not the scroll bar is being dragged */
    private boolean mIsDragging = false;

    @Override
    public synchronized void initialize(final URL location, final ResourceBundle resources) {
        final Shell shell = Shell.getInstance();

        // Set the checkbox preferences
        autoStartToggle.setSelected(Pref.AUTO_START.getBoolean());
        startMinimisedToggle.setSelected(Pref.START_MINIMISED.getBoolean());
        showInTrayToggle.setSelected(Pref.SHOW_IN_TRAY.getBoolean());
        notificationToggle.setSelected(Pref.NOTIFICATIONS.getBoolean());
        notificationDingdongToggle.setSelected(Pref.NOTIFICATION_DINGDONG.getBoolean());

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

        // Add Vacker servers to the combo box
        final List<Vacker.Server> vackerServers = Arrays.asList(Vacker.Server.values());
        vackerServerSelection.getItems().addAll(vackerServers);

        // Update the Vacker server based on the selection
        vackerServerSelection.valueProperty().addListener(new ChangeListener<Vacker.Server>() {
            @Override
            public void changed(final ObservableValue<? extends Vacker.Server> observable,
                    final Vacker.Server oldValue, final Vacker.Server newValue) {
                Pref.VACKER_SERVER.put(newValue.getKey());
            }
        });

        // Select the preferred Vacker servers
        vackerServerSelection.getSelectionModel().select(Vacker.Server.getSelected());

        // Make the Vacker servers look nice within the combo box
        vackerServerSelection.setButtonCell(new VackerServerCell());
        vackerServerSelection.setCellFactory(new Callback<ListView<Vacker.Server>, ListCell<Vacker.Server>>() {
            @Override
            public ListCell<Vacker.Server> call(final ListView<Vacker.Server> param) {
                return new VackerServerCell();
            }
        });

        // Make the scroll speed not be painfully slow
        scrollPane.vvalueProperty().addListener(new ChangeListener<Number>() {
            /** The value set by this object to prevent recursive updates */
            private double mSetValue = 0;

            @Override
            public void changed(final ObservableValue<? extends Number> observable, final Number oldVal,
                    final Number newVal) {
                final double oldValue = oldVal.doubleValue();
                final double newValue = newVal.doubleValue();

                // Recursive update and dragging detection
                if (newValue == mSetValue || mIsDragging) {
                    return;
                }

                final double difference = newValue - oldValue;
                mSetValue = Math.max(scrollPane.getVmin(),
                        Math.min(scrollPane.getVmax(), newValue + difference * (SCROLL_SPEED - 1)));

                scrollPane.vvalueProperty().set(mSetValue);
            }
        });

        // Prevent scroll bar dragging from twitching due to the scroll speed fix
        scrollPane.setOnMouseEntered(new EventHandler<MouseEvent>() {
            @Override
            public void handle(final MouseEvent event) {
                scrollPane.setOnMouseEntered(null);
                final Set<Node> nodes = scrollPane.lookupAll(".scroll-bar .thumb");
                for (final Node node : nodes) {
                    node.setOnMousePressed(new EventHandler<MouseEvent>() {
                        @Override
                        public void handle(final MouseEvent event) {
                            mIsDragging = true;
                        }
                    });
                    node.setOnMouseReleased(new EventHandler<MouseEvent>() {
                        @Override
                        public void handle(final MouseEvent event) {
                            mIsDragging = false;
                        }
                    });
                }
            }
        });
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

}
