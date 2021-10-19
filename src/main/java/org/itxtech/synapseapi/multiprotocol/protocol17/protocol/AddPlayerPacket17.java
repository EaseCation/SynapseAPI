package org.itxtech.synapseapi.multiprotocol.protocol17.protocol;

import cn.nukkit.entity.data.EntityMetadata;
import cn.nukkit.item.Item;
import cn.nukkit.network.protocol.DataPacket;
import cn.nukkit.network.protocol.ProtocolInfo;
import cn.nukkit.utils.Binary;
import org.itxtech.synapseapi.multiprotocol.AbstractProtocol;
import org.itxtech.synapseapi.multiprotocol.utils.EntityMetadataGenerator;
import org.itxtech.synapseapi.utils.ClassUtils;

import java.util.UUID;

public class AddPlayerPacket17 extends Packet17 {
	public static final int NETWORK_ID = ProtocolInfo.ADD_PLAYER_PACKET;

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
	public EntityMetadata metadata = new EntityMetadata();

	//public EntityLink links = new EntityLink[0];
	public String deviceId = "";
	
	public int flags = 8;
	public int userCommandPermissions = 4;
	public int permissionFlags = 256;
	public int playerPermissions = 1;
	public int storedCustomAbilities = 1;
	public long playerUniqueId = 0L;

	@Override
	public void decode() {

	}

	@Override
	public void encode() {
		this.reset();
		this.putUUID(this.uuid);
		this.putString(this.username);
		this.putEntityUniqueId(this.entityUniqueId);
		this.putEntityRuntimeId(this.entityRuntimeId);
		this.putString(this.platformChatId); // platform chat id
		this.putVector3f(this.x, this.y, this.z);
		this.putVector3f(this.speedX, this.speedY, this.speedZ);
		this.putLFloat(this.pitch);
		this.putLFloat(this.yaw);
		this.putLFloat(this.headYaw);
		this.putSlot(this.item);

		this.put(Binary.writeMetadata(this.metadata));

		this.putUnsignedVarInt(flags); // flags
		this.putUnsignedVarInt(userCommandPermissions); // user command permissions
		this.putUnsignedVarInt(permissionFlags); // permission flags
		this.putUnsignedVarInt(playerPermissions); // player permissions
		this.putUnsignedVarInt(storedCustomAbilities); // stored custom abilities
		this.putLLong(playerUniqueId); // player unique Id
		this.putUnsignedVarInt(0); // entity links
		this.putString(deviceId);
	}
	
	@Override
    public DataPacket fromDefault(DataPacket pk, AbstractProtocol protocol, boolean netease) {
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
        this.metadata = EntityMetadataGenerator.generateFrom(packet.metadata, protocol, netease);
        return this;
	}
	
	public static Class<? extends DataPacket> getDefaultPacket() {
        return cn.nukkit.network.protocol.AddPlayerPacket.class;
    }
}
