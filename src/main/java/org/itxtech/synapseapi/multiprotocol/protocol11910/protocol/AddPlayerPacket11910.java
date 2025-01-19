package org.itxtech.synapseapi.multiprotocol.protocol11910.protocol;

import cn.nukkit.Player;
import cn.nukkit.command.data.CommandPermission;
import cn.nukkit.entity.data.EntityMetadata;
import cn.nukkit.item.Item;
import cn.nukkit.network.protocol.AddPlayerPacket;
import cn.nukkit.network.protocol.DataPacket;
import cn.nukkit.network.protocol.ProtocolInfo;
import cn.nukkit.network.protocol.types.AbilityLayer;
import cn.nukkit.network.protocol.types.EntityLink;
import cn.nukkit.network.protocol.types.PlayerAbility;
import cn.nukkit.utils.Utils;
import lombok.ToString;
import org.itxtech.synapseapi.multiprotocol.AbstractProtocol;
import org.itxtech.synapseapi.multiprotocol.utils.EntityMetadataGenerator;
import org.itxtech.synapseapi.utils.ClassUtils;

import java.util.UUID;

import static org.itxtech.synapseapi.SynapsePlayer116100.GAME_TYPE_SURVIVAL;

@ToString
public class AddPlayerPacket11910 extends Packet11910 {
    public static final int NETWORK_ID = ProtocolInfo.ADD_PLAYER_PACKET;

    private static final AbilityLayer[] SURVIVAL_ABILITY_LAYERS = new AbilityLayer[]{
            Utils.make(() -> {
                AbilityLayer layer = new AbilityLayer();
                layer.abilityValues.add(PlayerAbility.BUILD);
                layer.abilityValues.add(PlayerAbility.MINE);
                layer.abilityValues.add(PlayerAbility.DOORS_AND_SWITCHES);
                layer.abilityValues.add(PlayerAbility.OPEN_CONTAINERS);
                layer.abilityValues.add(PlayerAbility.ATTACK_PLAYERS);
                layer.abilityValues.add(PlayerAbility.ATTACK_MOBS);
                return layer;
            }),
    };

    @Override
    public int pid() {
        return NETWORK_ID;
    }

    public UUID uuid;
    public String username;
    public long entityUniqueId;
    public long entityRuntimeId;
    public String platformChatId = "";
    public float x;
    public float y;
    public float z;
    public float speedX;
    public float speedY;
    public float speedZ;
    public float pitch;
    public float yaw;
    public float headYaw;
    public Item item;
    public int gameMode = GAME_TYPE_SURVIVAL;
    public EntityMetadata metadata = new EntityMetadata();

    public EntityLink[] links = new EntityLink[0];
    public String deviceId = "";
    public int buildPlatform = -1;

    public int playerPermission = Player.PERMISSION_MEMBER;
    public CommandPermission commandPermission = CommandPermission.ALL;
    public AbilityLayer[] abilityLayers = SURVIVAL_ABILITY_LAYERS;

    @Override
    public void decode() {
    }

    @Override
    public void encode() {
        this.reset();
        this.putUUID(this.uuid);
        this.putString(this.username);
        this.putEntityRuntimeId(this.entityRuntimeId);
        this.putString(this.platformChatId);
        this.putVector3f(this.x, this.y, this.z);
        this.putVector3f(this.speedX, this.speedY, this.speedZ);
        this.putLFloat(this.pitch);
        this.putLFloat(this.yaw);
        this.putLFloat(this.headYaw);
        this.putSlot(this.item);
        this.putVarInt(this.gameMode);
        this.putEntityMetadata(this.metadata);

        // UpdateAbilitiesPacket
        this.putLLong(entityUniqueId);
        this.putByte(playerPermission);
        this.putByte(commandPermission.ordinal());
        this.putUnsignedVarInt(abilityLayers.length);
        for (AbilityLayer layer : abilityLayers) {
            helper.putAbilityLayer(this, layer);
        }

        this.putUnsignedVarInt(links.length);
        for (EntityLink link : links) {
            helper.putEntityLink(this, link);
        }

        this.putString(deviceId);
        this.putLInt(buildPlatform);
    }

    @Override
    public DataPacket fromDefault(DataPacket pk, AbstractProtocol protocol, boolean netease) {
        ClassUtils.requireInstance(pk, AddPlayerPacket.class);

        AddPlayerPacket packet = (AddPlayerPacket) pk;

        this.uuid = packet.uuid;
        this.username = packet.username;
        this.entityUniqueId = packet.entityUniqueId;
        this.entityRuntimeId = packet.entityRuntimeId;
        this.x = packet.x;
        this.y = packet.y;
        this.z = packet.z;
        this.speedX = packet.speedX;
        this.speedY = packet.speedY;
        this.speedZ = packet.speedZ;
        this.pitch = packet.pitch;
        this.yaw = packet.yaw;
        this.headYaw = packet.headYaw;
        this.item = packet.item;
        this.metadata = EntityMetadataGenerator.generateFrom(packet.metadata, protocol, netease);
        this.links = packet.links;
        return this;
    }

    public static Class<? extends DataPacket> getDefaultPacket() {
        return AddPlayerPacket.class;
    }
}
