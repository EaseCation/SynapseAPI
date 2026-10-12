package org.itxtech.synapseapi.network.protocol.spp;

import com.nukkitx.network.util.LatencyTrace;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.util.HexFormat;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class RedirectTraceTest {
    @TempDir
    Path directory;

    @Test
    void traceKeepsTheExistingSynapseWireFormat() {
        assertFalse(LatencyTrace.enabled());
        RedirectPacket plain = RedirectPacket.create();
        assertEquals(RedirectPacket.class, plain.getClass());
        byte[] expected = HexFormat.of().parseHex("00000000000000000100000000000000000003820003fe0102");
        assertArrayEquals(expected, encode(plain));
        LatencyTrace.Session session = LatencyTrace.start(directory, "trace", "test", 30);
        assertNotNull(session);
        try (session) {
            RedirectPacket traced = RedirectPacket.create();
            assertInstanceOf(TracedRedirectPacket.class, traced);
            assertArrayEquals(expected, encode(traced));
            ((TracedRedirectPacket) traced).traceEnqueue("enqueue", "connection");
            assertFalse(traced.getLatencyTraceKey().isEmpty());
            RedirectPacket cloned = (RedirectPacket) traced.clone();
            assertNotNull(cloned);
            assertEquals("", cloned.getLatencyTraceKey());
            assertArrayEquals(expected, cloned.getBuffer());
        }
        assertEquals(RedirectPacket.class, RedirectPacket.create().getClass());
    }

    private static byte[] encode(RedirectPacket packet) {
        packet.sessionId = new UUID(0, 1);
        packet.protocol = 898;
        packet.compressionAlgorithm = 0;
        packet.mcpeBuffer = new byte[]{(byte) 0xfe, 1, 2};
        packet.encode();
        return packet.getBuffer();
    }
}
