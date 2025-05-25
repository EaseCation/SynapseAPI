package org.itxtech.synapseapi.utils;

import cn.nukkit.utils.BinaryStream;
import cn.nukkit.utils.LoginChainData;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSObject;
import com.nimbusds.jose.crypto.ECDSAVerifier;
import lombok.extern.log4j.Log4j2;
import org.itxtech.synapseapi.multiprotocol.AbstractProtocol;
import org.itxtech.synapseapi.multiprotocol.protocol12.protocol.LoginPacket;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.interfaces.ECPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.*;

@Log4j2
public final class ClientChainDataXbox implements LoginChainData {
    private static final Gson GSON = new Gson();

    @Deprecated
    private static final String MOJANG_PUBLIC_KEY_BASE64 = "MHYwEAYHKoZIzj0CAQYFK4EEACIDYgAE8ELkixyLcwlZryUQcu1TvPOmI2B7vX83ndnWRUaXm74wFfa5f/lwQNTfrLVHa2PmenpGI6JhIMUJaWZrjmMj90NoKNFSNBuKdm8rYiXsfaz3K36x/1U26HpG0ZxK/V1V";
//    private static final PublicKey MOJANG_PUBLIC_KEY;
    private static final String NEW_MOJANG_PUBLIC_KEY_BASE64 = "MHYwEAYHKoZIzj0CAQYFK4EEACIDYgAECRXueJeTDqNRRgJi/vlRufByu/2G0i2Ebt6YMar5QX/R0DIIyrJMcUpruK4QveTfJSTp3Shlq4Gk34cD/4GUWwkv0DVuzeuB+tXija7HBxii03NHDbPAD0AKnLr2wdAp";
    private static final PublicKey NEW_MOJANG_PUBLIC_KEY;
    private static final boolean xboxAuth;

    static {
        boolean notAvailable = false;

/*
        PublicKey key;
        try {
            key = generateKey(MOJANG_PUBLIC_KEY_BASE64);
        } catch (InvalidKeySpecException | NoSuchAlgorithmException e) {
            key = null;
            notAvailable = true;
            log.warn(e);
        }
        MOJANG_PUBLIC_KEY = key;
*/

        PublicKey keyNew;
        try {
            keyNew = generateKey(NEW_MOJANG_PUBLIC_KEY_BASE64);
        } catch (InvalidKeySpecException | NoSuchAlgorithmException e) {
            keyNew = null;
            notAvailable = true;
            log.warn(e);
        }
        NEW_MOJANG_PUBLIC_KEY = keyNew;

        xboxAuth = !notAvailable;
    }

    public static ClientChainDataXbox of(byte[] buffer, int protocol) {
        return new ClientChainDataXbox(buffer, protocol);
    }

    public static ClientChainDataXbox read(LoginPacket pk) {
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

//    @Override
    public boolean isXboxAuthed() {
        return xboxAuthed;
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

//    @Override
    public String[] getOriginChainArr() {
        return originChainArr;
    }

    ///////////////////////////////////////////////////////////////////////////
    // Override
    ///////////////////////////////////////////////////////////////////////////

    @Override
    public boolean equals(Object obj) {
        return obj instanceof ClientChainDataXbox && Objects.equals(bs, ((ClientChainDataXbox) obj).bs);
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
    private boolean xboxAuthed = true;
    private String[] originChainArr;

    private transient final BinaryStream bs = new BinaryStream();

    private ClientChainDataXbox(byte[] buffer, int protocol) {
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
            if (map.isEmpty() || (chains = map.get("chain")) == null || chains.isEmpty()) {
                return;
            }
        } else {
            Object chain = root.get("chain");
            if (!(chain instanceof List list) || list.isEmpty()) {
                return;
            }
            chains = (List<String>) chain;
        }

        if (xboxAuth) {
            // Validate keys
            try {
                xboxAuthed = verifyChain(chains);
            } catch (Exception e) {
                xboxAuthed = false;
            }
        }

        this.originChainArr = chains.toArray(new String[0]);

        for (String c : chains) {
            JsonObject chainMap = decodeToken(c);
            if (chainMap == null) {
                continue;
            }
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
            if (chainMap.has("identityPublicKey")) {
                this.identityPublicKey = chainMap.get("identityPublicKey").getAsString();
            }
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

    private static ECPublicKey generateKey(String base64) throws NoSuchAlgorithmException, InvalidKeySpecException {
        return (ECPublicKey) KeyFactory.getInstance("EC").generatePublic(new X509EncodedKeySpec(Base64.getDecoder().decode(base64)));
    }

    private static boolean verifyChain(List<String> chains) throws Exception {
        long now = System.currentTimeMillis() / 1000;
        ECPublicKey lastKey = null;
        boolean mojangKeyVerified = false;

        Iterator<String> iterator = chains.iterator();
        while (iterator.hasNext()) {
            JWSObject jws = JWSObject.parse(iterator.next());

            URI x5u = jws.getHeader().getX509CertURL();
            if (x5u == null) {
                return false;
            }

            ECPublicKey expectedKey = generateKey(x5u.toString());
            // First key is self-signed
            if (lastKey == null) {
                lastKey = expectedKey;
            } else if (!lastKey.equals(expectedKey)) {
                return false;
            }

            if (!verify(lastKey, jws)) {
                return false;
            }

            if (mojangKeyVerified) {
                return !iterator.hasNext();
            }

            if (lastKey.equals(NEW_MOJANG_PUBLIC_KEY)/* || lastKey.equals(MOJANG_PUBLIC_KEY)*/) {
                mojangKeyVerified = true;
            }

            Map<String, Object> payload = jws.getPayload().toJSONObject();

            Object nbf = payload.get("nbf");
            if (!(nbf instanceof Number)) {
//                throw new IllegalStateException("Invalid nbf");
                return false;
            }
            if (((Number) nbf).longValue() > now) {
                // premature
                return false;
            }

            Object exp = payload.get("exp");
            if (!(exp instanceof Number)) {
//                throw new IllegalStateException("Invalid exp");
                return false;
            }
            if (((Number) exp).longValue() < now) {
                // expire
                return false;
            }

            Object base64key = payload.get("identityPublicKey");
            if (!(base64key instanceof String)) {
//                throw new IllegalStateException("No key found");
                return false;
            }
            lastKey = generateKey((String) base64key);
        }
        return mojangKeyVerified;
    }

    private static boolean verify(ECPublicKey key, JWSObject object) throws JOSEException {
        return object.verify(new ECDSAVerifier(key));
    }
}
