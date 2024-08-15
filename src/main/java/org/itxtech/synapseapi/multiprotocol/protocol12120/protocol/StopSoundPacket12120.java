package org.itxtech.synapseapi.multiprotocol.protocol12120.protocol;

import cn.nukkit.network.protocol.DataPacket;
import cn.nukkit.network.protocol.ProtocolInfo;
import cn.nukkit.network.protocol.StopSoundPacket;
import lombok.ToString;
import org.itxtech.synapseapi.utils.ClassUtils;

@ToString
public class StopSoundPacket12120 extends Packet12120 {
    public static final int NETWORK_ID = ProtocolInfo.STOP_SOUND_PACKET;

    public String name;
    public boolean stopAll;
    public boolean stopMusicLegacy;

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
        this.putBoolean(this.stopAll);
        this.putBoolean(this.stopMusicLegacy);
    }

    @Override
    public DataPacket fromDefault(DataPacket pk) {
        ClassUtils.requireInstance(pk, StopSoundPacket.class);

        StopSoundPacket packet = (StopSoundPacket) pk;
        this.name = packet.name;
        this.stopAll = packet.stopAll;
        this.stopMusicLegacy = packet.stopMusicLegacy;

        return this;
    }

    public static Class<? extends DataPacket> getDefaultPacket() {
        return StopSoundPacket.class;
    }
}

