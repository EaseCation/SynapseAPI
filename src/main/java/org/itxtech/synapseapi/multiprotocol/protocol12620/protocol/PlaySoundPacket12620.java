package org.itxtech.synapseapi.multiprotocol.protocol12620.protocol;

import cn.nukkit.network.protocol.DataPacket;
import cn.nukkit.network.protocol.PlaySoundPacket;
import cn.nukkit.network.protocol.ProtocolInfo;
import cn.nukkit.utils.BinaryStream;
import lombok.ToString;

import javax.annotation.Nullable;

@ToString
public class PlaySoundPacket12620 extends Packet12620 {
    public static final int NETWORK_ID = ProtocolInfo.PLAY_SOUND_PACKET;

    public String name;
    public int x;
    public int y;
    public int z;
    public float volume;
    public float pitch;
    @Nullable
    public Long serverSoundHandle;

    @Override
    public int pid() {
        return NETWORK_ID;
    }

    @Override
    public void decode() {
    }

    @Override
    public void encode() {
        this.reset();
        this.putString(this.name);
        this.putBlockVector3(this.x << 3, this.y << 3, this.z << 3);
        this.putLFloat(this.volume);
        this.putLFloat(this.pitch);
        this.putOptional(this.serverSoundHandle, BinaryStream::putLLong);
    }

    @Override
    public DataPacket fromDefault(DataPacket pk) {
        PlaySoundPacket packet = (PlaySoundPacket) pk;
        this.name = packet.name;
        this.x = packet.x;
        this.y = packet.y;
        this.z = packet.z;
        this.volume = packet.volume;
        this.pitch = packet.pitch;
        this.serverSoundHandle = packet.serverSoundHandle;
        return this;
    }

    public static Class<? extends DataPacket> getDefaultPacket() {
        return PlaySoundPacket.class;
    }
}

