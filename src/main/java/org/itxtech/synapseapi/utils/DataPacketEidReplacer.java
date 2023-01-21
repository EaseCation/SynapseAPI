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
import org.itxtech.synapseapi.multiprotocol.protocol11910.protocol.UpdateAbilitiesPacket11910;
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
            case AnimatePacket.NETWORK_ID:
                if (((AnimatePacket) packet).eid == from) ((AnimatePacket) packet).eid = to;
                break;
            case TakeItemEntityPacket.NETWORK_ID:
                if (((TakeItemEntityPacket) packet).entityId == from) ((TakeItemEntityPacket) packet).entityId = to;
                break;
            case SetEntityMotionPacket.NETWORK_ID:
                if (((SetEntityMotionPacket) packet).eid == from) ((SetEntityMotionPacket) packet).eid = to;
                break;
            case SetEntityLinkPacket.NETWORK_ID:
                if (((SetEntityLinkPacket) packet).vehicleUniqueId == from)
                    ((SetEntityLinkPacket) packet).vehicleUniqueId = to;
                if (((SetEntityLinkPacket) packet).riderUniqueId == from)
                    ((SetEntityLinkPacket) packet).riderUniqueId = to;
                break;
            case SetEntityDataPacket.NETWORK_ID:
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
            case UpdateAttributesPacket.NETWORK_ID:
                if (((UpdateAttributesPacket) packet).entityId == from) ((UpdateAttributesPacket) packet).entityId = to;
                break;
            case EntityEventPacket.NETWORK_ID:
                if (((EntityEventPacket) packet).eid == from) ((EntityEventPacket) packet).eid = to;
                break;
            case MovePlayerPacket.NETWORK_ID:
                if (packet instanceof MovePlayerPacket) {
                    if (((MovePlayerPacket) packet).eid == from) ((MovePlayerPacket) packet).eid = to;
                } else if (packet instanceof MovePlayerPacket116100NE) {
                    if (((MovePlayerPacket116100NE) packet).eid == from) ((MovePlayerPacket116100NE) packet).eid = to;
                }
                break;
            case MobEquipmentPacket.NETWORK_ID:
                if (((MobEquipmentPacket) packet).eid == from) ((MobEquipmentPacket) packet).eid = to;
                break;
            case MobEffectPacket.NETWORK_ID:
                if (((MobEffectPacket) packet).eid == from) ((MobEffectPacket) packet).eid = to;
                break;
            case MoveEntityPacket.NETWORK_ID:
                if (((MoveEntityPacket) packet).eid == from) ((MoveEntityPacket) packet).eid = to;
                break;
            case MobArmorEquipmentPacket.NETWORK_ID:
                if (((MobArmorEquipmentPacket) packet).eid == from) ((MobArmorEquipmentPacket) packet).eid = to;
                break;
            case PlayerListPacket.NETWORK_ID:
                Arrays.stream(((PlayerListPacket) packet).entries).filter(entry -> entry.entityId == from).forEach(entry -> entry.entityId = to);
                break;
            case BossEventPacket.NETWORK_ID:
                if (((BossEventPacket) packet).bossEid == from) ((BossEventPacket) packet).bossEid = to;
                break;
            case AdventureSettingsPacket.NETWORK_ID:
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
            case AddEntityPacket.NETWORK_ID:
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
            case EntityEventPacket.NETWORK_ID:
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
