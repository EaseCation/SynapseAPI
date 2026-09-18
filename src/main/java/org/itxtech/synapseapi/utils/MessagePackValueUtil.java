package org.itxtech.synapseapi.utils;

import org.msgpack.value.ArrayValue;
import org.msgpack.value.IntegerValue;
import org.msgpack.value.MapValue;
import org.msgpack.value.Value;

import javax.annotation.Nullable;
import java.nio.ByteBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Objects;

/**
 * Provides strictly typed access to the MessagePack tree for PyRpc.
 */
public final class MessagePackValueUtil {

    public static Value getRequired(MapValue map, String key) {
        Value value = getOptional(map, key);
        if (value == null) {
            throw new IllegalArgumentException("Missing MessagePack field: " + key);
        }
        return value;
    }

    @Nullable
    public static Value getOptional(MapValue map, String key) {
        Objects.requireNonNull(map, "map");
        Objects.requireNonNull(key, "key");
        for (Map.Entry<Value, Value> entry : map.entrySet()) {
            Value mapKey = entry.getKey();
            if (mapKey.isRawValue() && key.equals(asString(mapKey, "map key"))) {
                return entry.getValue();
            }
        }
        return null;
    }

    public static boolean has(MapValue map, String key) {
        return getOptional(map, key) != null;
    }

    public static String getString(MapValue map, String key) {
        return asString(getRequired(map, key), key);
    }

    public static int getInt(MapValue map, String key) {
        return asInt(getRequired(map, key), key);
    }

    public static long getLong(MapValue map, String key) {
        return asLong(getRequired(map, key), key);
    }

    public static float getFloat(MapValue map, String key) {
        return asFloat(getRequired(map, key), key);
    }

    public static double getDouble(MapValue map, String key) {
        return asDouble(getRequired(map, key), key);
    }

    public static boolean getBoolean(MapValue map, String key) {
        return asBoolean(getRequired(map, key), key);
    }

    public static MapValue getMap(MapValue map, String key) {
        return asMap(getRequired(map, key), key);
    }

    public static ArrayValue getArray(MapValue map, String key) {
        return asArray(getRequired(map, key), key);
    }

    public static String asString(Value value, String name) {
        Objects.requireNonNull(value, "value");
        if (!value.isStringValue() && !value.isBinaryValue()) {
            throw typeError(name, "string or UTF-8 binary", value);
        }

        try {
            return StandardCharsets.UTF_8.newDecoder()
                    .onMalformedInput(CodingErrorAction.REPORT)
                    .onUnmappableCharacter(CodingErrorAction.REPORT)
                    .decode(ByteBuffer.wrap(value.asRawValue().asByteArray()))
                    .toString();
        } catch (CharacterCodingException e) {
            throw new IllegalArgumentException("MessagePack value " + name + " is not valid UTF-8", e);
        }
    }

    public static String asMapKeyString(Value value, String name) {
        Objects.requireNonNull(value, "value");
        if (value.isStringValue() || value.isBinaryValue()) {
            return asString(value, name);
        }
        if (value.isIntegerValue()) {
            return value.asIntegerValue().asBigInteger().toString();
        }
        throw typeError(name, "string, UTF-8 binary, or integer map key", value);
    }

    public static int asInt(Value value, String name) {
        IntegerValue integer = requireInteger(value, name);
        if (!integer.isInIntRange()) {
            throw new IllegalArgumentException("MessagePack integer " + name + " is outside the int range");
        }
        return integer.asInt();
    }

    public static long asLong(Value value, String name) {
        IntegerValue integer = requireInteger(value, name);
        if (!integer.isInLongRange()) {
            throw new IllegalArgumentException("MessagePack integer " + name + " is outside the long range");
        }
        return integer.asLong();
    }

    public static float asFloat(Value value, String name) {
        Objects.requireNonNull(value, "value");
        if (!value.isNumberValue()) {
            throw typeError(name, "number", value);
        }

        float result = value.asNumberValue().toFloat();
        if (!Float.isFinite(result)) {
            throw new IllegalArgumentException("MessagePack number " + name + " is outside the finite float range");
        }
        return result;
    }

    public static double asDouble(Value value, String name) {
        Objects.requireNonNull(value, "value");
        if (!value.isNumberValue()) {
            throw typeError(name, "number", value);
        }

        double result = value.asNumberValue().toDouble();
        if (!Double.isFinite(result)) {
            throw new IllegalArgumentException("MessagePack number " + name + " must be finite");
        }
        return result;
    }

    public static boolean asBoolean(Value value, String name) {
        Objects.requireNonNull(value, "value");
        if (!value.isBooleanValue()) {
            throw typeError(name, "boolean", value);
        }
        return value.asBooleanValue().getBoolean();
    }

    public static MapValue asMap(Value value, String name) {
        Objects.requireNonNull(value, "value");
        if (!value.isMapValue()) {
            throw typeError(name, "map", value);
        }
        return value.asMapValue();
    }

    public static ArrayValue asArray(Value value, String name) {
        Objects.requireNonNull(value, "value");
        if (!value.isArrayValue()) {
            throw typeError(name, "array", value);
        }
        return value.asArrayValue();
    }

    private static IntegerValue requireInteger(Value value, String name) {
        Objects.requireNonNull(value, "value");
        if (!value.isIntegerValue()) {
            throw typeError(name, "integer", value);
        }
        return value.asIntegerValue();
    }

    private static IllegalArgumentException typeError(String name, String expected, Value actual) {
        return new IllegalArgumentException("MessagePack value " + name + " must be a " + expected + ", got " + actual.getValueType());
    }

    private MessagePackValueUtil() {
        throw new IllegalStateException();
    }
}
