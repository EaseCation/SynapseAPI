package org.itxtech.synapseapi.event.player;

import cn.nukkit.event.HandlerList;
import org.itxtech.synapseapi.SynapsePlayer;

public class SynapsePlayerInputModeChangeEvent extends SynapsePlayerEvent {

    private static final HandlerList handlers = new HandlerList();

    private final int inputModeFrom;
    private final int inputModeTo;

    public SynapsePlayerInputModeChangeEvent(SynapsePlayer player, int inputModeFrom, int inputModeTo) {
        super(player);
        this.inputModeFrom = inputModeFrom;
        this.inputModeTo = inputModeTo;
    }

    public static HandlerList getHandlers() {
        return handlers;
    }

    public int getInputModeFrom() {
        return inputModeFrom;
    }

    public int getInputMode() {
        return inputModeTo;
    }
}
