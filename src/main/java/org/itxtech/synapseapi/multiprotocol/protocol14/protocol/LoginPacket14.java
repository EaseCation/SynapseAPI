package org.itxtech.synapseapi.multiprotocol.protocol14.protocol;

import java.nio.charset.StandardCharsets;
import java.util.*;

import cn.nukkit.Server;
import cn.nukkit.utils.PersonaPiece;
import cn.nukkit.utils.PersonaPieceTint;
import cn.nukkit.utils.SerializedImage;
import cn.nukkit.utils.SkinAnimation;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;

import cn.nukkit.entity.data.Skin;
import cn.nukkit.network.protocol.ProtocolInfo;
import com.netease.mc.authlib.Profile;
import com.netease.mc.authlib.TokenChain;
import com.netease.mc.authlib.exception.AuthException;

/**
 * Created by on 15-10-13.
 */
public class LoginPacket14 extends Packet14 {

    public static final int NETWORK_ID = ProtocolInfo.LOGIN_PACKET;

    public boolean isFirstTimeLogin = false;

    public String username;
    public int protocol;
    public UUID clientUUID;
    public long clientId;
    public String deviceId;
    public String xuid;

    public Skin skin;

    @Override
    public int pid() {
        return NETWORK_ID;
    }

    @Override
    public void decode() {
        this.protocol = this.getInt();
        this.setBuffer(this.getByteArray(), 0);
        decodeChainData();
        decodeSkinData();
    }

    @Override
    public void encode() {

    }

    public int getProtocol() {
        return protocol;
    }

