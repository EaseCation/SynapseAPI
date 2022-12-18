package org.itxtech.synapseapi.multiprotocol.protocol16.protocol;

import cn.nukkit.network.protocol.ProtocolInfo;
import lombok.ToString;
import org.msgpack.core.MessageBufferPacker;
import org.msgpack.core.MessagePack;
import org.msgpack.core.MessageUnpacker;
import org.msgpack.value.*;

import java.io.IOException;

/**
 * author: MagicDroidX
 * Nukkit Project
 */
@ToString
public class NEPyRpcPacket16 extends Packet16 {

    public static final int NETWORK_ID = ProtocolInfo.PACKET_PY_RPC;

    private static final byte[] UNKNOWN_BYTES_SENDING = new byte[]{8, -44, -108, 0};

    public Value data;

    @Override
    public int pid() {
        return NETWORK_ID;
    }

    @Override
    public void decode() {
        MessageUnpacker unpacker = MessagePack.newDefaultUnpacker(this.getByteArray());
        try {
            if (unpacker.hasNext()) {
                data = unpacker.unpackValue();
            }
        } catch (IOException e) {
            throw new RuntimeException("MsgPack decode failed: " + e.getMessage(), e);
        }
    }

    @Override
    public void encode() {
        this.reset();
        MessageBufferPacker packer = MessagePack.newDefaultBufferPacker();
        try {
            packer.packValue(data);
            this.putByteArray(packer.toByteArray());
            packer.close();
        } catch (IOException e) {
            throw new RuntimeException("MsgPack encode failed: " + e.getMessage(), e);
        }
        this.put(UNKNOWN_BYTES_SENDING);
    }

}
