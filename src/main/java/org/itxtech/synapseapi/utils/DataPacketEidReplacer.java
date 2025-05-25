package org.itxtech.synapseapi.utils;

import cn.nukkit.entity.Entity;
import cn.nukkit.entity.data.EntityData;
import cn.nukkit.entity.data.EntityMetadata;
import cn.nukkit.entity.data.LongEntityData;
import cn.nukkit.network.protocol.*;
import cn.nukkit.network.protocol.PlayerListPacket.Entry;
import org.itxtech.synapseapi.multiprotocol.common.camera.CameraTargetInstruction;
import org.itxtech.synapseapi.multiprotocol.protocol113.protocol.MoveEntityDeltaPacket113;
import org.itxtech.synapseapi.multiprotocol.protocol116100.protocol.AnimateEntityPacket116100;
import org.itxtech.synapseapi.multiprotocol.protocol116100.protocol.EntityEventPacket116100;
import org.itxtech.synapseapi.multiprotocol.protocol116100.protocol.MotionPredictionHintsPacket116100;
import org.itxtech.synapseapi.multiprotocol.protocol116100.protocol.MoveEntityDeltaPacket116100;
import org.itxtech.synapseapi.multiprotocol.protocol116100ne.protocol.MovePlayerPacket116100NE;
import org.itxtech.synapseapi.multiprotocol.protocol11730.protocol.AnimateEntityPacket11730;
import org.itxtech.synapseapi.multiprotocol.protocol11830.protocol.SpawnParticleEffectPacket11830;
import org.itxtech.synapseapi.multiprotocol.protocol119.protocol.PlayerActionPacket119;
import org.itxtech.synapseapi.multiprotocol.protocol11910.protocol.UpdateAbilitiesPacket11910;
import org.itxtech.synapseapi.multiprotocol.protocol12070.protocol.MobEffectPacket12070;
import org.itxtech.synapseapi.multiprotocol.protocol12070.protocol.SetEntityMotionPacket12070;
import org.itxtech.synapseapi.multiprotocol.protocol12120.protocol.CameraInstructionPacket12120;
import org.itxtech.synapseapi.multiprotocol.protocol12120.protocol.MobArmorEquipmentPacket12120;
import org.itxtech.synapseapi.multiprotocol.protocol12140.protocol.CameraInstructionPacket12140;
import org.itxtech.synapseapi.multiprotocol.protocol12140.protocol.MobEffectPacket12140;
import org.itxtech.synapseapi.multiprotocol.protocol12140.protocol.MovementEffectPacket12140;
import org.itxtech.synapseapi.multiprotocol.protocol12160.protocol.BossEventPacket12160;
import org.itxtech.synapseapi.multiprotocol.protocol12170.protocol.LevelSoundEventPacketV312170;
import org.itxtech.synapseapi.multiprotocol.protocol12190.protocol.CameraInstructionPacket12190;
import org.itxtech.synapseapi.multiprotocol.protocol14.protocol.PlayerActionPacket14;
import org.itxtech.synapseapi.multiprotocol.protocol15.protocol.MoveEntityDeltaPacket;
import org.itxtech.synapseapi.multiprotocol.protocol18.protocol.SpawnParticleEffectPacket18;

import javax.annotation.Nullable;

/**
 * DataPacketEidReplacer
 * ===============
 * author: boybook
 * EaseCation Network Project
 * codefuncore
 * ===============
 */
public class DataPacketEidReplacer {

