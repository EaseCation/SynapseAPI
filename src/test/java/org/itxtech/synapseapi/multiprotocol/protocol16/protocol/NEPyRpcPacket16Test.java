package org.itxtech.synapseapi.multiprotocol.protocol16.protocol;

import cn.nukkit.utils.BinaryStream;
import org.itxtech.synapseapi.network.protocol.mod.AnimationEmotePacket;
import org.itxtech.synapseapi.network.protocol.mod.StoreBuySuccessPacket;
import org.itxtech.synapseapi.utils.MessagePackValueUtil;
import org.junit.jupiter.api.Test;
import org.msgpack.core.MessageBufferPacker;
import org.msgpack.core.MessagePack;
import org.msgpack.value.Value;
import org.msgpack.value.ValueFactory;

import java.io.IOException;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;
import static org.msgpack.value.ValueFactory.*;

class NEPyRpcPacket16Test {
    @Test
    void dispatchesBothLegacyWrappedAndModernArrayEnvelopes() throws IOException {
        for (boolean binary : new boolean[]{false, true}) {
            Value event = newArray(text("Minecraft", binary), text("emote", binary),
                    text("PlayEmoteEvent", binary), newMap(text("animName", binary), text("wave", binary)));
            Value modern = newArray(text("ModEventC2S", binary), event, newNil());
            Value legacy = newMap(text("value", binary), newArray(text("ModEventC2S", binary),
                    newMap(text("value", binary), event), newNil()));
            for (Value envelope : new Value[]{modern, legacy}) {
                NEPyRpcPacket16 packet = decode(pack(envelope));
                assertEquals(9753608, packet.msgId);
                assertEquals(1, packet.subPackets.size());
                assertInstanceOf(AnimationEmotePacket.class, packet.subPackets.getFirst());
            }
        }
    }

    @Test
    void preservesPurchaseNotificationAndIgnoresUnknownEvent() throws IOException {
        assertInstanceOf(StoreBuySuccessPacket.class,
                decode(pack(newArray(newString("StoreBuySuccServerEvent")))).subPackets.getFirst());
        assertTrue(decode(pack(newArray(newString("ModEventC2S"), newArray(newString("unknown_mod"),
                newString("client"), newString("unknown_event"), newMap(new Value[0]))))).subPackets.isEmpty());
    }

    @Test
    void preservesUnsignedEntityIdsAndUtf8WithoutJsonRoundTrip() throws IOException {
        BigInteger entityId = new BigInteger("18446744073709551615");
        Value data = newMap(newInteger(entityId), newArray(newFloat(1.25), newFloat(64.5), newFloat(-2.75)),
                newString("name"), text("玩家😀", true));
        NEPyRpcPacket16 packet = decode(pack(data));
        assertEquals(entityId, packet.data.asMapValue().getKeyValueArray()[0].asIntegerValue().asBigInteger());
        assertEquals("玩家😀", MessagePackValueUtil.getString(packet.data.asMapValue(), "name"));
    }

    @Test
    void rejectsDeclaredSizesBeforeLargeAllocation() throws IOException {
        try (MessageBufferPacker packer = MessagePack.newDefaultBufferPacker()) {
            packer.packArrayHeader(Integer.MAX_VALUE);
            assertDiscarded(decode(packer.toByteArray()));
        }
        try (MessageBufferPacker packer = MessagePack.newDefaultBufferPacker()) {
            packer.packBinaryHeader(256 * 1024 + 1);
            assertDiscarded(decode(packer.toByteArray()));
        }
        BinaryStream wire = new BinaryStream();
        wire.putUnsignedVarInt(1024 * 1024 + 1);
        NEPyRpcPacket16 packet = new NEPyRpcPacket16();
        packet.setBuffer(wire.getBuffer());
        assertThrows(IllegalArgumentException.class, packet::decode);
    }

    @Test
    void rejectsTruncationTrailingValuesAndNonFiniteNumbers() throws IOException {
        byte[] payload = pack(newArray(newString("StoreBuySuccServerEvent")));
        BinaryStream wire = new BinaryStream();
        wire.putByteArray(payload);
        NEPyRpcPacket16 packet = new NEPyRpcPacket16();
        packet.setBuffer(wire.getBuffer());
        assertThrows(IllegalArgumentException.class, packet::decode);
        assertThrows(IllegalArgumentException.class, () -> decode(new byte[]{(byte) 0xc0, (byte) 0xc0}));
        assertThrows(IllegalArgumentException.class, () -> decode(pack(newFloat(Double.NaN))));
    }

    @Test
    void rejectsAmbiguousMapKeysAndExcessiveNesting() throws IOException {
        Value duplicate = newMap(newString("key"), newInteger(1), text("key", true), newInteger(2));
        assertThrows(IllegalArgumentException.class, () -> decode(pack(duplicate)));
        Value nested = newNil();
        for (int i = 0; i < 34; i++) {
            nested = newArray(nested);
        }
        byte[] payload = pack(nested);
        assertDiscarded(decode(payload));
    }

