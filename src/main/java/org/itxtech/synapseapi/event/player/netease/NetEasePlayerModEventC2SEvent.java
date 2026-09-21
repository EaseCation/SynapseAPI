package org.itxtech.synapseapi.event.player.netease;

import cn.nukkit.event.HandlerList;
import com.google.gson.JsonObject;
import org.itxtech.synapseapi.SynapsePlayer;
import org.itxtech.synapseapi.event.player.SynapsePlayerEvent;

public class NetEasePlayerModEventC2SEvent extends SynapsePlayerEvent {

    private static final HandlerList handlers = new HandlerList();

    private final String modName;
    private final String systemName;
    private final String customEventName;
    private final JsonObject args;

    public NetEasePlayerModEventC2SEvent(SynapsePlayer player, String modName, String systemName, String eventName, JsonObject args) {
        super(player);
        this.modName = modName;
        this.systemName = systemName;
        this.customEventName = eventName;
        this.args = args;
    }

    public String getModName() {
        return modName;
    }

    public String getSystemName() {
        return systemName;
    }

    public String getCustomEventName() {
        return customEventName;
    }

    public JsonObject getArgs() {
        return args;
    }

    public static HandlerList getHandlers() {
        return handlers;
    }

}