    public static DataPacket replace(DataPacket pk, long from, long to) {
        DataPacket packet = pk.clone();
        boolean change = true;

        switch (packet.pid()) {
            case ProtocolInfo.ANIMATE_PACKET:
                if (((AnimatePacket) packet).eid == from) ((AnimatePacket) packet).eid = to;
                break;
            case ProtocolInfo.TAKE_ITEM_ACTOR_PACKET:
                if (((TakeItemEntityPacket) packet).entityId == from) ((TakeItemEntityPacket) packet).entityId = to;
                break;
            case ProtocolInfo.SET_ACTOR_MOTION_PACKET:
                if (packet instanceof SetEntityMotionPacket) {
                    if (((SetEntityMotionPacket) packet).eid == from) ((SetEntityMotionPacket) packet).eid = to;
                } else if (packet instanceof SetEntityMotionPacket12070) {
                    if (((SetEntityMotionPacket12070) packet).eid == from) ((SetEntityMotionPacket12070) packet).eid = to;
                }
                break;
            case ProtocolInfo.SET_ACTOR_LINK_PACKET:
                if (((SetEntityLinkPacket) packet).vehicleUniqueId == from)
                    ((SetEntityLinkPacket) packet).vehicleUniqueId = to;
                if (((SetEntityLinkPacket) packet).riderUniqueId == from)
                    ((SetEntityLinkPacket) packet).riderUniqueId = to;
                break;
            case ProtocolInfo.SET_ACTOR_DATA_PACKET:
                if (packet instanceof SetEntityDataPacket dp) {
                    if (dp.eid == from) {
                        dp.eid = to;
                    }
                    EntityMetadata newMetadata = replaceEntityMetadata(dp.metadata, from, to);
                    if (newMetadata != null) {
                        dp.metadata = newMetadata;
                    }
                }
                break;
            case ProtocolInfo.UPDATE_ATTRIBUTES_PACKET:
                if (((UpdateAttributesPacket) packet).entityId == from) ((UpdateAttributesPacket) packet).entityId = to;
                break;
            case ProtocolInfo.ACTOR_EVENT_PACKET:
                if (packet instanceof EntityEventPacket116100 dp) {
                    if (dp.eid == from) {
                        dp.eid = to;
                    }
                } else if (packet instanceof EntityEventPacket dp) {
                    if (dp.eid == from) {
                        dp.eid = to;
                    }
                }
                break;
            case ProtocolInfo.MOVE_PLAYER_PACKET:
                if (packet instanceof MovePlayerPacket) {
                    if (((MovePlayerPacket) packet).eid == from) ((MovePlayerPacket) packet).eid = to;
                } else if (packet instanceof MovePlayerPacket116100NE) {
                    if (((MovePlayerPacket116100NE) packet).eid == from) ((MovePlayerPacket116100NE) packet).eid = to;
                }
                break;
            case ProtocolInfo.MOB_EQUIPMENT_PACKET:
                if (((MobEquipmentPacket) packet).eid == from) ((MobEquipmentPacket) packet).eid = to;
                break;
            case ProtocolInfo.MOB_EFFECT_PACKET:
                if (packet instanceof MobEffectPacket) {
                    if (((MobEffectPacket) packet).eid == from) ((MobEffectPacket) packet).eid = to;
                } else if (packet instanceof MobEffectPacket12070) {
                    if (((MobEffectPacket12070) packet).eid == from) ((MobEffectPacket12070) packet).eid = to;
                } else if (packet instanceof MobEffectPacket12140 dp) {
                    if (dp.eid == from) {
                        dp.eid = to;
                    }
                }
                break;
            case ProtocolInfo.MOVE_ACTOR_ABSOLUTE_PACKET:
                if (((MoveEntityPacket) packet).eid == from) ((MoveEntityPacket) packet).eid = to;
                break;
            case ProtocolInfo.MOB_ARMOR_EQUIPMENT_PACKET:
                if (packet instanceof MobArmorEquipmentPacket dp) {
                    if (dp.eid == from) {
                        dp.eid = to;
                    }
                } else if (packet instanceof MobArmorEquipmentPacket12120 dp) {
                    if (dp.eid == from) {
                        dp.eid = to;
                    }
                }
                break;
            case ProtocolInfo.PLAYER_LIST_PACKET:
                if (packet instanceof PlayerListPacket dp) {
                    boolean cloned = false;
                    for (int i = 0; i < dp.entries.length; i++) {
                        if (dp.entries[i].entityId == from) {
                            if (!cloned) {
                                cloned = true;
                                dp.entries = dp.entries.clone();
                            }
                            Entry entry = dp.entries[i].copy();
                            entry.entityId = to;
                            dp.entries[i] = entry;
                        }
                    }
                }
                break;
            case ProtocolInfo.BOSS_EVENT_PACKET:
                if (packet instanceof BossEventPacket12160 dp) {
                    if (dp.bossEid == from) {
                        dp.bossEid = to;
                    }
                } else if (packet instanceof BossEventPacket dp) {
                    if (dp.bossEid == from) {
                        dp.bossEid = to;
                    }
                }
                break;
            case ProtocolInfo.ADVENTURE_SETTINGS_PACKET:
                if (((AdventureSettingsPacket) packet).entityUniqueId == from) ((AdventureSettingsPacket) packet).entityUniqueId = to;
                break;
            case ProtocolInfo.UPDATE_EQUIPMENT_PACKET:
                if (((UpdateEquipmentPacket) packet).eid == from) ((UpdateEquipmentPacket) packet).eid = to;
                break;
            case ProtocolInfo.CLIENTBOUND_MAP_ITEM_DATA_PACKET:
                if (packet instanceof ClientboundMapItemDataPacket dp) {
                    boolean cloned = false;
                    for (int i = 0; i < dp.trackedEntities.length; i++) {
                        if (dp.trackedEntities[i].entityUniqueId == from) {
                            if (!cloned) {
                                cloned = true;
                                dp.trackedEntities = dp.trackedEntities.clone();
                            }
                            dp.trackedEntities[i].entityUniqueId = to;
                        }
                    }
                }
                break;
            case ProtocolInfo.ADD_ACTOR_PACKET:
                if (packet instanceof AddEntityPacket dp) {
                    EntityMetadata newMetadata = replaceEntityMetadata(dp.metadata, from, to);
                    if (newMetadata != null) {
                        dp.metadata = newMetadata;
                    }
                }
                break;
            case ProtocolInfo.ADD_ITEM_ACTOR_PACKET:
                if (packet instanceof AddItemEntityPacket dp) {
                    EntityMetadata newMetadata = replaceEntityMetadata(dp.metadata, from, to);
                    if (newMetadata != null) {
                        dp.metadata = newMetadata;
                    }
                }
                break;
            case ProtocolInfo.ADD_PLAYER_PACKET:
                if (packet instanceof AddPlayerPacket dp) {
                    EntityMetadata newMetadata = replaceEntityMetadata(dp.metadata, from, to);
                    if (newMetadata != null) {
                        dp.metadata = newMetadata;
                    }
                }
                break;
            case ProtocolInfo.CONTAINER_OPEN_PACKET:
                if (((ContainerOpenPacket) packet).entityId == from) {
                    ((ContainerOpenPacket) packet).entityId = to;
                }
                break;
            case ProtocolInfo.SPAWN_PARTICLE_EFFECT_PACKET:
                if (packet instanceof SpawnParticleEffectPacket11830) {
                    if (((SpawnParticleEffectPacket11830) packet).uniqueEntityId == from) {
                        ((SpawnParticleEffectPacket11830) packet).uniqueEntityId = to;
                    }
                } else if (packet instanceof SpawnParticleEffectPacket18) {
                    if (((SpawnParticleEffectPacket18) packet).uniqueEntityId == from) {
                        ((SpawnParticleEffectPacket18) packet).uniqueEntityId = to;
                    }
                } else if (packet instanceof SpawnParticleEffectPacket) {
                    if (((SpawnParticleEffectPacket) packet).uniqueEntityId == from) {
                        ((SpawnParticleEffectPacket) packet).uniqueEntityId = to;
                    }
                }
                break;
            case ProtocolInfo.ANIMATE_ENTITY_PACKET:
                if (packet instanceof AnimateEntityPacket116100 dp) {
                    boolean cloned = false;
                    for (int i = 0; i < dp.entityRuntimeIds.length; i++) {
                        if (dp.entityRuntimeIds[i] == from) {
                            if (!cloned) {
                                cloned = true;
                                dp.entityRuntimeIds = dp.entityRuntimeIds.clone();
                            }
                            dp.entityRuntimeIds[i] = to;
                        }
                    }
                } else if (packet instanceof AnimateEntityPacket11730 dp) {
                    boolean cloned = false;
                    for (int i = 0; i < dp.entityRuntimeIds.length; i++) {
                        if (dp.entityRuntimeIds[i] == from) {
                            if (!cloned) {
                                cloned = true;
                                dp.entityRuntimeIds = dp.entityRuntimeIds.clone();
                            }
                            dp.entityRuntimeIds[i] = to;
                        }
                    }
                }
                break;
            case ProtocolInfo.MOVE_ACTOR_DELTA_PACKET:
                if (packet instanceof MoveEntityDeltaPacket116100) {
                    if (((MoveEntityDeltaPacket116100) packet).entityRuntimeId == from) {
                        ((MoveEntityDeltaPacket116100) packet).entityRuntimeId = to;
                    }
                } else if (packet instanceof MoveEntityDeltaPacket113) {
                    if (((MoveEntityDeltaPacket113) packet).entityRuntimeId == from) {
                        ((MoveEntityDeltaPacket113) packet).entityRuntimeId = to;
                    }
                } else if (packet instanceof MoveEntityDeltaPacket) {
                    if (((MoveEntityDeltaPacket) packet).entityRuntimeId == from) {
                        ((MoveEntityDeltaPacket) packet).entityRuntimeId = to;
                    }
                }
                break;
            case ProtocolInfo.UPDATE_ABILITIES_PACKET:
                if (((UpdateAbilitiesPacket11910) packet).entityUniqueId == from) {
                    ((UpdateAbilitiesPacket11910) packet).entityUniqueId = to;
                }
                break;
            case ProtocolInfo.PLAYER_ACTION_PACKET:
                if (packet instanceof PlayerActionPacket119) {
                    if (((PlayerActionPacket119) packet).entityId == from) {
                        ((PlayerActionPacket119) packet).entityId = to;
                    }
                } else if (packet instanceof PlayerActionPacket14) {
                    if (((PlayerActionPacket14) packet).entityId == from) {
                        ((PlayerActionPacket14) packet).entityId = to;
                    }
                } else if (packet instanceof PlayerActionPacket) {
                    if (((PlayerActionPacket) packet).entityId == from) {
                        ((PlayerActionPacket) packet).entityId = to;
                    }
                }
                break;
            case ProtocolInfo.MOTION_PREDICTION_HINTS_PACKET:
                if (((MotionPredictionHintsPacket116100) packet).entityRuntimeId == from) {
                    ((MotionPredictionHintsPacket116100) packet).entityRuntimeId = to;
                }
                break;
            case ProtocolInfo.CAMERA_INSTRUCTION_PACKET:
                if (packet instanceof CameraInstructionPacket12190 dp) {
                    CameraTargetInstruction target = dp.target;
                    if (target != null && target.entityId == from) {
                        CameraTargetInstruction copy = target.clone();
                        copy.entityId = to;
                        dp.target = copy;
                    }
                } else if (packet instanceof CameraInstructionPacket12140 dp) {
                    CameraTargetInstruction target = dp.target;
                    if (target != null && target.entityId == from) {
                        CameraTargetInstruction copy = target.clone();
                        copy.entityId = to;
                        dp.target = copy;
                    }
                } else if (packet instanceof CameraInstructionPacket12120 dp) {
                    CameraTargetInstruction target = dp.target;
                    if (target != null && target.entityId == from) {
                        CameraTargetInstruction copy = target.clone();
                        copy.entityId = to;
                        dp.target = copy;
                    }
                }
                break;
            case ProtocolInfo.MOVEMENT_EFFECT_PACKET:
                if (packet instanceof MovementEffectPacket12140 dp) {
                    if (dp.entityRuntimeId == from) {
                        dp.entityRuntimeId = to;
                    }
                }
                break;
            case ProtocolInfo.LEVEL_SOUND_EVENT_PACKET:
                if (packet instanceof LevelSoundEventPacketV312170 dp) {
                    if (dp.entityUniqueId == from) {
                        dp.entityUniqueId = to;
                    }
                } else if (packet instanceof LevelSoundEventPacket dp) {
                    if (dp.entityUniqueId == from) {
                        dp.entityUniqueId = to;
                    }
                }
                break;
            default:
                change = false;
                break;
        }

        if (change) {
            packet.isEncoded = false;
        }

        return packet;
    }

