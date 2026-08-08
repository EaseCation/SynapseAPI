package org.itxtech.synapseapi.multiprotocol.protocol12.utils;

import cn.nukkit.Server;
import cn.nukkit.utils.BinaryStream;
import cn.nukkit.utils.LoginChainData;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import com.netease.mc.authlib.TokenChainEC;
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
public final class ClientChainData12NetEase implements LoginChainData {
    private static final Gson GSON = new Gson();

    public static ClientChainData12NetEase of(byte[] buffer, int protocol) {
        return new ClientChainData12NetEase(buffer, protocol);
    }

    public static ClientChainData12NetEase read(LoginPacket pk) {
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
    public String getNetEaseDataVersion() {
        return neteaseDataVersion;
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
    public String getNetEaseGameType() {
        return neteaseGameType;
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

    public final static int UI_PROFILE_CLASSIC = 0;
    public final static int UI_PROFILE_POCKET = 1;

    @Override
    public int getUIProfile() {
        return UIProfile;
    }

    @Override
    public String getPlatformOfflineId() {
        return platformOfflineId;
    }

    @Override
    public String getPlatformOnlineId() {
        return platformOnlineId;
    }

    @Override
    public boolean isEditorMode() {
        return editorMode;
    }

    @Override
    public boolean isEditorCapable() {
        return editorCapable;
    }

    @Override
    public int isEditorConnectionIntent() {
        return editorConnectionIntent;
    }

    @Override
    public boolean isSupportClientChunkGeneration() {
        return supportClientChunkGeneration;
    }

    @Override
    public int getPlatformType() {
        return platformType;
    }

    @Override
    public int getMemoryTier() {
        return memoryTier;
    }

    @Override
    public int getMaxViewDistance() {
        return maxViewDistance;
    }

    @Override
    public int getGraphicsMode() {
        return graphicsMode;
    }

    @Override
    public String getPartyId() {
        return partyId;
    }

    @Override
    public boolean isPartyLeader() {
        return partyLeader;
    }

    @Override
    public boolean isFilterProfanity() {
        return filterProfanity;
    }

    @Override
    public boolean isNetEaseReconnect() {
        return neteaseReconnect;
    }

    @Override
    public String getNetEaseSkinIID() {
        return neteaseSkinIID;
    }

    @Override
    public int getNetEaseGrowthLevel() {
        return neteaseGrowthLevel;
    }

    @Override
    public String getSubject() {
        return "";
    }

    @Override
    public String getPlayFabId() {
        return "";
    }

    @Override
    public Integer getPfcd() {
        return null;
    }

    @Override
    public String getTitleId() {
        return "";
    }

    @Override
    public String getSandboxId() {
        return "";
    }

    ///////////////////////////////////////////////////////////////////////////
    // Override
    ///////////////////////////////////////////////////////////////////////////

    @Override
    public boolean equals(Object obj) {
        return obj instanceof ClientChainData12NetEase && Objects.equals(bs, ((ClientChainData12NetEase) obj).bs);
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
    private String neteaseDataVersion;

    private String neteasePlatform;
    private String neteaseClientOsName;
    private String neteaseEnv;
    private String neteaseClientEngineVersion;
    private String neteaseClientPatchVersion;
    private String neteaseClientBit;
    private String neteaseGameType;

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
    private String platformOfflineId;
    private String platformOnlineId;
    private boolean editorMode;
    private boolean editorCapable;
    private int editorConnectionIntent;
    private boolean supportClientChunkGeneration;
    private int platformType;
    private int memoryTier;
    private int maxViewDistance;
    private int graphicsMode;
    private String partyId;
    private boolean partyLeader;
    private boolean filterProfanity;

    private boolean neteaseReconnect;
    private String neteaseSkinIID;
    private int neteaseGrowthLevel;

    private String viaProxyAuthToken;

    private final transient BinaryStream bs = new BinaryStream();

    private ClientChainData12NetEase(byte[] buffer, int protocol) {
        bs.setBuffer(buffer, 0);
        //decodeChainData();
        neteaseDecode(protocol);
        decodeSkinData();
    }

    private void decodeChainData() {
        Map<String, List<String>> map = GSON.fromJson(new String(bs.get(bs.getLInt()), StandardCharsets.UTF_8),
                new TypeToken<Map<String, List<String>>>() {
                }.getType());
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

                if (extra.has("uid")) this.neteaseUid = extra.get("uid").getAsString();
                if (extra.has("netease_sid")) this.neteaseSid = extra.get("netease_sid").getAsString();
                if (extra.has("version")) this.neteaseDataVersion = extra.get("version").getAsString();

                if (extra.has("os_name")) this.neteaseClientOsName = extra.get("os_name").getAsString();
                if (extra.has("env")) this.neteaseEnv = extra.get("env").getAsString();
                if (extra.has("engineVersion")) this.neteaseClientEngineVersion = extra.get("engineVersion").getAsString();
                if (extra.has("patchVersion")) this.neteaseClientPatchVersion = extra.get("patchVersion").getAsString();
                if (extra.has("bit")) this.neteaseClientBit = extra.get("bit").getAsString();
                if (extra.has("game_type")) this.neteaseGameType = extra.get("game_type").getAsString();
            }
            if (chainMap.has("identityPublicKey"))
                this.identityPublicKey = chainMap.get("identityPublicKey").getAsString();
        }
    }

    //netease解析客户端信息。
    private void neteaseDecode(int protocol) {
        this.xuid = null;
        this.clientUUID = null;
        this.username = null;

        Map<String, ?> root = GSON.fromJson(new String(bs.get(bs.getLInt()), StandardCharsets.UTF_8),
                new TypeToken<Map<String, ?>>() {
                }.getType());
        if (root == null || root.isEmpty()) {
            return;
        }
        List<String> chains;
        if (protocol >= AbstractProtocol.PROTOCOL_121_90.getProtocolStart()) {
            Object authenticationType = root.get("AuthenticationType");
            if (!(authenticationType instanceof Number)) {
                return;
            }

            Object token = root.get("Token");
            if (!(token instanceof String jwt)) {
                return;
            }
            if (false && !jwt.isEmpty()) {
                JsonObject payload = decodeToken(jwt);
                if (payload != null) {
                    if (payload.has("xname")) this.username = payload.get("xname").getAsString();
                    if (payload.has("xid")) {
                        this.xuid = payload.get("xid").getAsString();
                        if (!xuid.isEmpty()) {
                            this.clientUUID = UUID.nameUUIDFromBytes(("pocket-auth-1-xuid:" + xuid).getBytes(StandardCharsets.UTF_8));
                        }
                    }
                }
                return;
            }

            Object certificate = root.get("Certificate");
            if (!(certificate instanceof String cert)) {
                return;
            }
            Map<String, List<String>> map = GSON.fromJson(cert, new TypeToken<Map<String, List<String>>>() {
            }.getType());
            if (map == null || map.isEmpty() || (chains = map.get("chain")) == null || chains.isEmpty()) {
                return;
            }
        } else {
            Object chain = root.get("chain");
            if (!(chain instanceof List list) || list.isEmpty()) {
                return;
            }
            chains = (List<String>) chain;
        }

        int chainSize = chains.size();
        if (chainSize < 2)//最少2个字符串。
        {
            Server.getInstance().getLogger().warning("短chainSize");
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
            JsonObject profile = TokenChainEC.check(chainArr);
            if (profile.has("XUID")) this.xuid = profile.get("XUID").getAsString();
            if (profile.has("identity")) this.clientUUID = UUID.fromString(profile.get("identity").getAsString());
            if (profile.has("displayName")) this.username = profile.get("displayName").getAsString();
            if (profile.has("uid")) this.neteaseUid = profile.get("uid").getAsString();
            if (profile.has("netease_sid")) this.neteaseSid = profile.get("netease_sid").getAsString();
            if (profile.has("platform")) this.neteasePlatform = profile.get("platform").getAsString();
            if (profile.has("os_name")) this.neteaseClientOsName = profile.get("os_name").getAsString();
            if (profile.has("env")) this.neteaseEnv = profile.get("env").getAsString();
            if (profile.has("engineVersion")) this.neteaseClientEngineVersion = profile.get("engineVersion").getAsString();
            if (profile.has("patchVersion")) this.neteaseClientPatchVersion = profile.get("patchVersion").getAsString();
            if (profile.has("bit")) this.neteaseClientBit = profile.get("bit").getAsString();
        } catch (Exception e) {
            // TODO: handle exception,认证失败
            //Server.getInstance().getLogger().logException(e);
            this.clientUUID = null;//若认证失败，则clientUUID为null。
        }
    }

    private void decodeSkinData() {
        JsonObject skinToken = decodeToken(new String(bs.get(bs.getLInt())));
        if (skinToken == null) return;
        if (skinToken.has("ClientRandomId")) this.clientId = skinToken.get("ClientRandomId").getAsLong();
        if (skinToken.has("ServerAddress")) this.serverAddress = skinToken.get("ServerAddress").getAsString();
        if (skinToken.has("DeviceId")) this.deviceId = skinToken.get("DeviceId").getAsString();
        if (skinToken.has("DeviceModel")) this.deviceModel = skinToken.get("DeviceModel").getAsString();
        if (skinToken.has("DeviceOS")) this.deviceOS = skinToken.get("DeviceOS").getAsInt();
        if (skinToken.has("GameVersion")) this.gameVersion = skinToken.get("GameVersion").getAsString();
        if (skinToken.has("GuiScale")) this.guiScale = skinToken.get("GuiScale").getAsInt();
        if (skinToken.has("LanguageCode")) this.languageCode = skinToken.get("LanguageCode").getAsString();
        if (skinToken.has("CurrentInputMode")) this.currentInputMode = skinToken.get("CurrentInputMode").getAsInt();
        if (skinToken.has("DefaultInputMode")) this.defaultInputMode = skinToken.get("DefaultInputMode").getAsInt();
        if (skinToken.has("UIProfile")) this.UIProfile = skinToken.get("UIProfile").getAsInt();
        if (skinToken.has("PlatformOfflineId")) this.platformOfflineId = skinToken.get("PlatformOfflineId").getAsString();
        if (skinToken.has("PlatformOnlineId")) this.platformOnlineId = skinToken.get("PlatformOnlineId").getAsString();
        if (skinToken.has("IsEditorMode")) this.editorMode = skinToken.get("IsEditorMode").getAsBoolean();
        if (skinToken.has("CompatibleWithClientSideChunkGen")) this.supportClientChunkGeneration = skinToken.get("CompatibleWithClientSideChunkGen").getAsBoolean();
        if (skinToken.has("PlatformType")) this.platformType = skinToken.get("PlatformType").getAsInt();
        if (skinToken.has("MemoryTier")) this.memoryTier = skinToken.get("MemoryTier").getAsInt();
        if (skinToken.has("MaxViewDistance")) this.maxViewDistance = skinToken.get("MaxViewDistance").getAsInt();
        if (skinToken.has("GraphicsMode")) this.graphicsMode = skinToken.get("GraphicsMode").getAsInt();
        if (skinToken.has("PartyId")) this.partyId = skinToken.get("PartyId").getAsString();
        if (skinToken.has("IsPartyLeader")) this.partyLeader = skinToken.get("IsPartyLeader").getAsBoolean();
        if (skinToken.has("FilterProfanity")) this.filterProfanity = skinToken.get("FilterProfanity").getAsBoolean();
        if (skinToken.has("ClientIsEditorCapable")) this.editorCapable = skinToken.get("ClientIsEditorCapable").getAsBoolean();
        if (skinToken.has("ClientEditorConnectionIntent")) this.editorConnectionIntent = skinToken.get("ClientEditorConnectionIntent").getAsInt();

        if (skinToken.has("IsReconnect")) this.neteaseReconnect = skinToken.get("IsReconnect").getAsBoolean();
        if (skinToken.has("SkinIID")) this.neteaseSkinIID = skinToken.get("SkinIID").getAsString();
        if (skinToken.has("GrowthLevel")) this.neteaseGrowthLevel = skinToken.get("GrowthLevel").getAsInt();

        if (skinToken.has("ViaProxyAuthToken")) this.viaProxyAuthToken = skinToken.get("ViaProxyAuthToken").getAsString();
    }

    @Override
    public String getViaProxyAuthToken() {
        return viaProxyAuthToken;
    }

    private JsonObject decodeToken(String token) {
        String[] base = token.split("\\.", 3);
        if (base.length != 3) return null;
        byte[] decode;
    	try {
        	decode = Base64.getUrlDecoder().decode(base[1]);
        } catch(IllegalArgumentException e) {
        	decode = Base64.getDecoder().decode(base[1]);
        }
        String json = new String(decode, StandardCharsets.UTF_8);
        return GSON.fromJson(json, JsonObject.class);
    }

    @Override
    public String toString() {
        return "ClientChainData12NetEase{" +
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
                '}';
    }
}