    private void decodeChainData() {
        Map<String, List<String>> map = new Gson().fromJson(new String(this.get(getLInt()), StandardCharsets.UTF_8),
                new TypeToken<Map<String, List<String>>>() {
                }.getType());
        try {
            if (map.isEmpty() || !map.containsKey("chain") || map.get("chain").isEmpty()) return;
            List<String> chains = map.get("chain");
            for (String c : chains) {
                JsonObject chainMap = decodeToken(c);
                if (chainMap == null) continue;
                if (chainMap.has("extraData")) {
                    JsonObject extra = chainMap.get("extraData").getAsJsonObject();
                    if (extra.has("displayName")) this.username = extra.get("displayName").getAsString();
                    if (extra.has("identity")) this.clientUUID = UUID.fromString(extra.get("identity").getAsString());
                    if (extra.has("XUID")) this.xuid = extra.get("XUID").getAsString();
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
    private void neteaseDecode() throws AuthException {
        this.clientUUID = null;
        this.username = null;
        Map<String, List<String>> map = new Gson().fromJson(new String(this.get(this.getLInt()), StandardCharsets.UTF_8),
                new TypeToken<Map<String, List<String>>>() {
                }.getType());
        if (map.isEmpty() || !map.containsKey("chain") || map.get("chain").isEmpty())
            return;
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
        try{
            Profile profile = TokenChain.check(chainArr);
            this.xuid = profile.XUID;
            this.clientUUID = profile.identity;
            this.username = profile.displayName;
        } catch (Exception e) {
            // TODO: handle exception,认证失败
            this.clientUUID = null;//若认证失败，则clientUUID为null。
            throw e;
        }
    }

    private void decodeSkinData() {
        JsonObject skinToken = decodeToken(new String(this.get(this.getLInt())));
        if (skinToken.has("ClientRandomId")) this.clientId = skinToken.get("ClientRandomId").getAsLong();
        if (skinToken.has("DeviceId")) this.deviceId = skinToken.get("DeviceId").getAsString();
        skin = new Skin().setPlayerSkin(true);
        if (skinToken.has("SkinId")) {
            skin.setSkinId(skinToken.get("SkinId").getAsString());
        }
        if (skinToken.has("PlayFabID")) {
            skin.setPlayFabId(skinToken.get("PlayFabID").getAsString());
        }
        if (skinToken.has("CapeId")) {
            skin.setCapeId(skinToken.get("CapeId").getAsString());
        }

        skin.setSkinData(getImage(skinToken, "Skin"));
        skin.setCapeData(getImage(skinToken, "Cape"));
        if (skinToken.has("PremiumSkin")) {
            skin.setPremium(skinToken.get("PremiumSkin").getAsBoolean());
        }
        if (skinToken.has("PersonaSkin")) {
            skin.setPersona(skinToken.get("PersonaSkin").getAsBoolean());
        }
        if (skinToken.has("CapeOnClassicSkin")) {
            skin.setCapeOnClassic(skinToken.get("CapeOnClassicSkin").getAsBoolean());
        }

        if (skinToken.has("SkinResourcePatch")) {
            skin.setSkinResourcePatch(new String(Base64.getDecoder().decode(skinToken.get("SkinResourcePatch").getAsString()), StandardCharsets.UTF_8));
        }

        if (skinToken.has("SkinGeometryData")) {
            skin.setGeometryData(new String(Base64.getDecoder().decode(skinToken.get("SkinGeometryData").getAsString()), StandardCharsets.UTF_8));
        }

        if (skinToken.has("AnimationData")) {
            skin.setAnimationData(new String(Base64.getDecoder().decode(skinToken.get("AnimationData").getAsString()), StandardCharsets.UTF_8));
        }

        if (skinToken.has("AnimatedImageData")) {
            JsonArray array = skinToken.get("AnimatedImageData").getAsJsonArray();
            for (JsonElement element : array) {
                skin.getAnimations().add(getAnimation(element.getAsJsonObject()));
            }
        }

        if (skinToken.has("SkinColor")) {
            skin.setSkinColor(skinToken.get("SkinColor").getAsString());
        }

        if (skinToken.has("ArmSize")) {
            skin.setArmSize(skinToken.get("ArmSize").getAsString());
        }

        if (skinToken.has("PersonaPieces")) {
            for (JsonElement object : skinToken.get("PersonaPieces").getAsJsonArray()) {
                skin.getPersonaPieces().add(getPersonaPiece(object.getAsJsonObject()));
            }
        }

        if (skinToken.has("PieceTintColors")) {
            for (JsonElement object : skinToken.get("PieceTintColors").getAsJsonArray()) {
                skin.getTintColors().add(getTint(object.getAsJsonObject()));
            }
        }
    }

    private JsonObject decodeToken(String token) {
        String[] base = token.split("\\.");
        if (base.length < 2) return null;
        byte[] decode;
        try {
            decode = Base64.getUrlDecoder().decode(base[1]);
        } catch(IllegalArgumentException e) {
            decode = Base64.getDecoder().decode(base[1]);
        }
        return new Gson().fromJson(new String(decode, StandardCharsets.UTF_8), JsonObject.class);
    }

    private static SkinAnimation getAnimation(JsonObject element) {
        float frames = element.get("Frames").getAsFloat();
        int type = element.get("Type").getAsInt();
        byte[] data = Base64.getDecoder().decode(element.get("Image").getAsString());
        int width = element.get("ImageWidth").getAsInt();
        int height = element.get("ImageHeight").getAsInt();
        int expression;
        if (element.has("AnimationExpression")) {
            expression = element.get("AnimationExpression").getAsInt();
        } else {
            expression = 0;
        }
        return new SkinAnimation(new SerializedImage(width, height, data), type, frames, expression);
    }

    private static SerializedImage getImage(JsonObject token, String name) {
        if (token.has(name + "Data")) {
            byte[] skinImage = Base64.getDecoder().decode(token.get(name + "Data").getAsString());
            if (token.has(name + "ImageHeight") && token.has(name + "ImageWidth")) {
                int width = token.get(name + "ImageWidth").getAsInt();
                int height = token.get(name + "ImageHeight").getAsInt();
                return new SerializedImage(width, height, skinImage);
            } else {
                return SerializedImage.fromLegacy(skinImage);
            }
        }
        return SerializedImage.EMPTY;
    }

    private static PersonaPiece getPersonaPiece(JsonObject object) {
        String pieceId = object.get("PieceId").getAsString();
        String pieceType = object.get("PieceType").getAsString();
        String packId = object.get("PackId").getAsString();
        boolean isDefault = object.get("IsDefault").getAsBoolean();
        String productId = object.get("ProductId").getAsString();
        return new PersonaPiece(pieceId, pieceType, packId, isDefault, productId);
    }

    public static PersonaPieceTint getTint(JsonObject object) {
        String pieceType = object.get("PieceType").getAsString();
        List<String> colors = new ArrayList<>();
        for (JsonElement element : object.get("Colors").getAsJsonArray()) {
            colors.add(element.getAsString()); // remove #
        }
        return new PersonaPieceTint(pieceType, colors);
    }

    @Override
    public Skin getSkin() {
        return this.skin;
    }
    
}
