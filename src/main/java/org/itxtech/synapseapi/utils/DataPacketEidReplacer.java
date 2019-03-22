package org.itxtech.synapseapi.utils;

import cn.nukkit.entity.Entity;
import cn.nukkit.entity.data.EntityData;
import cn.nukkit.entity.data.EntityMetadata;
import cn.nukkit.network.protocol.*;
import cn.nukkit.network.protocol.AdventureSettingsPacket;
import cn.nukkit.network.protocol.AnimatePacket;
import cn.nukkit.network.protocol.BossEventPacket;
import cn.nukkit.network.protocol.ClientboundMapItemDataPacket;
import cn.nukkit.network.protocol.EntityEventPacket;
import cn.nukkit.network.protocol.MobArmorEquipmentPacket;
import cn.nukkit.network.protocol.MobEffectPacket;
import cn.nukkit.network.protocol.MobEquipmentPacket;
import cn.nukkit.network.protocol.MoveEntityPacket;
import cn.nukkit.network.protocol.MovePlayerPacket;
import cn.nukkit.network.protocol.PlayerListPacket;
import cn.nukkit.network.protocol.ProtocolInfo;
import cn.nukkit.network.protocol.SetEntityDataPacket;
import cn.nukkit.network.protocol.SetEntityLinkPacket;
import cn.nukkit.network.protocol.SetEntityMotionPacket;
import cn.nukkit.network.protocol.TakeItemEntityPacket;
import cn.nukkit.network.protocol.UpdateAttributesPacket;

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
                if (((SetEntityLinkPacket) packet).rider == from)
                    ((SetEntityLinkPacket) packet).rider = to;
                if (((SetEntityLinkPacket) packet).riding == from)
                    ((SetEntityLinkPacket) packet).riding = to;
                break;
            case SetEntityDataPacket.NETWORK_ID:
                if (((SetEntityDataPacket) packet).eid == from) ((SetEntityDataPacket) packet).eid = to;
                if (((SetEntityDataPacket) packet).metadata.exists(Entity.DATA_OWNER_EID)) {
                    ((SetEntityDataPacket) packet).metadata = cloneEntityMetadata(((SetEntityDataPacket) packet).metadata);
                    if (((SetEntityDataPacket) packet).metadata.getLong(Entity.DATA_OWNER_EID) == from) {
                        ((SetEntityDataPacket) packet).metadata.putLong(Entity.DATA_OWNER_EID, to);
                    }
                }                break;
            case UpdateAttributesPacket.NETWORK_ID:
                if (((UpdateAttributesPacket) packet).entityId == from) ((UpdateAttributesPacket) packet).entityId = to;
                break;
            case EntityEventPacket.NETWORK_ID:
                if (((EntityEventPacket) packet).eid == from) ((EntityEventPacket) packet).eid = to;
                break;
            case MovePlayerPacket.NETWORK_ID:
                if (((MovePlayerPacket) packet).eid == from) ((MovePlayerPacket) packet).eid = to;
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
            default:
                change = false;
        }

        if(change) {
            packet.isEncoded = false;
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
