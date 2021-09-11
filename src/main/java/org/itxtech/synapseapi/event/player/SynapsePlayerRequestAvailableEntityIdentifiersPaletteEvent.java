package org.itxtech.synapseapi.event.player;

import cn.nukkit.Server;
import cn.nukkit.event.HandlerList;
import cn.nukkit.nbt.NBTIO;
import cn.nukkit.nbt.tag.CompoundTag;
import org.itxtech.synapseapi.SynapsePlayer;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.ByteOrder;

public class SynapsePlayerRequestAvailableEntityIdentifiersPaletteEvent extends SynapsePlayerEvent {

    private static final HandlerList handlers = new HandlerList();

    private int maxRid = 1000;

    private final byte[] data;
    private CompoundTag namedTag = null;

    public SynapsePlayerRequestAvailableEntityIdentifiersPaletteEvent(SynapsePlayer player, byte[] data) {
        super(player);
        this.data = data;
    }

    public static HandlerList getHandlers() {
        return handlers;
    }

    public CompoundTag getNamedTag() {
        if (namedTag == null) {
            try {
                namedTag = NBTIO.read(new ByteArrayInputStream(data), ByteOrder.LITTLE_ENDIAN, true);
            } catch (IOException e) {
                Server.getInstance().getLogger().logException(e);
                namedTag = new CompoundTag();
            }
        }

        return namedTag;
    }

    public SynapsePlayerRequestAvailableEntityIdentifiersPaletteEvent addEntity(String id) {
        this.getNamedTag().getList("idlist", CompoundTag.class).add(
                new CompoundTag()
                        .putBoolean("hasspawnegg", false)
                        .putBoolean("summonable", false)
                        .putString("id", id)
                        .putString("bid", "")
                        .putInt("rid", maxRid++)
        );
        return this;
    }

    public byte[] getData() {
        try {
            return namedTag != null ? NBTIO.writeNetwork(namedTag) : data;
        } catch (IOException e) {
            Server.getInstance().getLogger().logException(e);
            return data;
        }
    }
}
