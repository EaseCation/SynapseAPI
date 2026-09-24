package org.itxtech.synapseapi.utils;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import org.msgpack.core.MessageFormat;
import org.msgpack.core.MessagePack;
import org.msgpack.core.MessageUnpacker;
import org.msgpack.core.MessageStringCodingException;
import org.msgpack.value.ImmutableRawValue;
import org.msgpack.value.ImmutableValue;
import org.msgpack.value.Value;
import org.msgpack.value.ValueFactory;
import org.msgpack.value.ValueType;

import java.io.IOException;
import java.math.BigInteger;
import java.util.Objects;

/**
 * 分配容器和数据之前先校验预算，并复用文本校验与字段索引。
 */
public final class BoundedMessagePackUnpacker {

    private final MessageUnpacker unpacker;
    private final Limits limits;
    private final int payloadBytes;
    private int nodes;

    private BoundedMessagePackUnpacker(MessageUnpacker unpacker, Limits limits, int payloadBytes) {
        this.unpacker = unpacker;
        this.limits = limits;
        this.payloadBytes = payloadBytes;
    }

    public static Value unpack(byte[] payload, Limits limits) throws IOException {
        Objects.requireNonNull(payload, "payload");
        Objects.requireNonNull(limits, "limits");
        if (payload.length > limits.maxPayloadBytes()) {
            throw new MessagePackLimitException("MessagePack payload exceeds the byte limit");
        }

        try (MessageUnpacker unpacker = MessagePack.newDefaultUnpacker(payload)) {
            if (!unpacker.hasNext()) {
                throw new IOException("MessagePack payload is empty");
            }

            BoundedMessagePackUnpacker reader = new BoundedMessagePackUnpacker(unpacker, limits, payload.length);
            Value value = reader.readValue(0);
            if (unpacker.hasNext()) {
                throw new IOException("MessagePack payload contains trailing values");
            }
            return value;
        }
    }

    private ImmutableValue readValue(int depth) throws IOException {
        checkDepth(depth);
        consumeNode();

        MessageFormat format = unpacker.getNextFormat();
        ValueType type = format.getValueType();
        return switch (type) {
            case NIL -> readNil();
            case BOOLEAN -> ValueFactory.newBoolean(unpacker.unpackBoolean());
            case INTEGER -> ValueFactory.newInteger(unpacker.unpackBigInteger());
            case FLOAT -> readFloat(format);
            case STRING -> readString(limits.maxRawBytes(), "MessagePack string");
            case BINARY -> readBinary(limits.maxRawBytes(), "MessagePack binary value");
            case ARRAY -> readArray(depth);
            case MAP -> readMap(depth);
            case EXTENSION -> throw new IOException("MessagePack extension values are not allowed");
        };
    }

    private ImmutableValue readNil() throws IOException {
        unpacker.unpackNil();
        return ValueFactory.newNil();
    }

    private ImmutableValue readFloat(MessageFormat format) throws IOException {
        if (format == MessageFormat.FLOAT32) {
            float value = unpacker.unpackFloat();
            if (!Float.isFinite(value)) {
                throw new IOException("MessagePack float must be finite");
            }
            return ValueFactory.newFloat(value);
        }

        double value = unpacker.unpackDouble();
        if (!Double.isFinite(value)) {
            throw new IOException("MessagePack float must be finite");
        }
        return ValueFactory.newFloat(value);
    }

    private ImmutableValue readString(int maxBytes, String description) throws IOException {
        int length = unpacker.unpackRawStringHeader();
        byte[] bytes = readRaw(length, maxBytes, description);
        ImmutableRawValue value = ValueFactory.newString(bytes, true);
        decodeUtf8(value, description);
        return value;
    }

    private ImmutableValue readBinary(int maxBytes, String description) throws IOException {
        int length = unpacker.unpackBinaryHeader();
        byte[] bytes = readRaw(length, maxBytes, description);
        return ValueFactory.newBinary(bytes, true);
    }

    private byte[] readRaw(int length, int maxBytes, String description) throws IOException {
        if (length < 0 || length > maxBytes) {
            throw new MessagePackLimitException(description + " exceeds the byte limit");
        }
        requireAvailableBytes(length);
        return unpacker.readPayload(length);
    }

