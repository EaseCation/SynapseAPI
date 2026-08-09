package org.itxtech.synapseapi.messaging.java;

import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JavaCustomPayloadEnvelopeTest {

    @Test
    void decodesOnlyTheFixedBridgeEnvelopeAndDefensivelyCopiesPayload() {
        byte[] channel = "minecraft:storemod".getBytes(StandardCharsets.UTF_8);
        byte[] payload = new byte[]{0, 1, -1};
        byte[] envelope = new byte[1 + channel.length + payload.length];
        envelope[0] = (byte) channel.length;
        System.arraycopy(channel, 0, envelope, 1, channel.length);
        System.arraycopy(payload, 0, envelope, 1 + channel.length, payload.length);

        JavaCustomPayloadEnvelope.Payload decoded = JavaCustomPayloadEnvelope.decode(
                JavaCustomPayloadEnvelope.SCRIPT_MESSAGE_ID,
                Base64.getUrlEncoder().withoutPadding().encodeToString(envelope)).orElseThrow();
        byte[] firstCopy = decoded.payload();
        firstCopy[0] = 99;

        assertEquals("minecraft:storemod", decoded.channel());
        assertArrayEquals(payload, decoded.payload());
        assertFalse(JavaCustomPayloadEnvelope.decode("other:message", "AA").isPresent());
    }

    @Test
    void encodesTheSameEnvelopeThatTheDecoderAccepts() {
        byte[] payload = new byte[]{2, 1, 0, -1};

        String encoded = JavaCustomPayloadEnvelope.encode("easecation:launcher_commerce", payload).orElseThrow();
        JavaCustomPayloadEnvelope.Payload decoded = JavaCustomPayloadEnvelope.decode(
                JavaCustomPayloadEnvelope.SCRIPT_MESSAGE_ID, encoded).orElseThrow();

        assertEquals("easecation:launcher_commerce", decoded.channel());
        assertArrayEquals(payload, decoded.payload());
        assertTrue(JavaCustomPayloadEnvelope.encode("invalid channel", payload).isEmpty());
    }

    @Test
    void rejectsMalformedOrOverLimitEnvelopes() {
        byte[] malformedUtf8 = new byte[]{1, -1};
        byte[] oversized = new byte[1 + 1 + JavaCustomPayloadEnvelope.MAX_PAYLOAD_BYTES + 1];
        oversized[0] = 1;
        oversized[1] = 'x';

        assertTrue(JavaCustomPayloadEnvelope.decode(JavaCustomPayloadEnvelope.SCRIPT_MESSAGE_ID,
                Base64.getUrlEncoder().withoutPadding().encodeToString(malformedUtf8)).isEmpty());
        assertTrue(JavaCustomPayloadEnvelope.decode(JavaCustomPayloadEnvelope.SCRIPT_MESSAGE_ID,
                Base64.getUrlEncoder().withoutPadding().encodeToString(oversized)).isEmpty());
        assertTrue(JavaCustomPayloadEnvelope.decode(JavaCustomPayloadEnvelope.SCRIPT_MESSAGE_ID,
                "AA==").isEmpty());
    }
}
