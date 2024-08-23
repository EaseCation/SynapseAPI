package org.itxtech.synapseapi.multiprotocol.protocol12130.protocol;

import cn.nukkit.network.protocol.ProtocolInfo;
import it.unimi.dsi.fastutil.ints.IntObjectPair;
import lombok.ToString;
import org.itxtech.synapseapi.multiprotocol.protocol12120.protocol.Packet12120;

/**
 * This is used to trigger a clientside cleanup of the dynamic container registry. Whenever the serverside
 */
@ToString
public class ContainerRegistryCleanupPacket12030 extends Packet12120 {
    public static final int NETWORK_ID = ProtocolInfo.CONTAINER_REGISTRY_CLEANUP_PACKET;

    /**
     * Full Container Names
     */
    public IntObjectPair<Integer>[] removedContainers = new IntObjectPair[0];

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

        putUnsignedVarInt(removedContainers.length);
        for (IntObjectPair<Integer> container : removedContainers) {
            putByte(container.leftInt()); // containerSlotType
            putOptional(container.right(), (stream, dynamicContainerId) -> stream.putUnsignedVarInt(dynamicContainerId));
        }
    }
}
