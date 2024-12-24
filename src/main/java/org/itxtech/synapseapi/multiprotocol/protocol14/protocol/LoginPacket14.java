package org.itxtech.synapseapi.multiprotocol.protocol14.protocol;

import cn.nukkit.Server;
import cn.nukkit.entity.data.Skin;
import cn.nukkit.network.protocol.ProtocolInfo;
import cn.nukkit.utils.JsonUtil;
import cn.nukkit.utils.LoginChainData;
import cn.nukkit.utils.PersonaPiece;
import cn.nukkit.utils.PersonaPieceTint;
import cn.nukkit.utils.SerializedImage;
import cn.nukkit.utils.SkinAnimation;
import cn.nukkit.utils.TextFormat;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.netease.mc.authlib.TokenChainEC;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import lombok.ToString;
import lombok.extern.log4j.Log4j2;
import org.itxtech.synapseapi.multiprotocol.protocol12.utils.ClientChainData12;
import org.itxtech.synapseapi.multiprotocol.protocol12.utils.ClientChainData12NetEase;
import org.itxtech.synapseapi.multiprotocol.protocol12.utils.ClientChainData12Urgency;
import org.itxtech.synapseapi.utils.ClientChainDataXbox;

import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * Created by on 15-10-13.
 */
@Log4j2
@ToString
public class LoginPacket14 extends Packet14 {
    private static final boolean DEBUG_ENVIRONMENT = Boolean.getBoolean("easecation.debugging");

    private static final TypeReference<Map<String, List<String>>> CHAIN_DATA_TYPE_REFERENCE = new TypeReference<Map<String, List<String>>>() {
    };

    public static final int NETWORK_ID = ProtocolInfo.LOGIN_PACKET;

    public boolean isFirstTimeLogin = false;

    public String username;
    public int protocol;
    public UUID clientUUID;
    public long clientId;
    public String deviceId;
    public String platformOfflineId;
    public String platformOnlineId;
    public boolean editorMode;
    public boolean supportClientChunkGeneration;
    public int platformType;
    public int memoryTier;
    public int maxViewDistance;
    public String xuid;
    public String titleId;
    public String sandboxId;// = "RETAIL"

    public Skin skin;

    public LoginChainData decodedLoginChainData;
    public boolean netEaseClient;

    public boolean reconnect;
    public String skinIID;
    public int growthLevel;
    public String bloomData;

    @Override
    public int pid() {
        return NETWORK_ID;
    }

    @Override
    public void decode() {
        this.protocol = this.getInt();
        this.setBuffer(this.getByteArray(), 0);
        try {
            decodeChainData();
            decodeSkinData();
        } catch (JsonProcessingException e) {
            throw new RuntimeException("broken json", e);
        }

        tryDecodeLoginChainData();
    }

    public void tryDecodeLoginChainData() {
        try {
            byte[] buffer = getBuffer();

            decodedLoginChainData = ClientChainData12NetEase.of(buffer);
            if (decodedLoginChainData.getClientUUID() != null) { // 网易认证通过！
                this.netEaseClient = true;
                log.info("[Login] " + this.username + TextFormat.RED + " 中国版验证通过！" + protocol);
                return;
            }

            if (DEBUG_ENVIRONMENT && !ClientChainDataXbox.of(buffer).isXboxAuthed() && username.startsWith("netease")) { // 国际版验证失败, 特定前缀玩家名解析为中国版 (仅限调试环境)
                this.netEaseClient = true;
                log.info("[Login] " + this.username + TextFormat.BLUE + " Xbox验证未通过！" + protocol);
                return;
            }

            try { // 国际版普通认证
                log.info("[Login] " + this.username + TextFormat.GREEN + " 正在解析为国际版！" + protocol);
                decodedLoginChainData = ClientChainData12.of(buffer);
            } catch (Exception e) {
                log.info("[Login] " + this.username + TextFormat.YELLOW + " 解析时出现问题，采用紧急解析方案！", e);
                decodedLoginChainData = ClientChainData12Urgency.of(buffer);
            }
        } catch (Exception e) {
            decodedLoginChainData = null;
            log.throwing(e);
        }
    }

    @Override
    public void encode() {

    }

    public int getProtocol() {
        return protocol;
    }

