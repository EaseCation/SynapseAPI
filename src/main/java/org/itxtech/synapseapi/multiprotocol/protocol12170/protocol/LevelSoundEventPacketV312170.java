package org.itxtech.synapseapi.multiprotocol.protocol12170.protocol;

import cn.nukkit.math.Vector3f;
import cn.nukkit.network.protocol.DataPacket;
import cn.nukkit.network.protocol.LevelSoundEventPacket;
import cn.nukkit.network.protocol.ProtocolInfo;
import lombok.ToString;
import org.itxtech.synapseapi.multiprotocol.AbstractProtocol;
import org.itxtech.synapseapi.multiprotocol.utils.LevelSoundEventUtil;

@ToString
public class LevelSoundEventPacketV312170 extends Packet12170 {
    public static final int NETWORK_ID = ProtocolInfo.LEVEL_SOUND_EVENT_PACKET_V3;

    public int sound;
    public float x;
    public float y;
    public float z;
    public int extraData = -1;
    public String entityIdentifier = ":";
    public boolean isBabyMob;
    public boolean isGlobal;
    /**
     * @since 1.21.70
     */
    public long entityUniqueId = -1;

    @Override
    public void decode() {
        this.sound = (int) this.getUnsignedVarInt();
        Vector3f v = this.getVector3f();
        this.x = v.x;
        this.y = v.y;
        this.z = v.z;
        this.extraData = this.getVarInt();
        this.entityIdentifier = this.getString();
        this.isBabyMob = this.getBoolean();
        this.isGlobal = this.getBoolean();
        this.entityUniqueId = this.getLLong();
    }

    @Override
    public void encode() {
        this.reset();
        this.putUnsignedVarInt(this.sound);
        this.putVector3f(this.x, this.y, this.z);
        this.putVarInt(this.extraData);
        this.putString(this.entityIdentifier);
        this.putBoolean(this.isBabyMob);
        this.putBoolean(this.isGlobal);
        this.putLLong(this.entityUniqueId);
    }

    @Override
    public int pid() {
        return NETWORK_ID;
    }

    @Override
    public DataPacket fromDefault(DataPacket pk, AbstractProtocol protocol, boolean netease) {
        LevelSoundEventPacket packet = (LevelSoundEventPacket) pk;
        this.sound = packet.sound;
        this.extraData = LevelSoundEventUtil.translateTo18ExtraData(packet.sound, packet.extraData, packet.pitch, protocol, netease);
        this.x = packet.x;
        this.y = packet.y;
        this.z = packet.z;
        this.entityIdentifier = packet.entityIdentifier;
        this.isBabyMob = packet.isBabyMob;
        this.isGlobal = packet.isGlobal;
        this.entityUniqueId = packet.entityUniqueId;
        return this;
    }

    public static Class<? extends DataPacket> getDefaultPacket() {
        return LevelSoundEventPacket.class;
    }
}
