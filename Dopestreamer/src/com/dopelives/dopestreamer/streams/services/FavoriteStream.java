package com.dopelives.dopestreamer.streams.services;

import org.json.JSONObject;

import com.dopelives.dopestreamer.gui.combobox.ComboBoxItem;

public class FavoriteStream implements ComboBoxItem {

    private String label;
    private String iconUrl;
    private String streamServiceKey;
    private String channelName;

    public FavoriteStream(JSONObject Jason) {
        label = Jason.getString("label");
        streamServiceKey = Jason.getString("streamServiceKey");
        channelName = Jason.getString("channelName");
        try {
            iconUrl = StreamServiceManager.getStreamServiceByKey(streamServiceKey).getIconUrl();
        } catch (final NullPointerException ex) {
            iconUrl = "services/disabled.png";
            streamServiceKey = "none";
        }
    }

    public String GetChannelName() {
        return channelName;
    }

    public String GetKey() {
        return streamServiceKey;
    }

    @Override
    public String getLabel() {
        return label;
    }

    @Override
    public String getIconUrl() {
        return iconUrl;
    }

}
