package org.itxtech.synapseapi.multiprotocol.protocol18.protocol;

import cn.nukkit.math.Vector3f;
import cn.nukkit.network.protocol.DataPacket;
import cn.nukkit.network.protocol.ProtocolInfo;
import org.itxtech.synapseapi.multiprotocol.AbstractProtocol;
import org.itxtech.synapseapi.multiprotocol.utils.LevelSoundEventIDTranslator;
import org.itxtech.synapseapi.utils.ClassUtils;

public class LevelSoundEventPacket18 extends Packet18 {
    public static final int NETWORK_ID = ProtocolInfo.LEVEL_SOUND_EVENT_PACKET;

    public int sound;
    public float x;
    public float y;
    public float z;
    public int extraData = -1; //TODO: Check name
    public int pitch = 1; //TODO: Check name
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
        this.pitch = this.getVarInt();
        this.isBabyMob = this.getBoolean();
        this.isGlobal = this.getBoolean();
    }

    @Override
    public void encode() {
        this.reset();
        this.putUnsignedVarInt(this.sound);
        this.putVector3f(this.x, this.y, this.z);
        this.putVarInt(this.extraData);
        this.putVarInt(this.pitch);
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
		cn.nukkit.network.protocol.LevelSoundEventPacket packet = (cn.nukkit.network.protocol.LevelSoundEventPacket) pk;
		this.sound = LevelSoundEventIDTranslator.translateTo14Id(packet.sound);
		this.x = packet.x;
		this.y = packet.y;
		this.z = packet.z;
		this.extraData = LevelSoundEventIDTranslator.translateTo18ExtraData(this.sound, packet.extraData, packet.pitch, protocol, netease);
		this.pitch = packet.pitch;
		this.isBabyMob = packet.isBabyMob;
		this.isGlobal = packet.isGlobal;
		return this;
	}

    public static Class<? extends DataPacket> getDefaultPacket() {
        return cn.nukkit.network.protocol.LevelSoundEventPacket.class;
    }
}
