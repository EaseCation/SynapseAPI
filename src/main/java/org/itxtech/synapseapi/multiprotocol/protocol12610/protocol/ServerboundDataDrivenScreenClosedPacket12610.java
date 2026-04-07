package org.itxtech.synapseapi.multiprotocol.protocol12610.protocol;

import cn.nukkit.network.protocol.ProtocolInfo;
import lombok.ToString;
import org.itxtech.synapseapi.multiprotocol.common.ddui.DataDrivenScreenClosedReason;

/**
 * Sent from the client to the server when a data driven screen is closed.
 */
@ToString
public class ServerboundDataDrivenScreenClosedPacket12610 extends Packet12610 {
    public static final int NETWORK_ID = ProtocolInfo.SERVERBOUND_DATA_DRIVEN_SCREEN_CLOSED_PACKET;

    public int formId;
    public DataDrivenScreenClosedReason closeReason;

    @Override
    public int pid() {
        return NETWORK_ID;
    }

    @Override
    public void decode() {
        formId = getLInt();
        closeReason = getEnum(DataDrivenScreenClosedReason::byName);
    }

    @Override
    public void encode() {
    }
}
