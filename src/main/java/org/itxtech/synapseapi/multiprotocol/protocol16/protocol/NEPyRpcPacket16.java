package org.itxtech.synapseapi.multiprotocol.protocol16.protocol;

import cn.nukkit.math.Vector3;
import cn.nukkit.network.protocol.ProtocolInfo;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import lombok.ToString;
import org.itxtech.synapseapi.SynapseAPI;
import org.itxtech.synapseapi.network.protocol.mod.*;
import org.itxtech.synapseapi.network.protocol.mod.data.ClientScreenInfo;
import org.itxtech.synapseapi.network.protocol.mod.data.ECModClientInfo;
import org.itxtech.synapseapi.network.protocol.mod.data.InteractType;
import org.itxtech.synapseapi.network.protocol.mod.data.NavPathResult;
import org.itxtech.synapseapi.utils.EncryptUtil;
import org.msgpack.core.MessageBufferPacker;
import org.msgpack.core.MessagePack;
import org.msgpack.core.MessageUnpacker;
import org.msgpack.value.*;

import javax.annotation.Nullable;
import java.io.IOException;
import java.time.Instant;
import java.util.Collections;
import java.util.List;

/**
 * author: MagicDroidX
 * Nukkit Project
 */
@ToString
public class NEPyRpcPacket16 extends Packet16 {
    private static final Gson GSON = new Gson();

    public static final int NETWORK_ID = ProtocolInfo.PACKET_PY_RPC;

    private static final byte[] UNKNOWN_BYTES_SENDING = new byte[]{8, -44, -108, 0};

    public Value data;

