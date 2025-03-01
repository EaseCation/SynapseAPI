package org.itxtech.synapseapi.multiprotocol.protocol12170.protocol;

import cn.nukkit.network.protocol.ProtocolInfo;
import lombok.ToString;
import org.itxtech.synapseapi.multiprotocol.utils.DataFlagTranslator;

import java.util.BitSet;

/**
 * Only used in Server-Authoritative Movement with Rewind.
 * Sent periodically if the client has received corrections from the server.
 * Contains information about client-predictions that are relevant to movement.
 */
@ToString
public class ClientMovementPredictionSyncPacket12170 extends Packet12170 {
    public static final int NETWORK_ID = ProtocolInfo.CLIENT_MOVEMENT_PREDICTION_SYNC_PACKET;

    public BitSet entityDataFlags;

    public float scale;
    public float width;
    public float height;

    public float movementSpeed;
    public float underwaterMovementSpeed;
    public float lavaMovementSpeed;
    public float jumpStrength;
    public float health;
    public float hunger;

    public long entityUniqueId;

    public boolean flying;

    @Override
    public int pid() {
        return NETWORK_ID;
    }

    @Override
    public void decode() {
        entityDataFlags = getBitSet(DataFlagTranslator.COUNT[helper.getProtocol().ordinal()]);

        scale = getLFloat();
        width = getLFloat();
        height = getLFloat();

        movementSpeed = getLFloat();
        underwaterMovementSpeed = getLFloat();
        lavaMovementSpeed = getLFloat();
        jumpStrength = getLFloat();
        health = getLFloat();
        hunger = getLFloat();

        entityUniqueId = getEntityUniqueId();

        flying = getBoolean();
    }

    @Override
    public void encode() {
    }
}
