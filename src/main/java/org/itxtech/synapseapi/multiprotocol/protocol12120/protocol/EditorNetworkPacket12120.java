package org.itxtech.synapseapi.multiprotocol.protocol12120.protocol;

import cn.nukkit.network.protocol.ProtocolInfo;
import lombok.ToString;

@ToString
public class EditorNetworkPacket12120 extends Packet12120 {
    public static final int NETWORK_ID = ProtocolInfo.EDITOR_NETWORK_PACKET;

    public boolean routeToManager;
    /// CompoundTag
    public byte[] payload;

    @Override
    public int pid() {
        return NETWORK_ID;
    }

    @Override
    public void decode() {
        routeToManager = getBoolean();
        payload = get();
    }

    @Override
    public void encode() {
        reset();
        putBoolean(routeToManager);
        put(payload);
    }
}
