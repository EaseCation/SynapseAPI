package org.itxtech.synapseapi.multiprotocol.protocol16.protocol.data;

import com.google.gson.Gson;

public class NetEaseJsonData {
    private static final Gson GSON = new Gson();

    public static final String EVENT_OPEN_STORE = "ON_OPEN_STORE";

    public String eventName;
    public String category;

    public NetEaseJsonData(String eventName, String category) {
        this.eventName = eventName;
        this.category = category;
    }

    public String toJson() {
        return GSON.toJson(this);
    }
}
