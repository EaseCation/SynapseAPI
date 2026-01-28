package org.itxtech.synapseapi.multiprotocol.protocol11910.protocol;

import cn.nukkit.network.protocol.ProtocolInfo;
import lombok.ToString;

@ToString
public class EditorNetworkPacket11910 extends Packet11910 {
    public static final int NETWORK_ID = ProtocolInfo.EDITOR_NETWORK_PACKET;

    /// CompoundTag
    public byte[] payload;

    @Override
    public int pid() {
        return NETWORK_ID;
    }

    @Override
    public void decode() {
        payload = get();
    }

    @Override
    public void encode() {
        reset();
        put(payload);
    }
}
