package org.itxtech.synapseapi.multiprotocol.protocol11810.protocol;

import cn.nukkit.network.protocol.ProtocolInfo;
import lombok.ToString;

@ToString
public class AddVolumeEntityPacket11810 extends Packet11810 {
    public static final int NETWORK_ID = ProtocolInfo.ADD_VOLUME_ENTITY_PACKET;

    public int netId;
    /**
     * CompoundTag.
     */
    public byte[] components;
    public String identifier;
    public String instanceName;
    public String engineVersion;

    @Override
    public int pid() {
        return NETWORK_ID;
    }

    @Override
    public void decode() {
    }

    @Override
    public void encode() {
        reset();
        putUnsignedVarInt(netId);
        put(components);
        putString(identifier);
        putString(instanceName);
        putString(engineVersion);
    }
}
