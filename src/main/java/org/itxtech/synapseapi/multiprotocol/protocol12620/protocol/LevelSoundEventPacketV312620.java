package org.itxtech.synapseapi.multiprotocol.protocol12620.protocol;

import cn.nukkit.math.Vector3f;
import cn.nukkit.network.protocol.DataPacket;
import cn.nukkit.network.protocol.LevelSoundEventPacket;
import cn.nukkit.network.protocol.ProtocolInfo;
import cn.nukkit.utils.BinaryStream;
import lombok.ToString;
import org.itxtech.synapseapi.multiprotocol.AbstractProtocol;
import org.itxtech.synapseapi.multiprotocol.utils.LevelSoundEventUtil;

import javax.annotation.Nullable;

@ToString
public class LevelSoundEventPacketV312620 extends Packet12620 {
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
    /**
     * @since 1.26.20
     */
    @Nullable
    public Vector3f fireAtPosition;

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
        this.fireAtPosition = this.getOptional(BinaryStream::getVector3f);
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
        this.putOptional(this.fireAtPosition, BinaryStream::putVector3f);
    }

    @Override
    public int pid() {
        return NETWORK_ID;
    }

    @Override
    public DataPacket fromDefault(DataPacket pk, AbstractProtocol protocol, boolean netease) {
        LevelSoundEventPacket packet = (LevelSoundEventPacket) pk;
        this.sound = LevelSoundEventUtil.translateIdToClient(packet.sound, protocol, netease);
        this.extraData = LevelSoundEventUtil.translateTo18ExtraData(packet.sound, packet.extraData, packet.pitch, protocol, netease);
        this.x = packet.x;
        this.y = packet.y;
        this.z = packet.z;
        this.entityIdentifier = packet.entityIdentifier;
        this.isBabyMob = packet.isBabyMob;
        this.isGlobal = packet.isGlobal;
        this.entityUniqueId = packet.entityUniqueId;
        this.fireAtPosition = packet.fireAtPosition;
        return this;
    }

    public static Class<? extends DataPacket> getDefaultPacket() {
        return LevelSoundEventPacket.class;
    }
}
