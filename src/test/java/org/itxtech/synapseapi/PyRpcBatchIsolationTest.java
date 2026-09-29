package org.itxtech.synapseapi;

import cn.nukkit.GameVersion;
import cn.nukkit.Server;
import cn.nukkit.network.Compressor;
import cn.nukkit.network.CompressionAlgorithm;
import cn.nukkit.network.protocol.BatchPacket;
import cn.nukkit.network.protocol.DataPacket;
import cn.nukkit.network.protocol.ProtocolInfo;
import cn.nukkit.utils.BinaryStream;
import org.itxtech.synapseapi.multiprotocol.PacketRegister;
import org.itxtech.synapseapi.multiprotocol.protocol16.protocol.NEPyRpcPacket16;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.msgpack.core.MessageBufferPacker;
import org.msgpack.core.MessagePack;
import org.msgpack.value.Value;

import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.msgpack.value.ValueFactory.*;

class PyRpcBatchIsolationTest {
    @Test
    void failedApplicationEventAndOversizedRpcPreserveOtherBatchPackets() throws IOException {
        byte[] valid = payload(newArray(newString("StoreBuySuccServerEvent")));
        byte[] badEvent = payload(newArray(newString("ModEventC2S"), newArray(newString("Minecraft"),
                newString("emote"), newString("PlayEmoteEvent"), newMap(new Value[0]))));
        for (byte[] ignored : new byte[][]{badEvent, new byte[NEPyRpcPacket16.MAX_PAYLOAD_BYTES + 1]}) {
            List<DataPacket> decoded = processBatch(wire(valid), wire(ignored), wire(valid));
            assertNotNull(decoded);
            assertEquals(3, decoded.size());
            assertEquals(1, ((NEPyRpcPacket16) decoded.get(0)).subPackets.size());
            assertTrue(((NEPyRpcPacket16) decoded.get(1)).subPackets.isEmpty());
            assertEquals(1, ((NEPyRpcPacket16) decoded.get(2)).subPackets.size());
        }
    }

    @Test
    void truncatedRpcStillRejectsTheBatch() throws IOException {
        byte[] valid = payload(newArray(newString("StoreBuySuccServerEvent")));
        BinaryStream broken = new BinaryStream();
        broken.putUnsignedVarInt(ProtocolInfo.PACKET_PY_RPC);
        broken.putUnsignedVarInt(128);
        broken.putByte((byte) 0);
        assertNull(processBatch(wire(valid), broken.getBuffer(), wire(valid)));
    }

    private static List<DataPacket> processBatch(byte[]... packets) {
        int protocol = GameVersion.V1_21_80.getProtocol();
        Server server = mock(Server.class);
        when(server.getCompressor()).thenReturn(Compressor.SNAPPY);
        try (MockedStatic<Server> servers = mockStatic(Server.class);
             MockedStatic<PacketRegister> registry = mockStatic(PacketRegister.class)) {
            servers.when(Server::getInstance).thenReturn(server);
            registry.when(() -> PacketRegister.getPacket(ProtocolInfo.PACKET_PY_RPC, protocol))
                    .thenAnswer(call -> new NEPyRpcPacket16());
            BinaryStream buffer = new BinaryStream();
            for (byte[] packet : packets) buffer.putByteArray(packet);
            BatchPacket batch = new BatchPacket();
            batch.payload = buffer.getBuffer();
            return SynapseEntry.processBatch(batch, protocol, true, CompressionAlgorithm.NONE);
        }
    }

    private static byte[] wire(byte[] payload) {
        BinaryStream buffer = new BinaryStream();
        buffer.putUnsignedVarInt(ProtocolInfo.PACKET_PY_RPC);
        buffer.putByteArray(payload);
        buffer.putLInt(9753608);
        return buffer.getBuffer();
    }

    private static byte[] payload(Value value) throws IOException {
        try (MessageBufferPacker packer = MessagePack.newDefaultBufferPacker()) {
            packer.packValue(value);
            return packer.toByteArray();
        }
    }
}
