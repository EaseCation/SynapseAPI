package org.itxtech.synapseapi.network.synlib;

import io.netty.buffer.Unpooled;
import io.netty.channel.embedded.EmbeddedChannel;
import org.itxtech.synapseapi.network.protocol.spp.RedirectPacket;
import org.junit.jupiter.api.Test;

import java.nio.ByteBuffer;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;


class InboundPacketTimeTest {
    @Test
    void enabledEntranceStampsBeforePublishingTheRawPacket() {
        EmbeddedChannel channel = new EmbeddedChannel(new SynapsePacketDecoder(true));
        long before = System.nanoTime();
        channel.writeInbound(Unpooled.wrappedBuffer(frame()));
        long after = System.nanoTime();
        RedirectPacket packet = channel.readInbound();
        assertTrue(packet.receivedNanos - before >= 0);
        assertTrue(after - packet.receivedNanos >= 0);
        assertNull(packet.sessionId);
        assertSame(channel, packet.receivedChannel);
        assertFalse(channel.finishAndReleaseAll());
    }

    @Test
    void disabledEntranceKeepsTheLegacyUnstampedPath() {
        EmbeddedChannel channel = new EmbeddedChannel(new SynapsePacketDecoder());
        channel.writeInbound(Unpooled.wrappedBuffer(frame()));
        RedirectPacket packet = channel.readInbound();
        assertEquals(0, packet.receivedNanos);
        assertNull(packet.receivedChannel);
        assertFalse(channel.finishAndReleaseAll());
    }

    private static byte[] frame() {
        RedirectPacket packet = new RedirectPacket();
        packet.sessionId = UUID.randomUUID();
        packet.mcpeBuffer = new byte[]{1, 2};
        packet.encode();
        byte[] body = packet.getBuffer();
        return ByteBuffer.allocate(body.length + SynapseProtocolHeader.HEAD_LENGTH).putShort(SynapseProtocolHeader.MAGIC).put(packet.pid())
                .putInt(body.length).put(body).array();
    }

    @Test
    void localTimestampIsNeitherEncodedNorInheritedByAReusedPacket() {
        RedirectPacket packet = new RedirectPacket();
        packet.sessionId = UUID.randomUUID();
        packet.mcpeBuffer = new byte[]{1, 2};
        packet.encode();
        byte[] original = packet.getBuffer().clone();
        packet.receivedNanos = 123456;
        EmbeddedChannel channel = new EmbeddedChannel();
        packet.receivedChannel = channel;
        packet.encode();
        assertArrayEquals(original, packet.getBuffer());
        packet.clean();
        assertEquals(0, packet.receivedNanos);
        assertNull(packet.receivedChannel);
        channel.finishAndReleaseAll();
    }

}
