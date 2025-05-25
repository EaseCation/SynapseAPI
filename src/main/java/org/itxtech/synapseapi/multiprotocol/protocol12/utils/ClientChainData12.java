package org.itxtech.synapseapi.multiprotocol.protocol12.utils;

import cn.nukkit.utils.BinaryStream;
import cn.nukkit.utils.LoginChainData;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import org.itxtech.synapseapi.multiprotocol.AbstractProtocol;
import org.itxtech.synapseapi.multiprotocol.protocol12.protocol.LoginPacket;

import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * ClientChainData is a container of chain data sent from clients.
 * <p>
 * Device information such as client UUID, xuid and serverAddress, can be
 * read from instances of this object.
 * <p>
 * To get chain data, you can use player.getLoginChainData() or read(loginPacket)
 * <p>
 * ===============
 * author: boybook
 * Nukkit Project
 * ===============
 */
public final class ClientChainData12 implements LoginChainData {
    private static final Gson GSON = new Gson();

    public static ClientChainData12 of(byte[] buffer, int protocol) {
        return new ClientChainData12(buffer, protocol);
    }

    public static ClientChainData12 read(LoginPacket pk) {
        return of(pk.getBuffer(), pk.protocol);
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public UUID getClientUUID() {
        return clientUUID;
    }

    @Override
    public String getIdentityPublicKey() {
        return identityPublicKey;
    }

    @Override
    public long getClientId() {
        return clientId;
    }

    @Override
    public String getNetEaseUID() {
        return neteaseUid;
    }

    @Override
    public String getNetEaseSid() {
        return neteaseSid;
    }

    @Override
    public String getNetEasePlatform() {
        return neteasePlatform;
    }

    @Override
    public String getNetEaseClientOsName() {
        return neteaseClientOsName;
    }

    @Override
    public String getNetEaseEnv() {
        return neteaseEnv;
    }

    @Override
    public String getNetEaseClientEngineVersion() {
        return neteaseClientEngineVersion;
    }

    @Override
    public String getNetEaseClientPatchVersion() {
        return neteaseClientPatchVersion;
    }

    @Override
    public String getNetEaseClientBit() {
        return neteaseClientBit;
    }

    @Override
    public String getServerAddress() {
        return serverAddress;
    }

    @Override
    public String getDeviceId() {
        return deviceId;
    }

    @Override
    public String getDeviceModel() {
        return deviceModel;
    }

    @Override
    public int getDeviceOS() {
        return deviceOS;
    }

    @Override
    public String getGameVersion() {
        return gameVersion;
    }

    @Override
    public int getGuiScale() {
        return guiScale;
    }

    @Override
    public String getLanguageCode() {
        return languageCode;
    }

    @Override
    public String getXUID() {
        return xuid;
    }

    @Override
    public int getCurrentInputMode() {
        return currentInputMode;
    }

    @Override
    public void setCurrentInputMode(int mode) {
        this.currentInputMode = mode;
    }

    @Override
    public int getDefaultInputMode() {
        return defaultInputMode;
    }

    @Override
    public String getCapeData() {
        return capeData;
    }

    public final static int UI_PROFILE_CLASSIC = 0;
    public final static int UI_PROFILE_POCKET = 1;

    @Override
    public int getUIProfile() {
        return UIProfile;
    }

    ///////////////////////////////////////////////////////////////////////////
    // Override
    ///////////////////////////////////////////////////////////////////////////

    @Override
    public boolean equals(Object obj) {
        return obj instanceof ClientChainData12 && Objects.equals(bs, ((ClientChainData12) obj).bs);
    }

    @Override
    public int hashCode() {
        return bs.hashCode();
    }

    ///////////////////////////////////////////////////////////////////////////
    // Internal
    ///////////////////////////////////////////////////////////////////////////

    private String username;
    private UUID clientUUID;
    private String xuid;
    private String identityPublicKey;
    private String neteaseUid;
    private String neteaseSid;
    private String neteasePlatform;
    private String neteaseClientOsName;
    private String neteaseEnv;
    private String neteaseClientEngineVersion;
    private String neteaseClientPatchVersion;
    private String neteaseClientBit;

    private long clientId;
    private String serverAddress;
    private String deviceId;
    private String deviceModel;
    private int deviceOS;
    private String gameVersion;
    private int guiScale;
    private String languageCode;
    private int currentInputMode;
    private int defaultInputMode;

    private int UIProfile;

    private String capeData;

    private final transient BinaryStream bs = new BinaryStream();

    private ClientChainData12(byte[] buffer, int protocol) {
        bs.setBuffer(buffer, 0);
        decodeChainData(protocol);
        decodeSkinData();
    }

    private void decodeChainData(int protocol) {
        Map<String, ?> root = GSON.fromJson(new String(bs.get(bs.getLInt()), StandardCharsets.UTF_8),
                new TypeToken<Map<String, ?>>() {
                }.getType());
        if (root.isEmpty()) {
            return;
        }
        List<String> chains;
        if (protocol >= AbstractProtocol.PROTOCOL_121_90.getProtocolStart()) {
            Object authenticationType = root.get("AuthenticationType");
            if (!(authenticationType instanceof Number)) { //integer 0
                return;
            }
            Object token = root.get("Token");
            if (!(token instanceof String)) { //empty ""
                return;
            }
            Object certificate = root.get("Certificate");
            if (!(certificate instanceof String cert)) {
                return;
            }
            Map<String, List<String>> map = GSON.fromJson(cert, new TypeToken<Map<String, List<String>>>() {
            }.getType());
            if (map.isEmpty() ||  (chains = map.get("chain")) == null || chains.isEmpty()) {
                return;
            }
        } else {
            Object chain = root.get("chain");
            if (!(chain instanceof List list) || list.isEmpty()) {
                return;
            }
            chains = (List<String>) chain;
        }
        for (String c : chains) {
            JsonObject chainMap = decodeToken(c);
            if (chainMap == null) continue;
            if (chainMap.has("extraData")) {
                JsonObject extra = chainMap.get("extraData").getAsJsonObject();
                if (extra.has("displayName")) this.username = extra.get("displayName").getAsString();
                if (extra.has("identity")) this.clientUUID = UUID.fromString(extra.get("identity").getAsString());
                if (extra.has("XUID")) this.xuid = extra.get("XUID").getAsString();
                if (extra.has("uid")) this.neteaseUid = extra.get("uid").getAsString();
                if (extra.has("netease_sid")) this.neteaseSid = extra.get("netease_sid").getAsString();
                if (extra.has("platform")) this.neteasePlatform = extra.get("platform").getAsString();
                if (extra.has("os_name")) this.neteaseClientOsName = extra.get("os_name").getAsString();
                if (extra.has("env")) this.neteaseEnv = extra.get("env").getAsString();
                if (extra.has("engineVersion")) this.neteaseClientEngineVersion = extra.get("engineVersion").getAsString();
                if (extra.has("patchVersion")) this.neteaseClientPatchVersion = extra.get("patchVersion").getAsString();
                if (extra.has("bit")) this.neteaseClientBit = extra.get("bit").getAsString();
            }
            if (chainMap.has("identityPublicKey"))
                this.identityPublicKey = chainMap.get("identityPublicKey").getAsString();
        }
    }

    private void decodeSkinData() {
        JsonObject skinToken = decodeToken(new String(bs.get(bs.getLInt())));
        if (skinToken == null) return;
        if (skinToken.has("ClientRandomId")) this.clientId = skinToken.get("ClientRandomId").getAsLong();
        if (skinToken.has("DeviceId")) this.deviceId = skinToken.get("DeviceId").getAsString();
        if (skinToken.has("ServerAddress")) this.serverAddress = skinToken.get("ServerAddress").getAsString();
        if (skinToken.has("DeviceModel")) this.deviceModel = skinToken.get("DeviceModel").getAsString();
        if (skinToken.has("DeviceOS")) this.deviceOS = skinToken.get("DeviceOS").getAsInt();
        if (skinToken.has("GameVersion")) this.gameVersion = skinToken.get("GameVersion").getAsString();
        if (skinToken.has("GuiScale")) this.guiScale = skinToken.get("GuiScale").getAsInt();
        if (skinToken.has("LanguageCode")) this.languageCode = skinToken.get("LanguageCode").getAsString();
        if (skinToken.has("CurrentInputMode")) this.currentInputMode = skinToken.get("CurrentInputMode").getAsInt();
        if (skinToken.has("DefaultInputMode")) this.defaultInputMode = skinToken.get("DefaultInputMode").getAsInt();
        if (skinToken.has("UIProfile")) this.UIProfile = skinToken.get("UIProfile").getAsInt();
        if (skinToken.has("CapeData")) this.capeData = skinToken.get("CapeData").getAsString();
    }

    private JsonObject decodeToken(String token) {
        String[] base = token.split("\\.", 4);
        if (base.length < 2) return null;
        byte[] decode;
    	try {
        	decode = Base64.getUrlDecoder().decode(base[1]);
        } catch(IllegalArgumentException e) {
        	decode = Base64.getDecoder().decode(base[1]);
        }
        String json = new String(decode, StandardCharsets.UTF_8);
        //Server.getInstance().getLogger().debug(json);
        return GSON.fromJson(json, JsonObject.class);
    }

    @Override
    public String toString() {
        return "ClientChainData12{" +
                "username='" + username + '\'' +
                ", clientUUID=" + clientUUID +
                ", xuid='" + xuid + '\'' +
                ", identityPublicKey='" + identityPublicKey + '\'' +
                ", clientId=" + clientId +
                ", serverAddress='" + serverAddress + '\'' +
                ", deviceModel='" + deviceModel + '\'' +
                ", deviceOS=" + deviceOS +
                ", gameVersion='" + gameVersion + '\'' +
                ", guiScale=" + guiScale +
                ", languageCode='" + languageCode + '\'' +
                ", currentInputMode=" + currentInputMode +
                ", defaultInputMode=" + defaultInputMode +
                ", UIProfile=" + UIProfile +
                ", capeData='" + capeData + '\'' +
                ", bs=" + bs +
                '}';
    }
}
