package org.itxtech.synapseapi.java;

import java.nio.ByteBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Base64;
import java.util.Optional;
import java.util.regex.Pattern;

/**
 * ViaBedrock 双向 Java 自定义载荷桥的 ScriptMessage 信封编解码器。
 */
public final class JavaCustomPayloadEnvelope {

    public static final String SCRIPT_MESSAGE_ID = "easecation:java_custom_payload_v1";
    public static final int MAX_CHANNEL_BYTES = 128;
    public static final int MAX_PAYLOAD_BYTES = 8 * 1024;
    private static final Pattern CHANNEL_PATTERN = Pattern.compile("[a-z0-9_.-]+:[a-z0-9/._-]+");

    private JavaCustomPayloadEnvelope() {
    }

    public static Optional<Payload> decode(final String messageId, final String encodedValue) {
        if (!SCRIPT_MESSAGE_ID.equals(messageId)) {
            return Optional.empty();
        }
        if (!isUnpaddedBase64Url(encodedValue)) {
            return Optional.empty();
        }
        try {
            final byte[] envelope = Base64.getUrlDecoder().decode(encodedValue);
            if (envelope.length < 2 || envelope.length > 1 + MAX_CHANNEL_BYTES + MAX_PAYLOAD_BYTES) {
                return Optional.empty();
            }
            final int channelLength = Byte.toUnsignedInt(envelope[0]);
            if (channelLength == 0 || channelLength > MAX_CHANNEL_BYTES || envelope.length < 1 + channelLength) {
                return Optional.empty();
            }
            final String channel = StandardCharsets.UTF_8.newDecoder()
                    .onMalformedInput(CodingErrorAction.REPORT)
                    .onUnmappableCharacter(CodingErrorAction.REPORT)
                    .decode(ByteBuffer.wrap(envelope, 1, channelLength))
                    .toString();
            final byte[] payload = Arrays.copyOfRange(envelope, 1 + channelLength, envelope.length);
            if (payload.length > MAX_PAYLOAD_BYTES) {
                return Optional.empty();
            }
            return Optional.of(new Payload(channel, payload));
        } catch (IllegalArgumentException | CharacterCodingException ignored) {
            return Optional.empty();
        }
    }

    public static Optional<String> encode(final String channel, final byte[] payload) {
        if (channel == null || payload == null || !CHANNEL_PATTERN.matcher(channel).matches()) {
            return Optional.empty();
        }
        final byte[] channelBytes = channel.getBytes(StandardCharsets.UTF_8);
        if (channelBytes.length == 0 || channelBytes.length > MAX_CHANNEL_BYTES || payload.length > MAX_PAYLOAD_BYTES) {
            return Optional.empty();
        }
        final byte[] envelope = new byte[1 + channelBytes.length + payload.length];
        envelope[0] = (byte) channelBytes.length;
        System.arraycopy(channelBytes, 0, envelope, 1, channelBytes.length);
        System.arraycopy(payload, 0, envelope, 1 + channelBytes.length, payload.length);
        return Optional.of(Base64.getUrlEncoder().withoutPadding().encodeToString(envelope));
    }

    private static boolean isUnpaddedBase64Url(String value) {
        if (value == null || value.isEmpty()) {
            return false;
        }
        for (int index = 0; index < value.length(); index++) {
            char character = value.charAt(index);
            if (!(character >= 'A' && character <= 'Z')
                    && !(character >= 'a' && character <= 'z')
                    && !(character >= '0' && character <= '9')
                    && character != '-' && character != '_') {
                return false;
            }
        }
        return true;
    }

    public record Payload(String channel, byte[] payload) {
        public Payload {
            payload = payload.clone();
        }

        @Override
        public byte[] payload() {
            return payload.clone();
        }
    }
}
