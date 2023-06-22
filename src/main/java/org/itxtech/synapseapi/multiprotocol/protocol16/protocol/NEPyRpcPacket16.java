package org.itxtech.synapseapi.multiprotocol.protocol16.protocol;

import cn.nukkit.network.protocol.ProtocolInfo;
import lombok.ToString;
import org.msgpack.core.MessageBufferPacker;
import org.msgpack.core.MessagePack;
import org.msgpack.core.MessageUnpacker;
import org.msgpack.value.*;

import java.io.IOException;
import java.util.function.Consumer;

/**
 * author: MagicDroidX
 * Nukkit Project
 */
@ToString
public class NEPyRpcPacket16 extends Packet16 {

    public static final int NETWORK_ID = ProtocolInfo.PACKET_PY_RPC;

    public static Consumer<NEPyRpcPacket16> THREAD_DESERIALIZE;
    public static Consumer<NEPyRpcPacket16> THREAD_ENCRYPT;

    private static final byte[] UNKNOWN_BYTES_SENDING = new byte[]{8, -44, -108, 0};

    public Value data;

    public Runnable[] actions;
    public boolean encrypt;

    @Override
    public int pid() {
        return NETWORK_ID;
    }

    @Override
    public void decode() {
        try (MessageUnpacker unpacker = MessagePack.newDefaultUnpacker(this.getByteArray())) {
            if (unpacker.hasNext()) {
                data = unpacker.unpackValue();
            }
        } catch (IOException e) {
            throw new RuntimeException("MsgPack decode failed: " + e.getMessage(), e);
        }

        if (THREAD_DESERIALIZE != null) {
            THREAD_DESERIALIZE.accept(this);
        }
    }

    @Override
    public void encode() {
        this.reset();
        if (encrypt && THREAD_ENCRYPT != null) {
            THREAD_ENCRYPT.accept(this);
        }
        try (MessageBufferPacker packer = MessagePack.newDefaultBufferPacker()) {
            packer.packValue(data);
            this.putByteArray(packer.toByteArray());
        } catch (IOException e) {
            throw new RuntimeException("MsgPack encode failed: " + e.getMessage(), e);
        }
        this.put(UNKNOWN_BYTES_SENDING);
    }

}
