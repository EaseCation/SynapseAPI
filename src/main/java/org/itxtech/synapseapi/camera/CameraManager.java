package org.itxtech.synapseapi.camera;

import cn.nukkit.network.protocol.BatchPacket;
import cn.nukkit.network.protocol.DataPacket;
import cn.nukkit.utils.BinaryStream;
import cn.nukkit.utils.Hash;
import lombok.extern.log4j.Log4j2;
import org.itxtech.synapseapi.SynapseAPI;
import org.itxtech.synapseapi.event.server.CameraRegistryChecksumChangedEvent;
import org.itxtech.synapseapi.multiprotocol.AbstractProtocol;
import org.itxtech.synapseapi.multiprotocol.common.camera.CameraPreset;
import org.itxtech.synapseapi.multiprotocol.protocol11970.protocol.CameraPresetsPacket11970;
import org.itxtech.synapseapi.multiprotocol.protocol12030.protocol.CameraPresetsPacket12030;
import org.itxtech.synapseapi.multiprotocol.protocol121130.protocol.CameraAimAssistPresetsPacket121130;
import org.itxtech.synapseapi.multiprotocol.protocol12120.protocol.CameraPresetsPacket12120;
import org.itxtech.synapseapi.multiprotocol.protocol12130.protocol.CameraPresetsPacket12130;
import org.itxtech.synapseapi.multiprotocol.protocol12140.protocol.CameraPresetsPacket12140;
import org.itxtech.synapseapi.multiprotocol.protocol12150.protocol.CameraAimAssistPresetsPacket12150;
import org.itxtech.synapseapi.multiprotocol.protocol12150.protocol.CameraPresetsPacket12150;
import org.itxtech.synapseapi.multiprotocol.protocol12160.protocol.CameraAimAssistPresetsPacket12160;
import org.itxtech.synapseapi.multiprotocol.protocol12160.protocol.CameraPresetsPacket12160;
import org.itxtech.synapseapi.multiprotocol.protocol12180.protocol.CameraPresetsPacket12180;
import org.itxtech.synapseapi.multiprotocol.protocol12190.protocol.CameraPresetsPacket12190;

import javax.annotation.Nullable;
import java.util.*;
import java.util.Map.Entry;
import java.util.zip.Deflater;

@Log4j2
public class CameraManager {
    private static final CameraManager INSTANCE = new CameraManager();

    private static long CAMERA_REGISTRY_CHECKSUM;

    private final Map<String, CameraPreset> cameras = new TreeMap<>();
    private final Map<String, CameraPreset> releaseCameras = new TreeMap<>();
    private final Map<AbstractProtocol, Map<String, CameraPreset>> experimentCameras = new EnumMap<>(AbstractProtocol.class);
    private CameraPreset[] runtimeCameras;
    private final Map<AbstractProtocol, CameraPreset[]> clientCameras = new EnumMap<>(AbstractProtocol.class);
    private final Map<AbstractProtocol, DataPacket> packets = new EnumMap<>(AbstractProtocol.class);

    private CameraManager() {
        // register builtin cameras
        registerCamera(CameraPreset.CONTROL_SCHEME_CAMERA_PRESET, AbstractProtocol.PROTOCOL_121_80, null);
        registerCamera(CameraPreset.FIRST_PERSON_PRESET);
        registerCamera(CameraPreset.FIXED_BOOM_PRESET, AbstractProtocol.PROTOCOL_121_50, AbstractProtocol.PROTOCOL_121_70);
        registerCamera(CameraPreset.FOLLOW_ORBIT_PRESET, AbstractProtocol.PROTOCOL_121_20, AbstractProtocol.PROTOCOL_121_40);
        registerCamera(CameraPreset.FREE_PRESET);
        registerCamera(CameraPreset.THIRD_PERSON_PRESET);
        registerCamera(CameraPreset.THIRD_PERSON_FRONT_PRESET);
        rebuildRuntimeId();
    }

    public boolean registerCamera(CameraPreset camera) {
        if (cameras.putIfAbsent(camera.name, camera) != null) {
            return false;
        }
        return releaseCameras.putIfAbsent(camera.name, camera) == null;
    }

