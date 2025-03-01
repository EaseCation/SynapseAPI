package org.itxtech.synapseapi.multiprotocol.protocol12160.protocol;

import cn.nukkit.network.protocol.ProtocolInfo;
import cn.nukkit.utils.BinaryStream;
import lombok.ToString;
import org.itxtech.synapseapi.multiprotocol.common.camera.CameraAimAssistCategories.Category;
import org.itxtech.synapseapi.multiprotocol.common.camera.CameraAimAssistCategories.Category.Priority;
import org.itxtech.synapseapi.multiprotocol.common.camera.CameraAimAssistPreset;
import org.itxtech.synapseapi.multiprotocol.common.camera.CameraAimAssistPreset.ItemSetting;

@ToString
public class CameraAimAssistPresetsPacket12160 extends Packet12160 {
    public static final int NETWORK_ID = ProtocolInfo.CAMERA_AIM_ASSIST_PRESETS_PACKET;

    public static final int OPERATION_SET = 0;
    public static final int OPERATION_ADD_TO_EXISTING = 1;

    public static final Category[] DEFAULT_CATEGORIES = new Category[]{
            new Category("minecraft:bucket", Priority.EMPTY, new Priority[]{
                    new Priority("minecraft:cauldron", 60),
                    new Priority("minecraft:water", 60),
                    new Priority("minecraft:lava", 60),
            }, null, null),
            new Category("minecraft:empty_hand", Priority.EMPTY, new Priority[]{
                    new Priority("minecraft:oak_log", 60),
                    new Priority("minecraft:birch_log", 60),
                    new Priority("minecraft:spruce_log", 60),
                    new Priority("minecraft:jungle_log", 60),
                    new Priority("minecraft:acacia_log", 60),
                    new Priority("minecraft:dark_oak_log", 60),
                    new Priority("minecraft:mangrove_log", 60),
                    new Priority("minecraft:cherry_log", 60),
            }, null, null),
            new Category("minecraft:default", Priority.EMPTY, new Priority[]{
                    new Priority("minecraft:lever", 60),
                    new Priority("minecraft:oak_button", 60),
                    new Priority("minecraft:birch_button", 60),
                    new Priority("minecraft:spruce_button", 60),
                    new Priority("minecraft:dark_oak_button", 60),
            }, null, null)
    };
    public static final CameraAimAssistPreset[] DEFAULT_PRESETS = new CameraAimAssistPreset[]{
            CameraAimAssistPreset.builder()
                    .identifier("minecraft:aim_assist_default")
                    .exclusions(new String[]{
                            "minecraft:bedrock",
                            "minecraft:arrow",
                    })
                    .liquidTargetingList(new String[]{
                            "minecraft:bucket",
                            "minecraft:oak_boat",
                            "minecraft:birch_boat",
                            "minecraft:spruce_boat",
                            "minecraft:jungle_boat",
                            "minecraft:acacia_boat",
                            "minecraft:dark_oak_boat",
                            "minecraft:mangrove_boat",
                            "minecraft:cherry_boat",
                            "minecraft:bamboo_boat",
                            "minecraft:oak_chest_boat",
                            "minecraft:birch_chest_boat",
                            "minecraft:spruce_chest_boat",
                            "minecraft:jungle_chest_boat",
                            "minecraft:acacia_chest_boat",
                            "minecraft:dark_oak_chest_boat",
                            "minecraft:mangrove_chest_boat",
                            "minecraft:cherry_chest_boat",
                            "minecraft:bamboo_chest_boat",
                    })
                    .itemSettings(new ItemSetting[]{
                            new ItemSetting("minecraft:bucket", "minecraft:bucket"),
                    })
                    .defaultItemSettings("minecraft:default")
                    .handSettings("minecraft:empty_hand")
                    .build()
    };

    public Category[] categories = new Category[0];
    public CameraAimAssistPreset[] presets = new CameraAimAssistPreset[0];
    public int operation = OPERATION_SET;

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
        for (Category category : categories) {
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
