package org.itxtech.synapseapi.multiprotocol.protocol11960.protocol;

import cn.nukkit.entity.data.Skin;
import cn.nukkit.network.protocol.DataPacket;
import cn.nukkit.network.protocol.PlayerSkinPacket;
import cn.nukkit.network.protocol.ProtocolInfo;
import lombok.ToString;
import org.itxtech.synapseapi.utils.ClassUtils;

import java.util.UUID;

/**
 * 1.19.62 HACK.
 */
@ToString
public class PlayerSkinPacket11960 extends Packet11960 {
    public static final int NETWORK_ID = ProtocolInfo.PLAYER_SKIN_PACKET;

    public UUID uuid;
    public Skin skin;
    public String newSkinName;
    public String oldSkinName;

    public boolean extend;
    public boolean error = false;

    @Override
    public int pid() {
        return NETWORK_ID;
    }

    @Override
    public void decode() {
        uuid = getUUID();
        skin = getSkin();

        int mark = this.getOffset();

        try {
            newSkinName = getString();

            if (this.newSkinName.isEmpty()) {
                this.setOffset(mark);

                this.error = !decodeExtended();
                return;
            }

            oldSkinName = getString();
            skin.setTrusted(getBoolean());
        } catch (Exception e) {
            this.setOffset(mark);

            this.error = !decodeExtended();
            return;
        }

        if (!this.feof()) {
            this.setOffset(mark);

            this.error = !decodeExtended();
        }
    }

    private boolean decodeExtended() {
        try {
            skin.setOverridingPlayerAppearance(this.getBoolean()); // 1.19.62

            newSkinName = getString();
            oldSkinName = getString();
            skin.setTrusted(getBoolean());
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public void encode() {
        reset();
        putUUID(uuid);
        putSkin(skin);

        if (extend) {
            this.putBoolean(skin.isOverridingPlayerAppearance()); // 1.19.62
        }

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