    public SubPacket[] subPackets = new SubPacket[0];
    public boolean encrypt;

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
//                        String systemName = value1.get(1).getAsString();
                        String eventName = value1.get(2).getAsString();
                        JsonObject eventData = value1.get(3).getAsJsonObject();
                        SubPacket subPacket = decodeSubPacket(modName, eventName, eventData);
                        if (subPacket == null) {
                            return;
                        }
                        subPackets = new SubPacket[]{subPacket};
                    }
                } else if ("StoreBuySuccServerEvent".equals(value0.get(0).getAsString())) {
                    StoreBuySuccessPacket subPacket = new StoreBuySuccessPacket();
                    subPackets = new SubPacket[]{subPacket};
                }
            }
        } else if (data.isArrayValue()) {
            String json = data.toJson();
            JsonArray array = GSON.fromJson(json, JsonArray.class);
            if (array.size() >= 1 && array.get(0).isJsonPrimitive()) {
                String type = array.get(0).getAsString();
                if ("ModEventC2S".equals(type) && array.size() >= 2 && array.get(1).isJsonArray()) {
                    JsonArray value0 = array.get(1).getAsJsonArray();
                    String modName = value0.get(0).getAsString();
//                    String systemName = value0.get(1).getAsString();
                    String eventName = value0.get(2).getAsString();
                    JsonObject eventData = value0.get(3).getAsJsonObject();
                    SubPacket subPacket = decodeSubPacket(modName, eventName, eventData);
                    if (subPacket == null) {
                        return;
                    }
                    subPackets = new SubPacket[]{subPacket};
                } else if ("StoreBuySuccServerEvent".equals(type)) {
                    StoreBuySuccessPacket subPacket = new StoreBuySuccessPacket();
                    subPackets = new SubPacket[]{subPacket};
                }
            }
        }
    }

    @Nullable
    private static SubPacket decodeSubPacket(String modName, String eventName, JsonObject eventData) {
        switch (modName) {
            case "ECNukkitClientMod":
                switch (eventName) {
                    case "RequestHudNodeDataEvent": {
                        return new RequestHudPacket();
                    }
                    case "RequestClickHudBtEvent": {
                        return new HudButtonInteractPacket(
                                eventData.get("TouchEvent").getAsInt(),
                                eventData.get("buttonName").getAsString()
                        );
                    }
                    case "RequestClickStackBtEvent": {
                        return new ScreenInteractPacket(
                                eventData.get("TouchEvent").getAsInt(),
                                eventData.get("buttonName").getAsString()
                        );
                    }
                    case "ScreenInfoEvent": {
                        ClientScreenInfo info = GSON.fromJson(eventData, ClientScreenInfo.class);
                        return new DisplayEnvironmentPacket(info);
                    }
                    case "InteractBeforeClientEvent": {
                        return new GameInteractPacket(
                                InteractType.valueOf(eventData.get("type").getAsString())
                        );
                    }
                    case "FPSInfoEvent": {
                        return new FrameStatsPacket(
                                eventData.get("req_id").getAsInt(),
                                eventData.get("fps").getAsFloat()
                        );
                    }
                    case "ResponseGetNavPathEvent": {
                        NavPathResult result;
                        if (eventData.get("path").isJsonPrimitive()) {
                            result = NavPathResult.ofError(eventData.get("path").getAsInt());
                        } else if (eventData.get("path").isJsonArray()) {
                            List<Vector3> path = new ObjectArrayList<>();
                            for (JsonElement el : eventData.get("path").getAsJsonArray()) {
                                JsonArray v3 = el.getAsJsonObject().get("value").getAsJsonArray();
                                path.add(new Vector3(v3.get(0).getAsFloat(), v3.get(1).getAsFloat(), v3.get(2).getAsFloat()));
                            }
                            result = NavPathResult.ofSuccess(path);
                        } else {
                            result = NavPathResult.ofError(-1);
                        }
                        return new PathfindingResponsePacket(
                                eventData.get("req_id").getAsInt(),
                                result
                        );
                    }
                    case "ECHudClickEvent": {
                        if (eventData.has("button")) {
                            return new HudInteractPacket(
                                    eventData.get("button").getAsString()
                            );
                        }
                        break;
                    }
                    case "PlayerInputModeChangeEvent": {
                        if (eventData.has("inputMode")) {
                            return new InputChangedPacket(
                                    eventData.get("inputMode").getAsInt()
                            );
                        }
                        break;
                    }
                    case "DimensionChangeFinishClient": {
                        Vector3 toPos;
                        if (eventData.get("toPos").isJsonArray()) {
                            JsonArray jsonArray = eventData.get("toPos").getAsJsonArray();
                            toPos = new Vector3(jsonArray.get(0).getAsDouble(), jsonArray.get(1).getAsDouble(), jsonArray.get(2).getAsDouble());
                        } else {
                            JsonArray jsonArray = eventData.get("toPos").getAsJsonObject().get("value").getAsJsonArray();
                            toPos = new Vector3(jsonArray.get(0).getAsDouble(), jsonArray.get(1).getAsDouble(), jsonArray.get(2).getAsDouble());
                        }
                        return new DimensionChangedPacket(
                                eventData.get("playerId").getAsString(),
                                eventData.get("fromDimensionId").getAsInt(),
                                eventData.get("toDimensionId").getAsInt(),
                                toPos
                        );
                    }
                    case "ModUIException": {
                        String report = eventData.get("report").getAsString();
                        SynapseAPI.getInstance().getLogger().debug("ModUIException: " + report);
                        return new ErrorReportPacket(
                                eventData.get("playerId").getAsString(),
                                eventData.get("exception").getAsString(),
                                eventData.get("data").getAsString(),
                                eventData.get("stacktrace").getAsString(),
                                report
                        );
                    }
                }
                break;
            case "Minecraft":
                switch (eventName) {
                    // ClientInfo
                    case "OnClientNodePreloadSuccess":
                        try {
                            ECModClientInfo info = GSON.fromJson(EncryptUtil.decrypt(eventData.get("data").getAsString()), ECModClientInfo.class);
                            info.check();
                            return new ClientEnvironmentPacket(
                                    info
                            );
                        } catch (Exception e) {
                            SynapseAPI.getInstance().getLogger().alert("[mod] Failed to decrypt [client info]: " + e.getMessage(), e);
                        }
                        break;
                    //OnRequestCheckMaps
                    case "OnClientRenderSuccess":
                        try {
                            JsonObject json = GSON.fromJson(EncryptUtil.decrypt(eventData.get("data").getAsString()), JsonObject.class);
                            Instant time = Instant.ofEpochSecond(json.get("time").getAsInt());
                            List<String> result = Collections.emptyList();
                            if (json.get("result").isJsonArray()) {
                                result = ObjectArrayList.of(GSON.fromJson(json.get("result").getAsJsonArray(), String[].class));
                            }
                            return new MagicMapResultPacket(
                                    time,
                                    result
                            );
                        } catch (Exception e) {
                            SynapseAPI.getInstance().getLogger().alert("[mod] Failed to decrypt [check maps]: " + e.getMessage(), e);
                        }
                        break;
                    //OnRequestCheckDir
                    case "OnClientCreateUIEvent":
                        try {
                            JsonObject json = GSON.fromJson(EncryptUtil.decrypt(eventData.get("data").getAsString()), JsonObject.class);
                            Instant time = Instant.ofEpochSecond(json.get("time").getAsInt());
                            List<String> result = Collections.emptyList();
                            if (json.get("result").isJsonArray()) {
                                result = ObjectArrayList.of(GSON.fromJson(json.get("result").getAsJsonArray(), String[].class));
                            }
                            return new MagicDirectoryResultPacket(
                                    time,
                                    result
                            );
                        } catch (Exception e) {
                            SynapseAPI.getInstance().getLogger().alert("[mod] Failed to decrypt [check dir]: " + e.getMessage(), e);
                        }
                        break;
                    //MagicThingsReportResult
                    case "OnClientCameraChangedEvent":
                        try {
                            JsonObject json = GSON.fromJson(EncryptUtil.decrypt(eventData.get("data").getAsString()), JsonObject.class);
                            Instant time = Instant.ofEpochSecond(json.get("time").getAsInt());
                            if (json.has("result")) {
                                List<String> result = Collections.emptyList();
                                Integer gameType = null;
                                Boolean selinux = null;
                                if (json.get("result").isJsonArray()) {
                                    result = ObjectArrayList.of(GSON.fromJson(json.get("result").getAsJsonArray(), String[].class));
                                }
                                if (json.has("gameType") && json.isJsonPrimitive()) {
                                    gameType = json.get("gameType").getAsInt();
                                }
                                if (json.has("is_selinux") && json.isJsonPrimitive()) {
                                    selinux = json.get("is_selinux").getAsBoolean();
                                }
                                return new MagicReportPacket(
                                        time,
                                        result,
                                        gameType,
                                        selinux
                                );
                            } else if (json.has("error")) {
                                return new MagicReportFailPacket(
                                        time,
                                        json.get("error").getAsString()
                                );
                            }
                        } catch (Exception e) {
                            SynapseAPI.getInstance().getLogger().alert("[mod] Failed to decrypt [magic things report]: " + e.getMessage(), e);
                        }
                        break;
                    case "OnClientNodeUpdated": {
                        return new MagicNodeUpdatedPacket();
                    }
                }
                break;
        }
        return null;
    }

    @Override
    public void encode() {
        this.reset();
        try (MessageBufferPacker packer = MessagePack.newDefaultBufferPacker()) {
            if (encrypt) {
                packer.packValue(subPackets[0].pack());
            } else {
                packer.packValue(data);
            }
            this.putByteArray(packer.toByteArray());
        } catch (IOException e) {
            throw new RuntimeException("MsgPack encode failed: " + e.getMessage(), e);
        }
        this.put(UNKNOWN_BYTES_SENDING);
    }

}
