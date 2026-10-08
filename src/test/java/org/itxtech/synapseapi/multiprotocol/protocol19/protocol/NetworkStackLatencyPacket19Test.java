package org.itxtech.synapseapi.multiprotocol.protocol19.protocol;

import cn.nukkit.network.protocol.ProtocolInfo;
import org.junit.jupiter.api.Test;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class NetworkStackLatencyPacket19Test {
    @Test
    void ordinaryPacketKeepsItsExactLegacyBytes() {
        NetworkStackLatencyPacket19 packet = new NetworkStackLatencyPacket19();
        packet.timestamp = 123456789L;
        packet.isFromServer = true;
        packet.encode();
        ByteBuffer expected = ByteBuffer.allocate(10).order(ByteOrder.LITTLE_ENDIAN);
        expected.put((byte) ProtocolInfo.NETWORK_STACK_LATENCY_PACKET).putLong(packet.timestamp).put((byte) 1);
        assertArrayEquals(expected.array(), packet.getBuffer());
        assertNull(decode(packet.getBuffer()).boundaryIdentifier);
    }

    @Test
    void requestIdentifierRoundTripsAsTwoLittleEndianLongs() {
        NetworkStackLatencyPacket19 packet = new NetworkStackLatencyPacket19();
        packet.timestamp = 2708798963713L;
        packet.isFromServer = true;
        packet.boundaryIdentifier = new UUID(0x89abcdef01234567L, 0xfedcba9876543210L);
        packet.encode();
        assertEquals(26, packet.getCount());
        ByteBuffer expected = ByteBuffer.allocate(16).order(ByteOrder.LITTLE_ENDIAN)
                .putLong(packet.boundaryIdentifier.getMostSignificantBits()).putLong(packet.boundaryIdentifier.getLeastSignificantBits());
        assertArrayEquals(expected.array(), Arrays.copyOfRange(packet.getBuffer(), 10, 26));
        NetworkStackLatencyPacket19 decoded = decode(packet.getBuffer());
        assertEquals(packet.timestamp, decoded.timestamp);
        assertEquals(packet.boundaryIdentifier, decoded.boundaryIdentifier);
        assertEquals(decoded.getCount(), decoded.getOffset());
    }

    @Test
    void partialOrOverlongTrailerCannotCreateAnIdentifier() {
        NetworkStackLatencyPacket19 packet = new NetworkStackLatencyPacket19();
        packet.timestamp = 2708798963713L;
        packet.encode();
        for (int length : new int[]{1, 8, 15, 17, 32}) {
            assertNull(decode(Arrays.copyOf(packet.getBuffer(), 10 + length)).boundaryIdentifier);
        }
    }

    @Test
    void reusedDecoderDoesNotCarryThePriorRequestIdentifier() {
        NetworkStackLatencyPacket19 packet = new NetworkStackLatencyPacket19();
        packet.boundaryIdentifier = UUID.randomUUID();
        packet.encode();
        NetworkStackLatencyPacket19 reused = decode(packet.getBuffer());
        packet.boundaryIdentifier = null;
        packet.encode();
        reused.setBuffer(packet.getBuffer(), 0);
        assertEquals(ProtocolInfo.NETWORK_STACK_LATENCY_PACKET, reused.getUnsignedVarInt());
        reused.decode();
        assertNull(reused.boundaryIdentifier);
    }

    private static NetworkStackLatencyPacket19 decode(byte[] bytes) {
        NetworkStackLatencyPacket19 packet = new NetworkStackLatencyPacket19();
        packet.setBuffer(bytes);
        assertEquals(ProtocolInfo.NETWORK_STACK_LATENCY_PACKET, packet.getUnsignedVarInt());
        packet.decode();
        return packet;
    }
}