    public static DataPacket replaceBack(DataPacket packet, long from, long to) {
        switch (packet.pid()) {
            case ProtocolInfo.ACTOR_EVENT_PACKET:
                if (packet instanceof EntityEventPacket116100 pk) {
                    if (pk.eid == from) {
                        pk.eid = to;
                    }
                } else if (packet instanceof EntityEventPacket pk) {
                    if (pk.eid == from) {
                        pk.eid = to;
                    }
                }
                break;
        }

        return packet;
    }

    @Nullable
    private static EntityMetadata replaceEntityMetadata(EntityMetadata metadata, long from, long to) {
        EntityMetadata newMetadata = null;
        newMetadata = replaceEntityMetadata(metadata, newMetadata, Entity.DATA_OWNER_EID, from, to);
        newMetadata = replaceEntityMetadata(metadata, newMetadata, Entity.DATA_TARGET_EID, from, to);
        newMetadata = replaceEntityMetadata(metadata, newMetadata, Entity.DATA_ARROW_SHOOTER_EID, from, to);
        newMetadata = replaceEntityMetadata(metadata, newMetadata, Entity.DATA_FIREWORK_ATTACHED_EID, from, to);
        newMetadata = replaceEntityMetadata(metadata, newMetadata, Entity.DATA_LEAD_HOLDER_EID, from, to);
        newMetadata = replaceEntityMetadata(metadata, newMetadata, Entity.DATA_WITHER_TARGET_1_EID, from, to);
        newMetadata = replaceEntityMetadata(metadata, newMetadata, Entity.DATA_WITHER_TARGET_2_EID, from, to);
        newMetadata = replaceEntityMetadata(metadata, newMetadata, Entity.DATA_WITHER_TARGET_3_EID, from, to);
        newMetadata = replaceEntityMetadata(metadata, newMetadata, Entity.DATA_TRADING_PLAYER_EID, from, to);
        newMetadata = replaceEntityMetadata(metadata, newMetadata, Entity.DATA_BALLOON_ATTACHED_EID, from, to);
        newMetadata = replaceEntityMetadata(metadata, newMetadata, Entity.DATA_PLAYER_AGENT_EID, from, to);
        return newMetadata;
    }

    @Nullable
    private static EntityMetadata replaceEntityMetadata(EntityMetadata metadata, @Nullable EntityMetadata newMetadata, int dataId, long from, long to) {
        EntityData<?> entityData = metadata.get(dataId);
        if (!(entityData instanceof LongEntityData data)) {
            return newMetadata;
        }
        if (data.getDataAsLong() != from) {
            return newMetadata;
        }
        if (newMetadata == null) {
            newMetadata = metadata.copy();
        }
        newMetadata.putLong(dataId, to);
        return newMetadata;
    }
}
