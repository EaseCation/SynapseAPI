package org.itxtech.synapseapi.multiprotocol.protocol11830.protocol;

import cn.nukkit.network.protocol.ProtocolInfo;
import lombok.ToString;

@ToString
public class AddVolumeEntityPacket11830 extends Packet11830 {
    public static final int NETWORK_ID = ProtocolInfo.ADD_VOLUME_ENTITY_PACKET;

    public int netId;
    /**
     * CompoundTag.
     */
    public byte[] components;
    public String identifier;
    public String instanceName;
    public int minX;
    public int minY;
    public int minZ;
    public int maxX;
    public int maxY;
    public int maxZ;
    public int dimension;
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
        putBlockVector3(minX, minY, minZ);
        putBlockVector3(maxX, maxY, maxZ);
        putVarInt(dimension);
        putString(engineVersion);
    }
}