    @Test
    void rawAndEnvelopeBudgetsDiscardOnlyTheRpc() throws IOException {
        NEPyRpcPacket16 atLimit = decode(pack(newBinary(new byte[NEPyRpcPacket16.MAX_RAW_BYTES])));
        assertTrue(atLimit.data.isBinaryValue());
        assertDiscarded(decode(pack(newBinary(new byte[NEPyRpcPacket16.MAX_RAW_BYTES + 1]))));
        assertDiscarded(decode(new byte[NEPyRpcPacket16.MAX_PAYLOAD_BYTES + 1]));
    }

    @Test
    void semanticFailureAndThrowingPluginDoNotEscapeDecode() throws IOException {
        NEPyRpcPacket16 missingField = decode(pack(newArray(newString("ModEventC2S"), newArray(
                newString("Minecraft"), newString("emote"), newString("PlayEmoteEvent"), newMap(new Value[0])))));
        assertTrue(missingField.subPackets.isEmpty());
        assertTrue(missingField.feof());
        NEPyRpcPacket16.addDeserializer((mod, system, event, data) -> {
            if (mod.equals("failing_review_plugin")) throw new IllegalStateException("Plugin decode failure");
            return null;
        });
        NEPyRpcPacket16 failedPlugin = decode(pack(newArray(newString("ModEventC2S"), newArray(
                newString("failing_review_plugin"), newString("client"), newString("event"), newMap(new Value[0])))));
        assertTrue(failedPlugin.subPackets.isEmpty());
        assertTrue(failedPlugin.feof());
    }

    @Test
    void indexedLookupPreservesRawKeyTypesAndOriginalEncoding() throws IOException {
        Value original = newMap(text("binary", true), text("value", true), newString("text"), newInteger(2),
                newInteger(3), newString("integer key"));
        NEPyRpcPacket16 packet = decode(pack(original));
        assertEquals("value", MessagePackValueUtil.getString(packet.data.asMapValue(), "binary"));
        assertEquals(2, MessagePackValueUtil.getInt(packet.data.asMapValue(), "text"));
        assertNull(MessagePackValueUtil.getOptional(packet.data.asMapValue(), "3"));
        assertNull(MessagePackValueUtil.getOptional(packet.data.asMapValue(), "missing"));
        assertTrue(packet.data.asMapValue().getKeyValueArray()[0].isBinaryValue());
        assertArrayEquals(pack(original), pack(packet.data));
        assertSame(MessagePackValueUtil.getString(packet.data.asMapValue(), "binary"),
                MessagePackValueUtil.getString(packet.data.asMapValue(), "binary"));
    }

    @Test
    void strictUtf8ValidationSurvivesCaching() throws IOException {
        Value malformedText = newBinary(new byte[]{(byte) 0xff});
        // 即使 toString 已经生成容错缓存，严格字符串访问仍不能接受非法 UTF-8。
        malformedText.toString();
        assertThrows(IllegalArgumentException.class, () -> MessagePackValueUtil.asString(malformedText, "field"));
        assertThrows(IllegalArgumentException.class, () -> decode(pack(newMap(malformedText, newInteger(1)))));
    }

    @Test
    void truncatedValuesWithinBudgetAreStillMalformed() throws IOException {
        try (MessageBufferPacker packer = MessagePack.newDefaultBufferPacker()) {
            packer.packBinaryHeader(64 * 1024);
            assertThrows(IllegalArgumentException.class, () -> decode(packer.toByteArray()));
        }
        try (MessageBufferPacker packer = MessagePack.newDefaultBufferPacker()) {
            packer.packMapHeader(4096);
            assertThrows(IllegalArgumentException.class, () -> decode(packer.toByteArray()));
        }
    }

    private static void assertDiscarded(NEPyRpcPacket16 packet) {
        assertTrue(packet.subPackets.isEmpty());
        assertTrue(packet.data.isNilValue());
        assertTrue(packet.feof());
        assertEquals(9753608, packet.msgId);
    }

    private static Value text(String text, boolean binary) {
        return binary ? ValueFactory.newBinary(text.getBytes(StandardCharsets.UTF_8)) : newString(text);
    }

    private static byte[] pack(Value value) throws IOException {
        try (MessageBufferPacker packer = MessagePack.newDefaultBufferPacker()) {
            packer.packValue(value);
            return packer.toByteArray();
        }
    }

    private static NEPyRpcPacket16 decode(byte[] payload) {
        BinaryStream wire = new BinaryStream();
        wire.putByteArray(payload);
        wire.putLInt(9753608);
        NEPyRpcPacket16 packet = new NEPyRpcPacket16();
        packet.setBuffer(wire.getBuffer());
        packet.decode();
        return packet;
    }
}
