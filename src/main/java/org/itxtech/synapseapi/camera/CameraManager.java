package org.itxtech.synapseapi.camera;

import org.itxtech.synapseapi.multiprotocol.AbstractProtocol;
import org.itxtech.synapseapi.multiprotocol.common.camera.CameraPreset;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

//TODO: mv
public class CameraManager {
    private static final CameraManager INSTANCE = new CameraManager();

    private final Map<String, CameraPreset> cameras = new TreeMap<>();
    private CameraPreset[] runtimeCameras;

    private CameraManager() {
        // register builtin cameras
        registerCamera(CameraPreset.FIRST_PERSON_PRESET);
        registerCamera(CameraPreset.FOLLOW_ORBIT_PRESET, AbstractProtocol.PROTOCOL_121_40);
        registerCamera(CameraPreset.FREE_PRESET);
        registerCamera(CameraPreset.THIRD_PERSON_PRESET);
        registerCamera(CameraPreset.THIRD_PERSON_FRONT_PRESET);
        rebuildRuntimeId();
    }

    public boolean registerCamera(CameraPreset camera) {
        return cameras.putIfAbsent(camera.name, camera) == null;
    }

    public boolean registerCamera(CameraPreset camera, AbstractProtocol protocol) {
        return false; //TODO: mv
    }

    public void rebuildRuntimeId() {
        int runtimeId = 0;
        List<CameraPreset> runtimeCameras = new ArrayList<>();
        for (CameraPreset preset : cameras.values()) {
            preset.runtimeId = runtimeId++;
            runtimeCameras.add(preset);
        }
        this.runtimeCameras = runtimeCameras.toArray(CameraPreset.EMPTY_PRESETS);
    }

    @Nullable
    public CameraPreset getCamera(int runtimeId) {
        if (runtimeId < 0 || runtimeId >= runtimeCameras.length) {
            return null;
        }
        return runtimeCameras[runtimeId];
    }

    @Nullable
    public CameraPreset getCamera(String name) {
        return cameras.get(name);
    }

    public CameraPreset[] getCameras() {
        return runtimeCameras;
    }

    public static void init() {
    }

    public static CameraManager getInstance() {
        return INSTANCE;
    }
}
