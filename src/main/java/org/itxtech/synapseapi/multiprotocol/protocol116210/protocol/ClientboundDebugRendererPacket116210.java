package org.itxtech.synapseapi.multiprotocol.protocol116210.protocol;

import cn.nukkit.network.protocol.ProtocolInfo;
import lombok.ToString;

@ToString
public class ClientboundDebugRendererPacket116210 extends Packet116210 {
    public static final int NETWORK_ID = ProtocolInfo.CLIENTBOUND_DEBUG_RENDERER_PACKET;

    public static final int TYPE_INVALID = 0;
    public static final int TYPE_CLEAR = 1;
    public static final int TYPE_ADD_CUBE = 2;

    public int type = TYPE_INVALID;

    public String text = "";
    public float x;
    public float y;
    public float z;
    public float red;
    public float green;
    public float blue;
    public float alpha;
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
        putLInt(type);
        if (type == TYPE_ADD_CUBE) {
            putString(text);
            putVector3f(x, y, z);
            putLFloat(red);
            putLFloat(green);
            putLFloat(blue);
            putLFloat(alpha);
            putLLong(durationMillis);
        }
    }
}
