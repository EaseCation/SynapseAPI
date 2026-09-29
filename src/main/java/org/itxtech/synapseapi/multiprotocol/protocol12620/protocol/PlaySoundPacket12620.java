package org.itxtech.synapseapi.multiprotocol.protocol12620.protocol;

import cn.nukkit.math.Mth;
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
    /**
     * 1/8 格定点精确坐标，语义与 {@link PlaySoundPacket} 保持一致，必须随 fromDefault 一起复制，
     * 否则 1.26.20+ 客户端会静默退回整格坐标。
     */
    public boolean exactPosition;
    public double exactX;
    public double exactY;
    public double exactZ;

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
        if (this.exactPosition && this.exactY >= 0) {
            // 客户端按 1/8 格定点解析坐标，编码为 floor(坐标 * 8)
            this.putBlockVector3(Mth.floor(this.exactX * 8.0d), Mth.floor(this.exactY * 8.0d), Mth.floor(this.exactZ * 8.0d));
        } else {
            this.putBlockVector3(this.x << 3, this.y << 3, this.z << 3);
        }
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
        this.exactPosition = packet.exactPosition;
        this.exactX = packet.exactX;
        this.exactY = packet.exactY;
        this.exactZ = packet.exactZ;
        return this;
    }

    public static Class<? extends DataPacket> getDefaultPacket() {
        return PlaySoundPacket.class;
    }
}

