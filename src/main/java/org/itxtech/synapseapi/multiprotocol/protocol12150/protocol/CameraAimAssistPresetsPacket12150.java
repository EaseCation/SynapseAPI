package org.itxtech.synapseapi.multiprotocol.protocol12150.protocol;

import cn.nukkit.network.protocol.ProtocolInfo;
import cn.nukkit.utils.BinaryStream;
import lombok.ToString;
import org.itxtech.synapseapi.multiprotocol.common.camera.CameraAimAssistCategoriesDefinition;
import org.itxtech.synapseapi.multiprotocol.common.camera.CameraAimAssistCategoryDefinition;
import org.itxtech.synapseapi.multiprotocol.common.camera.CameraAimAssistCategoryPriorities;
import org.itxtech.synapseapi.multiprotocol.common.camera.CameraAimAssistPresetDefinition;

@ToString
public class CameraAimAssistPresetsPacket12150 extends Packet12150 {
    public static final int NETWORK_ID = ProtocolInfo.CAMERA_AIM_ASSIST_PRESETS_PACKET;

    public CameraAimAssistCategoriesDefinition[] categories = new CameraAimAssistCategoriesDefinition[0];
    public CameraAimAssistPresetDefinition[] presets = new CameraAimAssistPresetDefinition[0];

    @Override
    public int pid() {
        return NETWORK_ID;
    }

    @Override
    public void decode() {
    }

    @Override
    public void encode() {
        reset();

        putUnsignedVarInt(categories.length);
        for (CameraAimAssistCategoriesDefinition definition : categories) {
            putString(definition.identifier);

            putUnsignedVarInt(definition.categories.length);
            for (CameraAimAssistCategoryDefinition category : definition.categories) {
                putString(category.name);

                putUnsignedVarInt(category.priorities.length);
                for (CameraAimAssistCategoryPriorities priority : category.priorities) {
                    putByte(priority.entities);
                    putByte(priority.blocks);
                    putByte(priority.entityDefault);
                    putByte(priority.blockDefault);
                }
            }
        }

        putUnsignedVarInt(presets.length);
        for (CameraAimAssistPresetDefinition preset : presets) {
            putString(preset.identifier);
            putString(preset.categories);

            putUnsignedVarInt(preset.exclusions.length);
            for (String exclusion : preset.exclusions) {
                putString(exclusion);
            }

            putUnsignedVarInt(preset.liquidTargetingList.length);
            for (String liquidTargeting : preset.liquidTargetingList) {
                putString(liquidTargeting);
            }

            putUnsignedVarInt(preset.itemSettings.length);
            for (String itemSetting : preset.itemSettings) {
                putString(itemSetting);
            }

            putOptional(preset.defaultItemSettings, BinaryStream::putString);
            putOptional(preset.handSettings, BinaryStream::putString);
        }
    }
}
