package org.itxtech.synapseapi.multiprotocol.protocol121130.protocol;

import cn.nukkit.network.protocol.ProtocolInfo;
import lombok.ToString;

@ToString
public class ClientboundDebugRendererPacket121130 extends Packet121130 {
    public static final int NETWORK_ID = ProtocolInfo.CLIENTBOUND_DEBUG_RENDERER_PACKET;

    public static final int TYPE_INVALID = 0;
    public static final int TYPE_CLEAR = 1;
    public static final int TYPE_ADD_CUBE = 2;

    public int type = TYPE_INVALID;

    public String text = "";
    public float x;
    public float y;
    public float z;
    /// ARGB
    public int color;
    public long durationMillis;

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
        switch (type) {
            case TYPE_CLEAR -> {
                putString("cleardebugmarkers");
                putBoolean(false);
            }
            case TYPE_ADD_CUBE -> {
                putString("adddebugmarkercube");
                putBoolean(true);

                putString(text);
                putVector3f(x, y, z);
                putLInt(color);
                putLLong(durationMillis);
            }
            case TYPE_INVALID -> {
                putString("invalid");
                putBoolean(false);
            }
        }
    }
}
