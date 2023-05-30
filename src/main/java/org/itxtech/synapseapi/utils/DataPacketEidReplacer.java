package org.itxtech.synapseapi.utils;

import cn.nukkit.entity.Entity;
import cn.nukkit.entity.data.EntityData;
import cn.nukkit.entity.data.EntityMetadata;
import cn.nukkit.network.protocol.*;
import org.itxtech.synapseapi.multiprotocol.protocol113.protocol.MoveEntityDeltaPacket113;
import org.itxtech.synapseapi.multiprotocol.protocol116100.protocol.AnimateEntityPacket116100;
import org.itxtech.synapseapi.multiprotocol.protocol116100.protocol.MoveEntityDeltaPacket116100;
import org.itxtech.synapseapi.multiprotocol.protocol116100ne.protocol.MovePlayerPacket116100NE;
import org.itxtech.synapseapi.multiprotocol.protocol11730.protocol.AnimateEntityPacket11730;
import org.itxtech.synapseapi.multiprotocol.protocol11830.protocol.SpawnParticleEffectPacket11830;
import org.itxtech.synapseapi.multiprotocol.protocol119.protocol.PlayerActionPacket119;
import org.itxtech.synapseapi.multiprotocol.protocol11910.protocol.UpdateAbilitiesPacket11910;
import org.itxtech.synapseapi.multiprotocol.protocol14.protocol.PlayerActionPacket14;
import org.itxtech.synapseapi.multiprotocol.protocol15.protocol.MoveEntityDeltaPacket;
import org.itxtech.synapseapi.multiprotocol.protocol18.protocol.SpawnParticleEffectPacket18;

import java.util.Arrays;
import java.util.Map;

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
                if (((SetEntityMotionPacket) packet).eid == from) ((SetEntityMotionPacket) packet).eid = to;
                break;
            case ProtocolInfo.SET_ACTOR_LINK_PACKET:
                if (((SetEntityLinkPacket) packet).vehicleUniqueId == from)
                    ((SetEntityLinkPacket) packet).vehicleUniqueId = to;
                if (((SetEntityLinkPacket) packet).riderUniqueId == from)
                    ((SetEntityLinkPacket) packet).riderUniqueId = to;
                break;
            case ProtocolInfo.SET_ACTOR_DATA_PACKET:
                if (((SetEntityDataPacket) packet).eid == from) ((SetEntityDataPacket) packet).eid = to;
                if (((SetEntityDataPacket) packet).metadata.exists(Entity.DATA_OWNER_EID)) {
                    ((SetEntityDataPacket) packet).metadata = cloneEntityMetadata(((SetEntityDataPacket) packet).metadata);
                    if (((SetEntityDataPacket) packet).metadata.getLong(Entity.DATA_OWNER_EID) == from) {
                        ((SetEntityDataPacket) packet).metadata.putLong(Entity.DATA_OWNER_EID, to);
                    }
                }
                if (((SetEntityDataPacket) packet).metadata.exists(Entity.DATA_TARGET_EID)) {
                    ((SetEntityDataPacket) packet).metadata = cloneEntityMetadata(((SetEntityDataPacket) packet).metadata);
                    if (((SetEntityDataPacket) packet).metadata.getLong(Entity.DATA_TARGET_EID) == from) {
                        ((SetEntityDataPacket) packet).metadata.putLong(Entity.DATA_TARGET_EID, to);
                    }
                }
                break;
            case ProtocolInfo.UPDATE_ATTRIBUTES_PACKET:
                if (((UpdateAttributesPacket) packet).entityId == from) ((UpdateAttributesPacket) packet).entityId = to;
                break;
            case ProtocolInfo.ACTOR_EVENT_PACKET:
                if (((EntityEventPacket) packet).eid == from) ((EntityEventPacket) packet).eid = to;
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
                if (((MobEffectPacket) packet).eid == from) ((MobEffectPacket) packet).eid = to;
                break;
            case ProtocolInfo.MOVE_ACTOR_ABSOLUTE_PACKET:
                if (((MoveEntityPacket) packet).eid == from) ((MoveEntityPacket) packet).eid = to;
                break;
            case ProtocolInfo.MOB_ARMOR_EQUIPMENT_PACKET:
                if (((MobArmorEquipmentPacket) packet).eid == from) ((MobArmorEquipmentPacket) packet).eid = to;
                break;
            case ProtocolInfo.PLAYER_LIST_PACKET:
                Arrays.stream(((PlayerListPacket) packet).entries).filter(entry -> entry.entityId == from).forEach(entry -> entry.entityId = to);
                break;
            case ProtocolInfo.BOSS_EVENT_PACKET:
                if (((BossEventPacket) packet).bossEid == from) ((BossEventPacket) packet).bossEid = to;
                break;
            case ProtocolInfo.ADVENTURE_SETTINGS_PACKET:
                if (((AdventureSettingsPacket) packet).entityUniqueId == from) ((AdventureSettingsPacket) packet).entityUniqueId = to;
                break;
            case ProtocolInfo.UPDATE_EQUIPMENT_PACKET:
                if (((UpdateEquipmentPacket) packet).eid == from) ((UpdateEquipmentPacket) packet).eid = to;
                break;
            case ProtocolInfo.CLIENTBOUND_MAP_ITEM_DATA_PACKET:
                for (ClientboundMapItemDataPacket.MapTrackedObject object : ((ClientboundMapItemDataPacket) packet).trackedEntities) {
                    if (object.entityUniqueId == from) object.entityUniqueId = to;
                }
                break;
            case ProtocolInfo.ADD_ACTOR_PACKET:
                if (((AddEntityPacket) packet).metadata.exists(Entity.DATA_OWNER_EID)) {
                    ((AddEntityPacket) packet).metadata = cloneEntityMetadata(((AddEntityPacket) packet).metadata);
                    if (((AddEntityPacket) packet).metadata.getLong(Entity.DATA_OWNER_EID) == from) {
                        ((AddEntityPacket) packet).metadata.putLong(Entity.DATA_OWNER_EID, to);
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
                if (packet instanceof AnimateEntityPacket116100) {
                    for (int i = 0; i < ((AnimateEntityPacket116100) packet).entityRuntimeIds.length; i++) {
                        if (((AnimateEntityPacket116100) packet).entityRuntimeIds[i] == from) {
                            ((AnimateEntityPacket116100) packet).entityRuntimeIds[i] = to;
                        }
                    }
                } else if (packet instanceof AnimateEntityPacket11730) {
                    for (int i = 0; i < ((AnimateEntityPacket11730) packet).entityRuntimeIds.length; i++) {
                        if (((AnimateEntityPacket11730) packet).entityRuntimeIds[i] == from) {
                            ((AnimateEntityPacket11730) packet).entityRuntimeIds[i] = to;
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
                if (((EntityEventPacket) packet).eid == from) ((EntityEventPacket) packet).eid = to;
                break;
        }

        return packet;
    }

    private static EntityMetadata cloneEntityMetadata(EntityMetadata metadata) {
        Map<Integer, EntityData> map = metadata.getMap();
        EntityMetadata re = new EntityMetadata();
        map.forEach((i, data) -> re.put(data));
        return re;
    }
}
