package org.itxtech.synapseapi.network.protocol.mod.data;

import com.google.gson.JsonObject;
import it.unimi.dsi.fastutil.objects.Object2BooleanFunction;
import it.unimi.dsi.fastutil.objects.Object2IntFunction;
import lombok.ToString;

import javax.annotation.Nullable;
import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;

@ToString
public class ECModClientInfo {

    public final Integer time;
    public final String mod_version;
    public final JsonObject system_info;
    public final JsonObject mcp;
    public final JsonObject android;
    public final Integer game_type;
    @Nullable
    public final List<String> library_list;

    private transient Hack hack;
    private transient boolean validModVersion;
    private transient boolean emptyMacAddress;
    private transient boolean emptyFirstUdid;
    private transient String deviceBanReason;
    private transient boolean androidOs;
    private transient boolean neteaseChannel;
    private transient int libraryBlacklistIndex = -1;

    public ECModClientInfo(Integer time, String modVersion, JsonObject systemInfo, JsonObject mcp, JsonObject android, Integer gameType, @Nullable List<String> libraryList) {
        this.time = time;
        this.mod_version = modVersion;
        this.system_info = systemInfo;
        this.mcp = mcp;
        this.android = android;
        this.game_type = gameType;
        this.library_list = libraryList;
    }

    public Hack getHack() {
        return hack;
    }

    public boolean isValidModVersion() {
        return validModVersion;
    }

    public boolean isEmptyMacAddress() {
        return emptyMacAddress;
    }

    public boolean isEmptyFirstUdid() {
        return emptyFirstUdid;
    }

    public String getDeviceBanReason() {
        return deviceBanReason;
    }

    public boolean isAndroid() {
        return androidOs;
    }

    public boolean isNeteaseChannel() {
        return neteaseChannel;
    }

    public int getLibraryBlacklistIndex() {
        return libraryBlacklistIndex;
    }

    public void check() {
        if (!checkTimeValid()) {
            hack = Hack.TIME;
            return;
        }

        validModVersion = mod_version != null && Check.checkModVersion.getBoolean(mod_version);

        JsonObject obj = system_info;
        if ("windows".equals(obj.get("os_name").getAsString())) {
            hack = Hack.OS_NAME_WINDOWS;
            return;
        }
        if (obj.get("os_version").getAsString().isEmpty()) {
            hack = Hack.OS_VERSION;
            return;
        }
        if (obj.get("mac_address").getAsString().isEmpty()) {
            emptyMacAddress = true;
        }
        if (obj.get("first_udid").getAsString().isEmpty()) {
            emptyFirstUdid = true;
        }
        if (obj.get("machine_code").getAsString().isEmpty()) {
            hack = Hack.MACHINE_CODE;
            return;
        }
        String deviceId = obj.get("machine_code").getAsString();
        if (deviceId != null && !deviceId.isEmpty()) {
            deviceBanReason = Check.checkBannedDevice.apply(deviceId);
            if (deviceBanReason != null) {
                hack = Hack.MACHINE_BAN;
                return;
            }
        }
        if (obj.get("unisdk_ver").getAsString().isEmpty()) {
            hack = Hack.UNISDK_VER;
            return;
        }
        if (obj.get("app_channel").getAsString().isEmpty()) {
            hack = Hack.APP_CHANNEL;
            return;
        }
        if ("0".equals(obj.get("get_platform").getAsString())) {
            hack = Hack.GET_PLATFORM0;
            return;
        }
        if ("Win32".equals(obj.get("py_get_platform").getAsString())) {
            hack = Hack.PY_GET_PLATFORM_WIN32;
            return;
        }
        if (obj.get("windows_dir_exists").getAsBoolean()) {
            hack = Hack.WINDOWS_DIR_EXISTS;
            return;
        }

        if (mcp.has("is_loaded_invalid_mcp") && mcp.get("is_loaded_invalid_mcp").getAsBoolean()) {
            hack = Hack.LOADED_INVALID_MCP;
            return;
        }

        if (obj.has("os_name") && obj.get("os_name").getAsString().equals("android")) {
            if (android != null) {
                androidOs = true;
                if (android.has("is_use_shared_user_id") && android.get("is_use_shared_user_id").getAsBoolean()) {
                    hack = Hack.USE_SHARED_USER_ID;
                    return;
                }
                if (android.has("is_quark_func") && android.get("is_quark_func").getAsBoolean()) {
                    hack = Hack.QUARK_FUNC;
                    return;
                }
                if (android.has("is_vm") && android.get("is_vm").getAsBoolean()) {
                    hack = Hack.USE_VM;
                    return;
                }
                if (android.has("is_selinux") && !android.get("is_selinux").getAsBoolean()) {
                    hack = Hack.SELINUX;
                    return;
                }
            }
            if (library_list != null && obj.get("app_channel").getAsString().equals("netease")) {
                neteaseChannel = true;
                libraryBlacklistIndex = Check.checkLibraries.getInt(library_list);
                if (libraryBlacklistIndex != -1) {
                    hack = Hack.LIBRARY_BLACKLIST;
                }
            }
        }
    }

    boolean checkTimeValid() {
        Instant now = Instant.now();
        int nowSecond = (int) now.getEpochSecond();
        return time != null && Math.abs(time - nowSecond) < 60 * 60;
    }

    public static class Check {
        private static Object2BooleanFunction<String> checkModVersion = modVersion -> true;
        private static Function<String, String> checkBannedDevice = machineCode -> null;
        private static Object2IntFunction<List<String>> checkLibraries = libraries -> -1;

        public static void setCheckModVersion(Object2BooleanFunction<String> method) {
            Objects.requireNonNull(method, "method");
            checkModVersion = method;
        }

        public static void setCheckBannedDevice(Function<String, String> method) {
            Objects.requireNonNull(method, "method");
            checkBannedDevice = method;
        }

        public static void setCheckLibraries(Object2IntFunction<List<String>> method) {
            Objects.requireNonNull(method, "method");
            checkLibraries = method;
        }
    }

    public enum Hack {
        TIME,
        OS_NAME_WINDOWS,
        OS_VERSION,
        MACHINE_CODE,
        MACHINE_BAN,
        UNISDK_VER,
        APP_CHANNEL,
        GET_PLATFORM0,
        PY_GET_PLATFORM_WIN32,
        WINDOWS_DIR_EXISTS,
        LOADED_INVALID_MCP,
        USE_SHARED_USER_ID,
        QUARK_FUNC,
        USE_VM,
        SELINUX,
        LIBRARY_BLACKLIST,
    }
}
