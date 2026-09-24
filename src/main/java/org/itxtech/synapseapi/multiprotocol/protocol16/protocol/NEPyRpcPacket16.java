package org.itxtech.synapseapi.multiprotocol.protocol16.protocol;

import cn.nukkit.network.protocol.ProtocolInfo;
import com.google.common.util.concurrent.RateLimiter;
import lombok.ToString;
import lombok.extern.log4j.Log4j2;
import org.itxtech.synapseapi.network.protocol.mod.AnimationEmotePacket;
import org.itxtech.synapseapi.network.protocol.mod.StoreBuySuccessPacket;
import org.itxtech.synapseapi.network.protocol.mod.SubPacket;
import org.itxtech.synapseapi.network.protocol.mod.SubPacketHandler;
import org.itxtech.synapseapi.utils.BoundedMessagePackUnpacker;
import org.itxtech.synapseapi.utils.MessagePackValueUtil;
import org.itxtech.synapseapi.utils.MessagePackLimitException;
import org.msgpack.core.MessageBufferPacker;
import org.msgpack.core.MessagePack;
import org.msgpack.value.ArrayValue;
import org.msgpack.value.MapValue;
import org.msgpack.value.Value;
import org.msgpack.value.ValueFactory;

import javax.annotation.Nullable;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;

/**
 * author: MagicDroidX
 * Nukkit Project
 */
@ToString
@Log4j2
public class NEPyRpcPacket16 extends Packet16 {

    public static final int NETWORK_ID = ProtocolInfo.PACKET_PY_RPC;

    public static final int MAX_PAYLOAD_BYTES = 1024 * 1024;
    public static final int MAX_RAW_BYTES = 256 * 1024;
    private static final BoundedMessagePackUnpacker.Limits MESSAGE_PACK_LIMITS = new BoundedMessagePackUnpacker.Limits(
            MAX_PAYLOAD_BYTES,
            32,
            4096,
            4096,
            MAX_RAW_BYTES,
            256,
            16 * 1024);

    // 全局限频，避免客户端重复上报错误造成日志和异常上报风暴。
    private static final RateLimiter DISCARD_LOG_LIMITER = RateLimiter.create(0.1);
    private static final AtomicLong DISCARDED_EVENTS = new AtomicLong();

    public Value data;
    public int msgId = 9753608;

    public List<SubPacket<? extends SubPacketHandler<?>>> subPackets = List.of();
    public boolean encrypt;

    private static final Set<SubPacketDeserializer> DESERIALIZER = new HashSet<>();

    public static void addDeserializer(SubPacketDeserializer deserializer) {
        Objects.requireNonNull(deserializer, "deserializer");
        DESERIALIZER.add(deserializer);
    }

    @Override
    public int pid() {
        return NETWORK_ID;
    }

    @Override
    public void decode() {
        data = ValueFactory.newNil();
        subPackets = List.of();
        long length = getUnsignedVarInt();
        // 即使业务数据超限，也必须先验证外层长度和消息编号完整，才能安全跳过。
        if (length < 0 || length > (long) getCount() - getOffset() - Integer.BYTES) {
            throw new IllegalArgumentException("PyRpc payload or message ID is truncated");
        }
        if (length > MAX_PAYLOAD_BYTES) {
            skip((int) length);
            msgId = getLInt();
            reportDiscard("payload byte limit", null);
            return;
        }
        byte[] payload = get((int) length);
        msgId = getLInt();
        try {
            data = BoundedMessagePackUnpacker.unpack(payload, MESSAGE_PACK_LIMITS);
        } catch (MessagePackLimitException e) {
            reportDiscard(e.getMessage(), null);
            return;
        } catch (IOException e) {
            throw new IllegalArgumentException("MessagePack decode failed: " + e.getMessage(), e);
        }

        try {
            decodeContent();
        } catch (RuntimeException e) {
            // 业务字段或插件解析失败只丢弃当前事件，不影响同批移动、交互等游戏包。
            subPackets = List.of();
            reportDiscard("application decode failure", e);
        }
    }

