package org.itxtech.synapseapi.multiprotocol.protocol12160.protocol;

import cn.nukkit.network.protocol.ProtocolInfo;
import cn.nukkit.utils.BinaryStream;
import lombok.ToString;
import org.itxtech.synapseapi.multiprotocol.common.camera.CameraAimAssistCategories;
import org.itxtech.synapseapi.multiprotocol.common.camera.CameraAimAssistCategories.Category;
import org.itxtech.synapseapi.multiprotocol.common.camera.CameraAimAssistCategories.Category.Priority;
import org.itxtech.synapseapi.multiprotocol.common.camera.CameraAimAssistPreset;
import org.itxtech.synapseapi.multiprotocol.common.camera.CameraAimAssistPreset.ItemSetting;

@ToString
public class CameraAimAssistPresetsPacket12160 extends Packet12160 {
    public static final int NETWORK_ID = ProtocolInfo.CAMERA_AIM_ASSIST_PRESETS_PACKET;

    public CameraAimAssistCategories[] categories = new CameraAimAssistCategories[0];
    public CameraAimAssistPreset[] presets = new CameraAimAssistPreset[0];
    public int operation;

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
        for (CameraAimAssistCategories definition : categories) {
            putString(definition.identifier);

            putUnsignedVarInt(definition.categories.length);
            for (Category category : definition.categories) {
                putString(category.name);

                putUnsignedVarInt(category.entityPriorities.length);
                for (Priority priority : category.entityPriorities) {
                    putString(priority.identifier);
                    putLInt(priority.priority);
                }

                putUnsignedVarInt(category.blockPriorities.length);
                for (Priority priority : category.blockPriorities) {
                    putString(priority.identifier);
                    putLInt(priority.priority);
                }

                putOptional(category.defaultEntityPriority, BinaryStream::putLInt);
                putOptional(category.defaultBlockPriority, BinaryStream::putLInt);
            }
        }

        putUnsignedVarInt(presets.length);
        for (CameraAimAssistPreset preset : presets) {
            putString(preset.identifier);

            putUnsignedVarInt(preset.exclusions.length);
            for (String exclusion : preset.exclusions) {
                putString(exclusion);
            }

            putUnsignedVarInt(preset.liquidTargetingList.length);
            for (String liquidTargeting : preset.liquidTargetingList) {
                putString(liquidTargeting);
            }

            putUnsignedVarInt(preset.itemSettings.length);
            for (ItemSetting itemSetting : preset.itemSettings) {
                putString(itemSetting.identifier);
                putString(itemSetting.category);
            }

            putOptional(preset.defaultItemSettings, BinaryStream::putString);
            putOptional(preset.handSettings, BinaryStream::putString);
        }

        putByte(operation);
    }
}
