package org.itxtech.synapseapi.multiprotocol.protocol12.utils;

import cn.nukkit.utils.BinaryStream;
import cn.nukkit.utils.LoginChainData;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.itxtech.synapseapi.multiprotocol.protocol12.protocol.LoginPacket;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Objects;
import java.util.UUID;

/**
 * @author CreeperFace
 */
public final class ClientChainData12Urgency implements LoginChainData {
    private static final Gson GSON = new Gson();

    public static ClientChainData12Urgency of(byte[] buffer) {
        return new ClientChainData12Urgency(buffer);
    }

    public static ClientChainData12Urgency read(LoginPacket pk) {
        ClientChainData12Urgency data = of(pk.getBuffer());
        data.username = pk.username;
        data.clientUUID = pk.clientUUID;
        data.xuid = pk.xuid;
        return data;
    }

    public String getUsername() {
        return username;
    }

    public UUID getClientUUID() {
        return clientUUID;
    }

    public String getIdentityPublicKey() {
        return identityPublicKey;
    }

    public long getClientId() {
        return clientId;
    }

    public String getServerAddress() {
        return serverAddress;
    }

    @Override
    public String getNetEaseUID() {
        return "";
    }

    @Override
    public String getNetEaseSid() {
        return "";
    }

    @Override
    public String getNetEaseDataVersion() {
        return "";
    }

    @Override
    public String getNetEasePlatform() {
        return "";
    }

    @Override
    public String getNetEaseClientOsName() {
        return "";
    }

    @Override
    public String getNetEaseEnv() {
        return "";
    }

    @Override
    public String getNetEaseClientEngineVersion() {
        return "";
    }

    @Override
    public String getNetEaseClientPatchVersion() {
        return "";
    }

    @Override
    public String getNetEaseClientBit() {
        return "";
    }

    @Override
    public String getNetEaseGameType() {
        return "";
    }

    @Override
    public String getDeviceId() {
        return deviceId;
    }

    public String getDeviceModel() {
        return deviceModel;
    }

    public int getDeviceOS() {
        return deviceOS;
    }

    public String getGameVersion() {
        return gameVersion;
    }

    public int getGuiScale() {
        return guiScale;
    }

    public String getLanguageCode() {
        return languageCode;
    }

    public String getXUID() {
        return xuid;
    }

    public int getCurrentInputMode() {
        return currentInputMode;
    }

    @Override
    public void setCurrentInputMode(int mode) {
        this.currentInputMode = mode;
    }

    public int getDefaultInputMode() {
        return defaultInputMode;
    }

    public final static int UI_PROFILE_CLASSIC = 0;
    public final static int UI_PROFILE_POCKET = 1;

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
        return false;
    }

    @Override
    public String getNetEaseSkinIID() {
        return "";
    }

    @Override
    public int getNetEaseGrowthLevel() {
        return 0;
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

    public String getXuid() {
        return xuid;
    }

    ///////////////////////////////////////////////////////////////////////////
    // Override
    ///////////////////////////////////////////////////////////////////////////

    @Override
    public boolean equals(Object obj) {
        return obj instanceof ClientChainData12Urgency && Objects.equals(bs, ((ClientChainData12Urgency) obj).bs);
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

    private String capeData;

    private String viaProxyAuthToken;

    private final transient BinaryStream bs = new BinaryStream();

    private ClientChainData12Urgency(byte[] buffer) {
        bs.setBuffer(buffer, 0);
        decodeChainData();
        decodeSkinData();
    }

    private void decodeChainData() {
        new String(bs.get(bs.getLInt()), StandardCharsets.UTF_8);
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
        if (skinToken.has("CapeData")) this.capeData = skinToken.get("CapeData").getAsString();
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
        if (skinToken.has("ViaProxyAuthToken")) this.viaProxyAuthToken = skinToken.get("ViaProxyAuthToken").getAsString();
    }

    @Override
    public String getViaProxyAuthToken() {
        return viaProxyAuthToken;
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
        return GSON.fromJson(json, JsonObject.class);
    }

    @Override
    public String toString() {
        return "ClientChainData12Urgency{" +
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
                '}';
    }
}
