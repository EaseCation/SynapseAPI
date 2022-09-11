package org.itxtech.synapseapi.multiprotocol.protocol11920.protocol;

import cn.nukkit.network.protocol.ProtocolInfo;
import lombok.ToString;

@ToString
public class ModalFormResponsePacket11920 extends Packet11920 {

    public static final int NETWORK_ID = ProtocolInfo.MODAL_FORM_RESPONSE_PACKET;

    public static final int CANCEL_REASON_CLOSED = 0;
    /**
     * Sent if a form is sent when the player is on a loading screen.
     */
    public static final int CANCEL_REASON_USER_BUSY = 1;

    public int formId;
    public boolean hasData;
    public String data = "null";
    public boolean canceled;
    public int cancelReason;

    @Override
    public int pid() {
        return NETWORK_ID;
    }

    @Override
    public void decode() {
        this.formId = this.getVarInt();

        this.hasData = this.getBoolean();
        if (this.hasData) {
            this.data = this.getString();
        }

        this.canceled = this.getBoolean();
        if (this.canceled) {
            this.cancelReason = this.getByte();
        }
    }

    @Override
    public void encode() {
    }
}
