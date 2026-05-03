package org.itxtech.synapseapi.multiprotocol.protocol12620.protocol;

import cn.nukkit.network.protocol.ProtocolInfo;
import org.itxtech.synapseapi.multiprotocol.common.level.DimensionDefinition;

/**
 * Data-driven dimensions.
 */
public class DimensionDataPacket12620 extends Packet12620 {
    public static final int NETWORK_ID = ProtocolInfo.DIMENSION_DATA_PACKET;

    public DimensionDefinition[] definitions = new DimensionDefinition[0];

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

        putUnsignedVarInt(definitions.length);
        for (DimensionDefinition definition : definitions) {
            putString(definition.identifier);

            putVarInt(definition.maximumHeight);
            putVarInt(definition.minimumHeight);
            putVarInt(definition.generatorType);
            putVarInt(definition.dimensionType);
        }
    }
}
