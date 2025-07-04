package org.itxtech.synapseapi.multiprotocol.protocol16.protocol;

import cn.nukkit.network.protocol.ProtocolInfo;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import lombok.ToString;
import org.itxtech.synapseapi.network.protocol.mod.AnimationEmotePacket;
import org.itxtech.synapseapi.network.protocol.mod.StoreBuySuccessPacket;
import org.itxtech.synapseapi.network.protocol.mod.SubPacket;
import org.itxtech.synapseapi.network.protocol.mod.SubPacketHandler;
import org.msgpack.core.MessageBufferPacker;
import org.msgpack.core.MessagePack;
import org.msgpack.core.MessageUnpacker;
import org.msgpack.value.Value;

import javax.annotation.Nullable;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * author: MagicDroidX
 * Nukkit Project
 */
@ToString
public class NEPyRpcPacket16 extends Packet16 {
    private static final Gson GSON = new Gson();

    public static final int NETWORK_ID = ProtocolInfo.PACKET_PY_RPC;

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
        try (MessageUnpacker unpacker = MessagePack.newDefaultUnpacker(this.getByteArray())) {
            if (unpacker.hasNext()) {
                data = unpacker.unpackValue();
            }
        } catch (IOException e) {
            throw new RuntimeException("MsgPack decode failed: " + e.getMessage(), e);
        }
        msgId = this.getLInt();

        try {
            decodeContent();
        } catch (Exception ignored) {
        }
    }

    private void decodeContent() {
        if (data.isMapValue()) {
            String json = data.toJson();
            JsonObject obj = GSON.fromJson(json, JsonObject.class);
            if (obj.has("value") && obj.get("value").isJsonArray()) {
                JsonArray value0 = obj.get("value").getAsJsonArray();
                if ("ModEventC2S".equals(value0.get(0).getAsString()) && value0.get(1).isJsonObject()) {
                    JsonObject obj1 = value0.get(1).getAsJsonObject();
                    if (obj1.has("value") && obj1.get("value").isJsonArray()) {
                        JsonArray value1 = obj1.get("value").getAsJsonArray();
                        String modName = value1.get(0).getAsString();
                        String systemName = value1.get(1).getAsString();
                        String eventName = value1.get(2).getAsString();
                        JsonObject eventData = value1.get(3).getAsJsonObject();
                        SubPacket<? extends SubPacketHandler<?>> subPacket = decodeSubPacket(modName, systemName, eventName, eventData);
                        if (subPacket == null) {
                            for (SubPacketDeserializer deserializer : DESERIALIZER) {
                                subPacket = deserializer.deserialize(modName, systemName, eventName, eventData);
                                if (subPacket != null) {
                                    break;
                                }
                            }
                            if (subPacket == null) {
                                return;
                            }
                        }
                        subPackets = List.of(subPacket);
                    }
                } else if ("StoreBuySuccServerEvent".equals(value0.get(0).getAsString())) {
                    StoreBuySuccessPacket subPacket = new StoreBuySuccessPacket();
                    subPackets = List.of(subPacket);
                }
            }
        } else if (data.isArrayValue()) {
            String json = data.toJson();
            JsonArray array = GSON.fromJson(json, JsonArray.class);
            if (!array.isEmpty() && array.get(0).isJsonPrimitive()) {
                String type = array.get(0).getAsString();
                if ("ModEventC2S".equals(type) && array.size() >= 2 && array.get(1).isJsonArray()) {
                    JsonArray value0 = array.get(1).getAsJsonArray();
                    String modName = value0.get(0).getAsString();
                    String systemName = value0.get(1).getAsString();
                    String eventName = value0.get(2).getAsString();
                    JsonObject eventData = value0.get(3).getAsJsonObject();
                    SubPacket<? extends SubPacketHandler<?>> subPacket = decodeSubPacket(modName, systemName, eventName, eventData);
                    if (subPacket == null) {
                        for (SubPacketDeserializer deserializer : DESERIALIZER) {
                            subPacket = deserializer.deserialize(modName, systemName, eventName, eventData);
                            if (subPacket != null) {
                                break;
                            }
                        }
                        if (subPacket == null) {
                            return;
                        }
                    }
                    subPackets = List.of(subPacket);
                } else if ("StoreBuySuccServerEvent".equals(type)) {
                    StoreBuySuccessPacket subPacket = new StoreBuySuccessPacket();
                    subPackets = List.of(subPacket);
                }
            }
        }
    }

    @Nullable
    private static SubPacket<? extends SubPacketHandler<?>> decodeSubPacket(String modName, String systemName, String eventName, JsonObject eventData) {
        switch (modName) {
            case "Minecraft": {
                switch (systemName) {
                    case "emote": {
                        switch (eventName) {
                            case "PlayEmoteEvent": {
                                return new AnimationEmotePacket(
                                        eventData.get("animName").getAsString()
                                );
                            }
                        }
                        break;
                    }
                }
                break;
            }
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
        SubPacket<? extends SubPacketHandler<?>> deserialize(String modName, String systemName, String eventName, JsonObject eventData);
    }
}
