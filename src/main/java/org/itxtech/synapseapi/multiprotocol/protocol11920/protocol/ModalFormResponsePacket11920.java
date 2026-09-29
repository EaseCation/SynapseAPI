package org.itxtech.synapseapi.multiprotocol.protocol11920.protocol;

import cn.nukkit.network.protocol.ProtocolInfo;
import cn.nukkit.utils.JsonUtil;
import lombok.ToString;
import tools.jackson.core.JacksonException;

import javax.annotation.Nullable;

import static cn.nukkit.SharedConstants.*;

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
    @Nullable
    public Object data;
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
            this.data = this.getJson();
        }

        this.canceled = this.getBoolean();
        if (this.canceled) {
            this.cancelReason = this.getByte();
        }
    }

    @Override
    public void encode() {
    }

    @Nullable
    private Object getJson() {
        int length = (int) this.getUnsignedVarInt();
        if (length < 0 || length > MAX_MODAL_FORM_RESPONSE_DATA_LENGTH) {
            throw new IllegalArgumentException("Form response exceeds maximum length");
        }
        if (!this.isReadable(length)) {
            throw new IllegalArgumentException("Form response length exceeds packet data");
        }

        byte[] data = this.get(length);
        try {
            return JsonUtil.UNTRUSTED_JSON_MAPPER.readValue(data, Object.class);
        } catch (JacksonException e) {
            throw new IllegalArgumentException("Invalid form response JSON", e);
        }
    }
}
