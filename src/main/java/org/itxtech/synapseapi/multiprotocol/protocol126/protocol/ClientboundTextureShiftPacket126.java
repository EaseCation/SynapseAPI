package org.itxtech.synapseapi.multiprotocol.protocol126.protocol;

import cn.nukkit.network.protocol.ProtocolInfo;
import cn.nukkit.utils.BinaryStream;
import lombok.ToString;

/**
 * Sends a set of update properties for the texture shift system from the server to the client.
 */
@ToString
public class ClientboundTextureShiftPacket126 extends Packet126 {
    public static final int NETWORK_ID = ProtocolInfo.CLIENTBOUND_TEXTURE_SHIFT_PACKET;

    public static final int ACTION_INVALID = 0;
    public static final int ACTION_INITIALIZE = 1;
    public static final int ACTION_START = 2;
    public static final int ACTION_SET_ENABLED = 3;
    public static final int ACTION_SYNC = 4;

    public int action = ACTION_INVALID;
    public String collectionName;
    public String fromStep;
    public String toStep;
    public String[] allSteps = new String[0];
    public long currentLengthInTicks;
    public long totalLengthInTicks;
    public boolean enabled;

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
        putByte(action);
        putString(collectionName);
        putString(fromStep);
        putString(toStep);
        putArray(allSteps, BinaryStream::putString);
        putLLong(currentLengthInTicks);
        putLLong(totalLengthInTicks);
        putBoolean(enabled);
    }
}
