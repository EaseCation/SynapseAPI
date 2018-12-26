package org.itxtech.synapseapi.multiprotocol.protocol14.protocol;

import org.itxtech.synapseapi.multiprotocol.utils.EntityMetadataGenerator;
import org.itxtech.synapseapi.utils.ClassUtils;

import cn.nukkit.entity.data.EntityMetadata;
import cn.nukkit.network.protocol.DataPacket;
import cn.nukkit.network.protocol.ProtocolInfo;
import cn.nukkit.utils.Binary;

/**
 * author: MagicDroidX
 * Nukkit Project
 */
public class SetEntityDataPacket14 extends Packet14 {
    public static final int NETWORK_ID = ProtocolInfo.SET_ENTITY_DATA_PACKET;

    @Override
    public int pid() {
        return NETWORK_ID;
    }

    public long eid;
    public EntityMetadata metadata;

    @Override
    public void decode() {

    }

    @Override
    public void encode() {
        this.reset();
        this.putEntityRuntimeId(this.eid);
        this.put(Binary.writeMetadata(this.metadata));
    }
 
    @Override
	public DataPacket fromDefault(DataPacket pk) {
    	ClassUtils.requireInstance(pk, cn.nukkit.network.protocol.SetEntityDataPacket.class);

    	cn.nukkit.network.protocol.SetEntityDataPacket packet = (cn.nukkit.network.protocol.SetEntityDataPacket) pk;
    	
        this.eid = packet.eid;
        this.metadata = EntityMetadataGenerator.generate14From(packet.metadata);
        return this;
	}

	public static Class<? extends DataPacket> getDefaultPacket() {
        return cn.nukkit.network.protocol.SetEntityDataPacket.class;
    }
    
}