    private ImmutableValue readArray(int depth) throws IOException {
        int size = unpacker.unpackArrayHeader();
        if (size < 0 || size > limits.maxArrayElements()) {
            throw new MessagePackLimitException("MessagePack array exceeds the element limit");
        }
        ensureMinimumNodes(size);
        requireAvailableBytes(size);

        Value[] values = new Value[size];
        for (int index = 0; index < size; index++) {
            values[index] = readValue(depth + 1);
        }
        return ValueFactory.newArray(values, true);
    }

    private ImmutableValue readMap(int depth) throws IOException {
        int size = unpacker.unpackMapHeader();
        if (size < 0 || size > limits.maxMapEntries()) {
            throw new MessagePackLimitException("MessagePack map exceeds the entry limit");
        }
        ensureMinimumNodes((long) size * 2);
        requireAvailableBytes((long) size * 2);

        Value[] keyValues = new Value[size * 2];
        Object2IntMap<String> offsets = new Object2IntOpenHashMap<>(size);
        offsets.defaultReturnValue(-1);
        for (int index = 0; index < size; index++) {
            MapKey key = readMapKey(depth + 1);
            if (offsets.putIfAbsent(key.text(), index * 2) >= 0) {
                throw new IOException("MessagePack map contains a duplicate key");
            }
            keyValues[index * 2] = key.value();
            keyValues[index * 2 + 1] = readValue(depth + 1);
        }
        return new IndexedMessagePackMap(keyValues, offsets);
    }

    private MapKey readMapKey(int depth) throws IOException {
        checkDepth(depth);
        consumeNode();

        ValueType type = unpacker.getNextFormat().getValueType();
        if (type == ValueType.STRING) {
            int length = unpacker.unpackRawStringHeader();
            byte[] bytes = readRaw(length, limits.maxMapKeyBytes(), "MessagePack map key");
            ImmutableRawValue value = ValueFactory.newString(bytes, true);
            return new MapKey(value, decodeUtf8(value, "MessagePack map key"));
        }
        if (type == ValueType.BINARY) {
            int length = unpacker.unpackBinaryHeader();
            byte[] bytes = readRaw(length, limits.maxMapKeyBytes(), "MessagePack map key");
            ImmutableRawValue value = ValueFactory.newBinary(bytes, true);
            return new MapKey(value, decodeUtf8(value, "MessagePack map key"));
        }
        if (type == ValueType.INTEGER) {
            BigInteger integer = unpacker.unpackBigInteger();
            return new MapKey(ValueFactory.newInteger(integer), integer.toString());
        }

        throw new IOException("MessagePack map keys must be strings, UTF-8 binary values, or integers");
    }

    private void checkDepth(int depth) throws IOException {
        if (depth > limits.maxDepth()) {
            throw new MessagePackLimitException("MessagePack nesting exceeds the depth limit");
        }
    }

    private void consumeNode() throws IOException {
        nodes++;
        if (nodes > limits.maxNodes()) {
            throw new MessagePackLimitException("MessagePack payload exceeds the node limit");
        }
    }

    private void ensureMinimumNodes(long additionalNodes) throws IOException {
        if ((long) nodes + additionalNodes > limits.maxNodes()) {
            throw new MessagePackLimitException("MessagePack payload exceeds the node limit");
        }
    }

    private void requireAvailableBytes(long minimumBytes) throws IOException {
        // 每个节点至少占一个字节；先检查实际剩余数据，避免短报文放大内存分配。
        if (minimumBytes > payloadBytes - unpacker.getTotalReadBytes()) {
            throw new IOException("MessagePack payload is truncated");
        }
    }

    private static String decodeUtf8(ImmutableRawValue value, String description) throws IOException {
        try {
            return value.asString();
        } catch (MessageStringCodingException e) {
            throw new IOException(description + " is not valid UTF-8", e);
        }
    }

    public record Limits(
            int maxPayloadBytes,
            int maxDepth,
            int maxArrayElements,
            int maxMapEntries,
            int maxRawBytes,
            int maxMapKeyBytes,
            int maxNodes
    ) {
        public Limits {
            if (maxPayloadBytes <= 0
                    || maxDepth < 0
                    || maxArrayElements < 0
                    || maxMapEntries < 0
                    || maxRawBytes < 0
                    || maxMapKeyBytes < 0
                    || maxNodes <= 0) {
                throw new IllegalArgumentException("MessagePack limits must be non-negative and include positive payload and node limits");
            }
        }
    }

    private record MapKey(Value value, String text) {
    }
}