    private void decodeChainData() throws JsonProcessingException {
        int length = this.getLInt();
        if (!this.isReadable(length)) {
            throw new IndexOutOfBoundsException("array length mismatch");
        }
        byte[] bytes = this.get(length);
        Map<String, List<String>> map = JsonUtil.COMMON_JSON_MAPPER.readValue(new String(bytes, StandardCharsets.UTF_8), CHAIN_DATA_TYPE_REFERENCE);
        try {
            if (map.isEmpty() || !map.containsKey("chain") || map.get("chain").isEmpty()) {
                return;
            }
            List<String> chains = map.get("chain");
            for (String c : chains) {
                JsonNode chainMap = decodeToken(c);
                if (chainMap == null || !chainMap.isObject()) {
                    continue;
                }
                JsonNode extra = chainMap.get("extraData");
                if (extra != null && extra.isObject()) {
                    JsonNode displayNameNode = extra.get("displayName");
                    if (displayNameNode != null) {
                        this.username = displayNameNode.asText();
                    }
                    JsonNode identityNode = extra.get("identity");
                    if (identityNode != null) {
                        this.clientUUID = UUID.fromString(identityNode.asText());
                    }
                    JsonNode xuidNode = extra.get("XUID");
                    if (xuidNode != null) {
                        this.xuid = xuidNode.asText();
                    }
                    JsonNode titleIdNode = extra.get("titleId");
                    if (titleIdNode != null) {
                        this.titleId = titleIdNode.asText();
                    }
                    JsonNode sandboxId = extra.get("sandboxId");
                    if (sandboxId != null) {
                        this.sandboxId = sandboxId.asText();
                    }
                }
            }
        } catch (Exception e) {
            this.setOffset(0);
            try {
                neteaseDecode();
            } catch (Exception e1) {
                Server.getInstance().getLogger().logException(e1);
            }
        }
    }

    //netease解析客户端信息。
    private void neteaseDecode() throws Exception {
        this.clientUUID = null;
        this.username = null;
        int length = this.getLInt();
        if (!this.isReadable(length)) {
            throw new IndexOutOfBoundsException("array length mismatch");
        }
        byte[] bytes = this.get(length);
        Map<String, List<String>> map = JsonUtil.COMMON_JSON_MAPPER.readValue(new String(bytes, StandardCharsets.UTF_8), CHAIN_DATA_TYPE_REFERENCE);
        if (map.isEmpty() || !map.containsKey("chain") || map.get("chain").isEmpty()) {
            return;
        }
        List<String> chains = map.get("chain");
        int chainSize = chains.size();
        if (chainSize < 2) {//最少2个字符串。
            //Server.getInstance().getLogger().alert("过短 chainSize");
            return;
        }
        String[] chainArr = new String[chainSize - 1];
        Iterator<String> iterator = chains.iterator();
        int index = 0;
        iterator.next();
        while (iterator.hasNext()) {
            chainArr[index] = iterator.next();
            ++index;
        }
        try {
            JsonObject profile = TokenChainEC.check(chainArr);

            JsonElement xuidNode = profile.get("XUID");
            if (xuidNode != null) {
                this.xuid = xuidNode.getAsString();
            }
            JsonElement identityNode = profile.get("identity");
            if (identityNode != null) {
                this.clientUUID = UUID.fromString(identityNode.getAsString());
            }
            JsonElement displayNameNode = profile.get("displayName");
            if (displayNameNode != null) {
                this.username = displayNameNode.getAsString();
            }
        } catch (Exception e) {
            // TODO: handle exception,认证失败
            this.clientUUID = null;//若认证失败，则clientUUID为null。
            throw e;
        }
    }