    public boolean registerCamera(CameraPreset camera, AbstractProtocol experimentProtocol, @Nullable AbstractProtocol releaseProtocol) {
        if (experimentProtocol.getVersion().isAvailable()) {
            return registerCamera(camera);
        }
        if (cameras.putIfAbsent(camera.name, camera) != null) {
            return false;
        }
        return experimentCameras.computeIfAbsent(experimentProtocol, k -> new TreeMap<>())
                .putIfAbsent(camera.name, camera) == null;
    }

    public void rebuildRuntimeId() {
        int runtimeId = 0;
        List<CameraPreset> runtimeCameras = new ArrayList<>();
        List<CameraPreset> releaseCameras = new ArrayList<>();
        for (CameraPreset preset : this.releaseCameras.values()) {
            preset.runtimeId = runtimeId++;
            runtimeCameras.add(preset);
            releaseCameras.add(preset);
        }
        for (Map<String, CameraPreset> cameras : experimentCameras.values()) {
            for (CameraPreset preset : cameras.values()) {
                preset.runtimeId = runtimeId++;
                runtimeCameras.add(preset);
            }
        }
        for (AbstractProtocol protocol : AbstractProtocol.getValues()) {
            if (protocol.getProtocolStart() < AbstractProtocol.PROTOCOL_119_70.getProtocolStart()) {
                clientCameras.put(protocol, CameraPreset.EMPTY_PRESETS);
                continue;
            }
            List<CameraPreset> clientCameras = new ArrayList<>(releaseCameras);
            for (Entry<AbstractProtocol, Map<String, CameraPreset>> entry : experimentCameras.entrySet()) {
                if (entry.getKey().getProtocolStart() > protocol.getProtocolStart()) {
                    continue;
                }
                clientCameras.addAll(entry.getValue().values());
            }
            this.clientCameras.put(protocol, clientCameras.toArray(CameraPreset.EMPTY_PRESETS));
        }
        this.runtimeCameras = runtimeCameras.toArray(CameraPreset.EMPTY_PRESETS);

        cachePresetPackets();

        recalculateCameraRegistryChecksum();
    }

    private void cachePresetPackets() {
        log.debug("cache camera presets and camera aim assist presets...");

        for (AbstractProtocol protocol : AbstractProtocol.getValues()) {
            if (protocol.getProtocolStart() < AbstractProtocol.PROTOCOL_120.getProtocolStart()) {
                continue;
            }
            if (protocol.ordinal() < AbstractProtocol.FIRST_AVAILABLE_PROTOCOL.ordinal()) {
                // drop support for unavailable versions
                continue;
            }

            cachePresetPacket(protocol);
        }
    }

