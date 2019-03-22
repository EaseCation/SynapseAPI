package org.itxtech.synapseapi.event.player;

import cn.nukkit.event.Cancellable;
import cn.nukkit.event.HandlerList;
import cn.nukkit.math.Vector3;
import org.itxtech.synapseapi.SynapsePlayer;
import org.itxtech.synapseapi.multiprotocol.utils.LevelSoundEventEnum;

/**
 * Created by boybook on 16/6/25.
 */
public class SynapsePlayerBroadcastLevelSoundEvent extends SynapsePlayerEvent implements Cancellable {

    private static final HandlerList handlers = new HandlerList();
    private LevelSoundEventEnum levelSound;
    private Vector3 pos;
    private int extraData;
    private int pitch;
    private String entityIdentifier;
    private boolean isBabyMob;
    private boolean isGlobal;

    public SynapsePlayerBroadcastLevelSoundEvent(SynapsePlayer player, LevelSoundEventEnum levelSound, Vector3 pos, int extraData, int pitch, String entityIdentifier, boolean isBabyMob, boolean isGlobal) {
        super(player);
        this.levelSound = levelSound;
        this.pos = pos;
        this.extraData = extraData;
        this.pitch = pitch;
        this.entityIdentifier = entityIdentifier;
        this.isBabyMob = isBabyMob;
        this.isGlobal = isGlobal;
    }

    public static HandlerList getHandlers() {
        return handlers;
    }

    public LevelSoundEventEnum getLevelSound() {
        return levelSound;
    }

    public Vector3 getPos() {
        return pos;
    }

    public int getExtraData() {
        return extraData;
    }

    public int getPitch() {
        return pitch;
    }

    public String getEntityIdentifier() {
        return entityIdentifier;
    }

    public boolean isBabyMob() {
        return isBabyMob;
    }

    public boolean isGlobal() {
        return isGlobal;
    }

    public void setLevelSound(LevelSoundEventEnum levelSound) {
        this.levelSound = levelSound;
    }

    public void setPos(Vector3 pos) {
        this.pos = pos;
    }

    public void setExtraData(int extraData) {
        this.extraData = extraData;
    }

    public void setPitch(int pitch) {
        this.pitch = pitch;
    }

    public void setEntityIdentifier(String entityIdentifier) {
        this.entityIdentifier = entityIdentifier;
    }

    public void setBabyMob(boolean babyMob) {
        isBabyMob = babyMob;
    }

    public void setGlobal(boolean global) {
        isGlobal = global;
    }
}