    private void decodeSkinData() throws JsonProcessingException {
        int length = this.getLInt();
        if (!this.isReadable(length)) {
            throw new IndexOutOfBoundsException("array length mismatch");
        }
        byte[] bytes = this.get(length);
        JsonNode skinToken = decodeToken(new String(bytes));

        JsonNode clientRandomIdNode = skinToken.get("ClientRandomId");
        if (clientRandomIdNode != null) {
            this.clientId = clientRandomIdNode.asLong();
        }
        JsonNode deviceIdNode = skinToken.get("DeviceId");
        if (deviceIdNode != null) {
            this.deviceId = skinToken.get("DeviceId").asText();
        }

        skin = new Skin().setPlayerSkin(true);
        JsonNode skinIdNode = skinToken.get("SkinId");
        if (skinIdNode != null) {
            skin.setSkinId(skinIdNode.asText());
        }
        JsonNode playFabIdNode = skinToken.get("PlayFabId");
        if (playFabIdNode != null) {
            skin.setPlayFabId(playFabIdNode.asText());
        }
        JsonNode capeIdNode = skinToken.get("CapeId");
        if (capeIdNode != null) {
            skin.setCapeId(capeIdNode.asText());
        }

        skin.setSkinData(getImage(skinToken, "Skin"));
        skin.setCapeData(getImage(skinToken, "Cape"));
        JsonNode premiumSkinNode = skinToken.get("PremiumSkin");
        if (premiumSkinNode != null) {
            skin.setPremium(premiumSkinNode.asBoolean());
        }
        JsonNode personaSkinNode = skinToken.get("PersonaSkin");
        if (personaSkinNode != null) {
            skin.setPersona(personaSkinNode.asBoolean());
        }
        JsonNode capeOnClassicSkinNode = skinToken.get("CapeOnClassicSkin");
        if (capeOnClassicSkinNode != null) {
            skin.setCapeOnClassic(capeOnClassicSkinNode.asBoolean());
        }

        JsonNode skinResourcePatchNode = skinToken.get("SkinResourcePatch");
        if (skinResourcePatchNode != null) {
            skin.setSkinResourcePatch(new String(Base64.getDecoder().decode(skinResourcePatchNode.asText()), StandardCharsets.UTF_8));
        }

        JsonNode skinGeometryDataNode = skinToken.get("SkinGeometryData");
        if (skinGeometryDataNode != null) {
            skin.setGeometryData(new String(Base64.getDecoder().decode(skinGeometryDataNode.asText()), StandardCharsets.UTF_8));
        }

        JsonNode skinGeometryDataEngineVersionNode = skinToken.get("SkinGeometryDataEngineVersion");
        if (skinGeometryDataEngineVersionNode != null) {
            skin.setGeometryDataEngineVersion(new String(Base64.getDecoder().decode(skinGeometryDataEngineVersionNode.asText()), StandardCharsets.UTF_8));
        }

        JsonNode skinAnimationDataNode = skinToken.get("SkinAnimationData");
        if (skinAnimationDataNode != null) {
            skin.setAnimationData(new String(Base64.getDecoder().decode(skinAnimationDataNode.asText()), StandardCharsets.UTF_8));
        } else if ((skinAnimationDataNode = skinToken.get("AnimationData")) != null) {
            skin.setAnimationData(new String(Base64.getDecoder().decode(skinAnimationDataNode.asText()), StandardCharsets.UTF_8));
        }

        JsonNode animatedImageDataNode = skinToken.get("AnimatedImageData");
        if (animatedImageDataNode != null) {
            for (JsonNode element : animatedImageDataNode) {
                skin.getAnimations().add(getAnimation(element));
            }
        }

        JsonNode skinColorNode = skinToken.get("SkinColor");
        if (skinColorNode != null) {
            skin.setSkinColor(skinColorNode.asText());
        }

        JsonNode armSizeNode = skinToken.get("ArmSize");
        if (armSizeNode != null) {
            skin.setArmSize(armSizeNode.asText());
        }

        JsonNode personaPiecesNode = skinToken.get("PersonaPieces");
        if (personaPiecesNode != null) {
            for (JsonNode object : personaPiecesNode) {
                skin.getPersonaPieces().add(getPersonaPiece(object));
            }
        }

        JsonNode pieceTintColorsNode = skinToken.get("PieceTintColors");
        if (pieceTintColorsNode != null) {
            for (JsonNode object : pieceTintColorsNode) {
                skin.getTintColors().add(getTint(object));
            }
        }

        JsonNode platformOfflineIdNode = skinToken.get("PlatformOfflineId");
        if (platformOfflineIdNode != null) {
            this.platformOfflineId = platformOfflineIdNode.asText();
        }

        JsonNode platformOnlineIdNode = skinToken.get("PlatformOnlineId");
        if (platformOnlineIdNode != null) {
            this.platformOnlineId = platformOnlineIdNode.asText();
        }

        JsonNode trustedSkinNode = skinToken.get("TrustedSkin");
        if (trustedSkinNode != null) {
            skin.setTrusted(trustedSkinNode.asBoolean());
        }

        JsonNode isEditorModeNode = skinToken.get("IsEditorMode");
        if (isEditorModeNode != null) {
            this.editorMode = isEditorModeNode.asBoolean();
        }

        JsonNode overrideSkinNode = skinToken.get("OverrideSkin");
        if (overrideSkinNode != null) {
            skin.setOverridingPlayerAppearance(overrideSkinNode.asBoolean());
        }

        JsonNode compatibleWithClientSideChunkGenNode = skinToken.get("CompatibleWithClientSideChunkGen");
        if (compatibleWithClientSideChunkGenNode != null) {
            this.supportClientChunkGeneration = compatibleWithClientSideChunkGenNode.asBoolean();
        }

        JsonNode platformTypeNode = skinToken.get("PlatformType");
        if (platformTypeNode != null) {
            this.platformType = platformTypeNode.asInt();
        }

        JsonNode memoryTierNode = skinToken.get("MemoryTier");
        if (memoryTierNode != null) {
            this.memoryTier = memoryTierNode.asInt();
        }

        JsonNode maxViewDistanceNode = skinToken.get("MaxViewDistance");
        if (maxViewDistanceNode != null) {
            this.maxViewDistance = maxViewDistanceNode.asInt();
        }

        // NetEase only:

        JsonNode isReconnectNode = skinToken.get("IsReconnect");
        if (isReconnectNode != null) {
            this.reconnect = isReconnectNode.asBoolean();
        }

        JsonNode skinIIDNode = skinToken.get("SkinIID");
        if (skinIIDNode != null) {
            this.skinIID = skinIIDNode.asText();
        }

        JsonNode growthLevelNode = skinToken.get("GrowthLevel");
        if (growthLevelNode != null) {
            this.growthLevel = growthLevelNode.asInt();
        }

        JsonNode bloomDataNode = skinToken.get("BloomData");
        if (bloomDataNode != null) {
            this.bloomData = bloomDataNode.asText();
        }
    }

