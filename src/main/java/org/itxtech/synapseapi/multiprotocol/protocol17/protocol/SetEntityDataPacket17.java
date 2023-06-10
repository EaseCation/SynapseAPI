package org.itxtech.synapseapi.multiprotocol.protocol17.protocol;

import cn.nukkit.entity.data.EntityMetadata;
import cn.nukkit.network.protocol.DataPacket;
import cn.nukkit.network.protocol.ProtocolInfo;
import lombok.ToString;
import org.itxtech.synapseapi.multiprotocol.AbstractProtocol;
import org.itxtech.synapseapi.multiprotocol.utils.EntityMetadataGenerator;
import org.itxtech.synapseapi.utils.ClassUtils;

/**
 * author: MagicDroidX
 * Nukkit Project
 */
@ToString
public class SetEntityDataPacket17 extends Packet17 {
    public static final int NETWORK_ID = ProtocolInfo.SET_ACTOR_DATA_PACKET;

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
        this.putEntityMetadata(this.metadata);
    }

    @Override
    public DataPacket fromDefault(DataPacket pk, AbstractProtocol protocol, boolean netease) {
    	ClassUtils.requireInstance(pk, cn.nukkit.network.protocol.SetEntityDataPacket.class);

    	cn.nukkit.network.protocol.SetEntityDataPacket packet = (cn.nukkit.network.protocol.SetEntityDataPacket) pk;

        this.eid = packet.eid;
        this.metadata = EntityMetadataGenerator.generateFrom(packet.metadata, protocol, netease);
        return this;
	}

	public static Class<? extends DataPacket> getDefaultPacket() {
        return cn.nukkit.network.protocol.SetEntityDataPacket.class;
    }

}