    private void cachePresetPacket(AbstractProtocol protocol) {
        List<DataPacket> packets = new ArrayList<>();

        if (protocol.getProtocolStart() >= AbstractProtocol.PROTOCOL_121_90.getProtocolStart()) {
            CameraPresetsPacket12190 packet = new CameraPresetsPacket12190();
            packet.presets = this.getCameras(protocol);
            packet.setHelper(protocol.getHelper());
            packets.add(packet);
        } else if (protocol.getProtocolStart() >= AbstractProtocol.PROTOCOL_121_80.getProtocolStart()) {
            CameraPresetsPacket12180 packet = new CameraPresetsPacket12180();
            packet.presets = this.getCameras(protocol);
            packet.setHelper(protocol.getHelper());
            packets.add(packet);
        } else if (protocol.getProtocolStart() >= AbstractProtocol.PROTOCOL_121_60.getProtocolStart()) {
            CameraPresetsPacket12160 packet = new CameraPresetsPacket12160();
            packet.presets = this.getCameras(protocol);
            packet.setHelper(protocol.getHelper());
            packets.add(packet);
        } else if (protocol.getProtocolStart() >= AbstractProtocol.PROTOCOL_121_50.getProtocolStart()) {
            CameraPresetsPacket12150 packet = new CameraPresetsPacket12150();
            packet.presets = this.getCameras(protocol);
            packet.setHelper(protocol.getHelper());
            packets.add(packet);
        } else if (protocol.getProtocolStart() >= AbstractProtocol.PROTOCOL_121_40.getProtocolStart()) {
            CameraPresetsPacket12140 packet = new CameraPresetsPacket12140();
            packet.presets = this.getCameras(protocol);
            packet.setHelper(protocol.getHelper());
            packets.add(packet);
        } else if (protocol.getProtocolStart() >= AbstractProtocol.PROTOCOL_121_30.getProtocolStart()) {
            CameraPresetsPacket12130 packet = new CameraPresetsPacket12130();
            packet.presets = this.getCameras(protocol);
            packet.setHelper(protocol.getHelper());
            packets.add(packet);
        } else if (protocol.getProtocolStart() >= AbstractProtocol.PROTOCOL_121_20.getProtocolStart()) {
            CameraPresetsPacket12120 packet = new CameraPresetsPacket12120();
            packet.presets = this.getCameras(protocol);
            packet.setHelper(protocol.getHelper());
            packets.add(packet);
        } else if (protocol.getProtocolStart() >= AbstractProtocol.PROTOCOL_120_30.getProtocolStart()) {
            CameraPresetsPacket12030 packet = new CameraPresetsPacket12030();
            packet.presets = this.getCameras(protocol);
            packet.setHelper(protocol.getHelper());
            packets.add(packet);
        } else if (protocol.getProtocolStart() >= AbstractProtocol.PROTOCOL_120.getProtocolStart()) {
            CameraPresetsPacket11970 packet = new CameraPresetsPacket11970();
            packet.presets = this.getCameras(protocol);
            packet.setHelper(protocol.getHelper());
            packets.add(packet);
        }

        if (protocol.getProtocolStart() >= AbstractProtocol.PROTOCOL_121_60.getProtocolStart()) {
            CameraAimAssistPresetsPacket121130 packet = new CameraAimAssistPresetsPacket121130();
            packet.categories = CameraAimAssistPresetsPacket121130.DEFAULT_CATEGORIES;
            packet.presets = CameraAimAssistPresetsPacket121130.DEFAULT_PRESETS;
            packet.operation = CameraAimAssistPresetsPacket121130.OPERATION_ADD_TO_EXISTING;
            packet.setHelper(protocol.getHelper());
            packets.add(packet);
        } else if (protocol.getProtocolStart() >= AbstractProtocol.PROTOCOL_121_60.getProtocolStart()) {
            CameraAimAssistPresetsPacket12160 packet = new CameraAimAssistPresetsPacket12160();
            packet.categories = CameraAimAssistPresetsPacket12160.DEFAULT_CATEGORIES;
            packet.presets = CameraAimAssistPresetsPacket12160.DEFAULT_PRESETS;
            packet.operation = CameraAimAssistPresetsPacket12160.OPERATION_ADD_TO_EXISTING;
            packet.setHelper(protocol.getHelper());
            packets.add(packet);
        } else if (protocol.getProtocolStart() >= AbstractProtocol.PROTOCOL_121_50.getProtocolStart()) {
            CameraAimAssistPresetsPacket12150 packet = new CameraAimAssistPresetsPacket12150();
            packet.categories = CameraAimAssistPresetsPacket12150.DEFAULT_CATEGORIES;
            packet.presets = CameraAimAssistPresetsPacket12150.DEFAULT_PRESETS;
            packet.setHelper(protocol.getHelper());
            packets.add(packet);
        }

        if (packets.isEmpty()) {
            return;
        }

        BatchPacket batch = BatchPacket.compress(protocol.getCompressor(), Deflater.BEST_COMPRESSION, packets.toArray(packets.toArray(new DataPacket[0])));
        this.packets.put(protocol, batch);
    }

    private void recalculateCameraRegistryChecksum() {
        BinaryStream stream = new BinaryStream();
        for (CameraPreset camera : runtimeCameras) {
            stream.putString(camera.name);
        }

        long newChecksum = Hash.xxh64(stream.getBuffer());
        if (CAMERA_REGISTRY_CHECKSUM == newChecksum) {
            return;
        }
        CAMERA_REGISTRY_CHECKSUM = newChecksum;

        new CameraRegistryChecksumChangedEvent(newChecksum).call();

        SynapseAPI.getInstance().getLogger().debug("CameraRegistry checksum: {}", newChecksum);
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

    public CameraPreset[] getCameras(AbstractProtocol protocol) {
        return clientCameras.get(protocol);
    }

    @Nullable
    public DataPacket getPresetPackets(AbstractProtocol protocol) {
        return packets.get(protocol);
    }

    public static void init() {
    }

    public static long getCameraRegistryChecksum() {
        return CAMERA_REGISTRY_CHECKSUM;
    }

    public static CameraManager getInstance() {
        return INSTANCE;
    }
}
