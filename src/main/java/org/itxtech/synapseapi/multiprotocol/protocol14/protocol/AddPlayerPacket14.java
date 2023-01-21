package org.itxtech.synapseapi.multiprotocol.protocol14.protocol;

import java.util.UUID;

import cn.nukkit.network.protocol.types.EntityLink;
import lombok.ToString;
import org.itxtech.synapseapi.multiprotocol.utils.EntityMetadataGenerator;
import org.itxtech.synapseapi.utils.ClassUtils;

import cn.nukkit.entity.data.EntityMetadata;
import cn.nukkit.item.Item;
import cn.nukkit.network.protocol.DataPacket;
import cn.nukkit.network.protocol.ProtocolInfo;
import cn.nukkit.utils.Binary;

@ToString
public class AddPlayerPacket14 extends Packet14 {
	public static final int NETWORK_ID = ProtocolInfo.ADD_PLAYER_PACKET;

	@Override
	public int pid() {
		return NETWORK_ID;
	}

	public UUID uuid;
	public String username;
	public String thirdPartyName = "";
	public int platformId = -1;
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
	public EntityMetadata metadata = new EntityMetadata();

	public int flags = 8;
	public int userCommandPermissions = 4;
	public int permissionFlags = 256;
	public int playerPermissions = 1;
	public int storedCustomAbilities = 1;

	public EntityLink[] links = new EntityLink[0];

	@Override
	public void decode() {

	}

	@Override
	public void encode() {
		this.reset();
		this.putUUID(this.uuid);
		this.putString(this.username);
		this.putString(thirdPartyName);
		this.putVarInt(platformId);
		this.putEntityUniqueId(this.entityUniqueId);
		this.putEntityRuntimeId(this.entityRuntimeId);
		this.putString(this.platformChatId);
		this.putVector3f(this.x, this.y, this.z);
		this.putVector3f(this.speedX, this.speedY, this.speedZ);
		this.putLFloat(this.pitch);
		this.putLFloat(this.yaw);
		this.putLFloat(this.headYaw);
		this.putSlot(this.item);

		this.put(Binary.writeMetadata(this.metadata));

		this.putUnsignedVarInt(flags);
		this.putUnsignedVarInt(userCommandPermissions);
		this.putUnsignedVarInt(permissionFlags);
		this.putUnsignedVarInt(playerPermissions);
		this.putUnsignedVarInt(storedCustomAbilities);
		this.putLLong(entityUniqueId);

		this.putUnsignedVarInt(links.length);
		for (EntityLink link : links) {
			helper.putEntityLink(this, link);
		}
	}

	@Override
	public DataPacket fromDefault(DataPacket pk) {
    	ClassUtils.requireInstance(pk, cn.nukkit.network.protocol.AddPlayerPacket.class);

    	cn.nukkit.network.protocol.AddPlayerPacket packet = (cn.nukkit.network.protocol.AddPlayerPacket) pk;

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
        this.metadata = EntityMetadataGenerator.generate14From(packet.metadata);
		this.links = packet.links;
        return this;
	}

	public static Class<? extends DataPacket> getDefaultPacket() {
        return cn.nukkit.network.protocol.AddPlayerPacket.class;
    }
}
