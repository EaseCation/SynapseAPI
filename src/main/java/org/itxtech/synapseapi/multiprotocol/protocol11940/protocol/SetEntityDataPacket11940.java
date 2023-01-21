package org.itxtech.synapseapi.multiprotocol.protocol11940.protocol;

import cn.nukkit.entity.data.EntityMetadata;
import cn.nukkit.network.protocol.DataPacket;
import cn.nukkit.network.protocol.ProtocolInfo;
import cn.nukkit.network.protocol.SetEntityDataPacket;
import cn.nukkit.utils.Binary;
import lombok.ToString;
import org.itxtech.synapseapi.multiprotocol.AbstractProtocol;
import org.itxtech.synapseapi.multiprotocol.utils.EntityMetadataGenerator;
import org.itxtech.synapseapi.utils.ClassUtils;

@ToString
public class SetEntityDataPacket11940 extends Packet11940 {
    public static final int NETWORK_ID = ProtocolInfo.SET_ACTOR_DATA_PACKET;

    public long eid;
    public EntityMetadata metadata;
    public long frame;

    @Override
    public int pid() {
        return NETWORK_ID;
    }

    @Override
    public void decode() {
    }

    @Override
    public void encode() {
        this.reset();
        this.putEntityRuntimeId(this.eid);
        this.put(Binary.writeMetadata(this.metadata));
        this.putUnsignedVarInt(0); // entity int properties
        this.putUnsignedVarInt(0); // entity float properties
        this.putUnsignedVarLong(this.frame);
    }

    @Override
    public DataPacket fromDefault(DataPacket pk, AbstractProtocol protocol, boolean netease) {
        ClassUtils.requireInstance(pk, SetEntityDataPacket.class);

        SetEntityDataPacket packet = (SetEntityDataPacket) pk;
        this.eid = packet.eid;
        this.metadata = EntityMetadataGenerator.generateFrom(packet.metadata, protocol, netease);

        return this;
    }

    public static Class<? extends DataPacket> getDefaultPacket() {
        return SetEntityDataPacket.class;
    }
}
