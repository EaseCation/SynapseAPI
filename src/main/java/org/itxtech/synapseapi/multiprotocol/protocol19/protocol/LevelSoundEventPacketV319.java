package org.itxtech.synapseapi.multiprotocol.protocol19.protocol;

import cn.nukkit.math.Vector3f;
import cn.nukkit.network.protocol.DataPacket;
import cn.nukkit.network.protocol.LevelSoundEventPacket;
import cn.nukkit.network.protocol.ProtocolInfo;
import org.itxtech.synapseapi.multiprotocol.AbstractProtocol;
import org.itxtech.synapseapi.multiprotocol.utils.LevelSoundEventEnum;
import org.itxtech.synapseapi.utils.ClassUtils;

import java.util.Optional;

public class LevelSoundEventPacketV319 extends Packet19 {
    public static final byte NETWORK_ID = ProtocolInfo.LEVEL_SOUND_EVENT_PACKET_V3;

    public int sound;
    public float x;
    public float y;
    public float z;
    public int extraData = -1;
    public String entityIdentifier;
    public boolean isBabyMob;
    public boolean isGlobal;

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
    }

    @Override
    public int pid() {
        return NETWORK_ID;
    }

    @Override
    public DataPacket fromDefault(DataPacket pk, AbstractProtocol protocol, boolean netease) {
        ClassUtils.requireInstance(pk, cn.nukkit.network.protocol.LevelSoundEventPacket.class);
        LevelSoundEventPacket packet = (cn.nukkit.network.protocol.LevelSoundEventPacket) pk;
        LevelSoundEventEnum sound = LevelSoundEventEnum.fromV12(packet.sound);
        this.sound = Optional.ofNullable(sound).orElse(LevelSoundEventEnum.SOUND_UNDEFINED).getV18();
        this.x = packet.x;
        this.y = packet.y;
        this.z = packet.z;
        this.extraData = Optional.ofNullable(sound).orElse(LevelSoundEventEnum.SOUND_UNDEFINED).translateTo18ExtraData(packet.extraData, packet.pitch, protocol, netease);
        this.entityIdentifier = ":";
        this.isBabyMob = packet.isBabyMob;
        this.isGlobal = packet.isGlobal;
        return this;
    }

    public static Class<? extends DataPacket> getDefaultPacket() {
        return cn.nukkit.network.protocol.LevelSoundEventPacket.class;
    }

}
