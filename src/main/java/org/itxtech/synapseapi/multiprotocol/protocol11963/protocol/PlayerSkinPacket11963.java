package org.itxtech.synapseapi.multiprotocol.protocol11963.protocol;

import cn.nukkit.entity.data.Skin;
import cn.nukkit.network.protocol.DataPacket;
import cn.nukkit.network.protocol.PlayerSkinPacket;
import cn.nukkit.network.protocol.ProtocolInfo;
import lombok.ToString;
import org.itxtech.synapseapi.utils.ClassUtils;

import java.util.UUID;

@ToString
public class PlayerSkinPacket11963 extends Packet11963 {
    public static final int NETWORK_ID = ProtocolInfo.PLAYER_SKIN_PACKET;

    public UUID uuid;
    public Skin skin;
    public String newSkinName;
    public String oldSkinName;

    @Override
    public int pid() {
        return NETWORK_ID;
    }

    @Override
    public void decode() {
        uuid = getUUID();
        skin = getSkin();
        newSkinName = getString();
        oldSkinName = getString();
        skin.setTrusted(getBoolean());
    }

    @Override
    public void encode() {
        reset();
        putUUID(uuid);
        putSkin(skin);
        putString(newSkinName);
        putString(oldSkinName);
        putBoolean(skin.isTrusted());
    }

    @Override
    public DataPacket fromDefault(DataPacket pk) {
        ClassUtils.requireInstance(pk, PlayerSkinPacket.class);

        PlayerSkinPacket packet = (PlayerSkinPacket) pk;
        this.uuid = packet.uuid;
        this.skin = packet.skin;
        this.newSkinName = packet.newSkinName;
        this.oldSkinName = packet.oldSkinName;

        return this;
    }

    public static Class<? extends DataPacket> getDefaultPacket() {
        return PlayerSkinPacket.class;
    }
}
