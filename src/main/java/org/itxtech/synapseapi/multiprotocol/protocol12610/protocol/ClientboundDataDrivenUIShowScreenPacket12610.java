package org.itxtech.synapseapi.multiprotocol.protocol12610.protocol;

import cn.nukkit.network.protocol.ProtocolInfo;
import cn.nukkit.utils.BinaryStream;
import lombok.ToString;

import javax.annotation.Nullable;

/**
 * Allows the server to tell the client to show a Data Driven UI screen.
 */
@ToString
public class ClientboundDataDrivenUIShowScreenPacket12610 extends Packet12610 {
    public static final int NETWORK_ID = ProtocolInfo.CLIENTBOUND_DATA_DRIVEN_UI_SHOW_SCREEN_PACKET;

    /**
     * The ID of the screen to show.
     */
    public String screenId;
    public int formId;
    @Nullable
    public Integer dataInstanceId;

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
        putString(screenId);
        putLInt(formId);
        putOptional(dataInstanceId, BinaryStream::putLInt);
    }
}