    private JsonNode decodeToken(String token) throws JsonProcessingException {
        String[] base = token.split("\\.", 4);
        if (base.length < 2) return null;
        byte[] decode;
        try {
            decode = Base64.getUrlDecoder().decode(base[1]);
        } catch (IllegalArgumentException e) {
            decode = Base64.getDecoder().decode(base[1]);
        }
        return JsonUtil.COMMON_JSON_MAPPER.readTree(new String(decode, StandardCharsets.UTF_8));
    }

    private static SkinAnimation getAnimation(JsonNode element) {
        float frames = (float) element.get("Frames").asDouble();
        int type = element.get("Type").asInt();
        byte[] data = Base64.getDecoder().decode(element.get("Image").asText());
        int width = element.get("ImageWidth").asInt();
        int height = element.get("ImageHeight").asInt();
        int expression;
        JsonNode node = element.get("AnimationExpression");
        if (node != null) {
            expression = node.asInt();
        } else {
            expression = 0;
        }
        return new SkinAnimation(new SerializedImage(width, height, data), type, frames, expression);
    }

    private static SerializedImage getImage(JsonNode token, String name) {
        String dataKey = name + "Data";
        JsonNode dataNode = token.get(dataKey);
        if (dataNode != null) {
            byte[] skinImage = Base64.getDecoder().decode(dataNode.asText());
            String widthKey = name + "ImageWidth";
            String heightKey = name + "ImageHeight";
            JsonNode widthNode = token.get(widthKey);
            JsonNode heightNode = token.get(heightKey);
            if (widthNode != null && heightNode != null) {
                int width = widthNode.asInt();
                int height = heightNode.asInt();
                return new SerializedImage(width, height, skinImage);
            } else {
                return SerializedImage.fromLegacy(skinImage);
            }
        }
        return SerializedImage.EMPTY;
    }

    private static PersonaPiece getPersonaPiece(JsonNode object) {
        String pieceId = object.get("PieceId").asText();
        String pieceType = object.get("PieceType").asText();
        String packId = object.get("PackId").asText();
        boolean isDefault = object.get("IsDefault").asBoolean();
        String productId = object.get("ProductId").asText();
        return new PersonaPiece(pieceId, pieceType, packId, isDefault, productId);
    }

    public static PersonaPieceTint getTint(JsonNode object) {
        String pieceType = object.get("PieceType").asText();
        List<String> colors = new ObjectArrayList<>();
        for (JsonNode element : object.get("Colors")) {
            colors.add(element.asText()); // remove #
        }
        return new PersonaPieceTint(pieceType, colors);
    }
}