    private static void reportDiscard(String reason, @Nullable RuntimeException exception) {
        DISCARDED_EVENTS.incrementAndGet();
        if (!DISCARD_LOG_LIMITER.tryAcquire()) {
            return;
        }
        long count = DISCARDED_EVENTS.getAndSet(0);
        if (exception == null) {
            log.warn("Discarded {} PyRpc events since the last report; reason={}", count, reason);
        } else {
            log.warn("Discarded {} PyRpc events since the last report; reason={}", count, reason, exception);
        }
    }

    private void decodeContent() {
        ArrayValue root = getRootArray();
        if (root == null || root.size() == 0) {
            return;
        }

        Value typeValue = root.get(0);
        if (!typeValue.isStringValue() && !typeValue.isBinaryValue()) {
            return;
        }

        String type = MessagePackValueUtil.asString(typeValue, "PyRpc type");
        if ("ModEventC2S".equals(type)) {
            decodeModEvent(root);
        } else if ("StoreBuySuccServerEvent".equals(type)) {
            subPackets = List.of(new StoreBuySuccessPacket());
        }
    }

    @Nullable
    private ArrayValue getRootArray() {
        if (data.isArrayValue()) {
            return data.asArrayValue();
        }
        if (!data.isMapValue()) {
            return null;
        }

        Value wrapped = MessagePackValueUtil.getOptional(data.asMapValue(), "value");
        if (wrapped == null) {
            return null;
        }
        return MessagePackValueUtil.asArray(wrapped, "PyRpc value");
    }

    private void decodeModEvent(ArrayValue root) {
        if (root.size() < 2) {
            throw new IllegalArgumentException("ModEventC2S payload is missing");
        }

        ArrayValue event = unwrapArray(root.get(1), "ModEventC2S payload");
        if (event.size() < 4) {
            throw new IllegalArgumentException("ModEventC2S payload must contain four values");
        }

        String modName = MessagePackValueUtil.asString(event.get(0), "modName");
        String systemName = MessagePackValueUtil.asString(event.get(1), "systemName");
        String eventName = MessagePackValueUtil.asString(event.get(2), "eventName");
        if (!(event.get(3) instanceof MapValue eventData)) {
            return;
        }

        SubPacket<? extends SubPacketHandler<?>> subPacket = decodeSubPacket(
                modName,
                systemName,
                eventName,
                eventData
        );
        if (subPacket == null) {
            for (SubPacketDeserializer deserializer : DESERIALIZER) {
                subPacket = deserializer.deserialize(modName, systemName, eventName, eventData);
                if (subPacket != null) {
                    break;
                }
            }
        }
        if (subPacket != null) {
            subPackets = List.of(subPacket);
        }
    }

    private static ArrayValue unwrapArray(Value value, String name) {
        if (value.isArrayValue()) {
            return value.asArrayValue();
        }
        MapValue wrapper = MessagePackValueUtil.asMap(value, name);
        return MessagePackValueUtil.getArray(wrapper, "value");
    }

    @Nullable
    private static SubPacket<? extends SubPacketHandler<?>> decodeSubPacket(
            String modName,
            String systemName,
            String eventName,
            MapValue eventData
    ) {
        if ("Minecraft".equals(modName)
                && "emote".equals(systemName)
                && "PlayEmoteEvent".equals(eventName)) {
            return new AnimationEmotePacket(MessagePackValueUtil.getString(eventData, "animName"));
        }
        return null;
    }

    @Override
    public void encode() {
        this.reset();
        try (MessageBufferPacker packer = MessagePack.newDefaultBufferPacker()) {
            if (encrypt) {
                packer.packValue(subPackets.getFirst().pack());
            } else {
                packer.packValue(data);
            }
            this.putByteArray(packer.toByteArray());
        } catch (IOException e) {
            throw new RuntimeException("MsgPack encode failed: " + e.getMessage(), e);
        }
        this.putLInt(msgId);
    }

    @FunctionalInterface
    public interface SubPacketDeserializer {
        @Nullable
        SubPacket<? extends SubPacketHandler<?>> deserialize(
                String modName,
                String systemName,
                String eventName,
                MapValue eventData
        );
    }
}
