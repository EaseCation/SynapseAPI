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
    public String getNetEasePlatform() {
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
    public String getCapeData() {
        return null;
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

    private String capeData;

    private BinaryStream bs = new BinaryStream();

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
    }

    private JsonObject decodeToken(String token) {
        String[] base = token.split("\\.", 4);
        if (base.length < 2) return null;
        byte[] decode = null;
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
                ", bs=" + bs +
                '}';
    }
}
