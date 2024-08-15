package org.itxtech.synapseapi.multiprotocol.protocol11940.protocol;

import cn.nukkit.entity.data.EntityMetadata;
import cn.nukkit.network.protocol.DataPacket;
import cn.nukkit.network.protocol.ProtocolInfo;
import cn.nukkit.network.protocol.SetEntityDataPacket;
import it.unimi.dsi.fastutil.ints.Int2FloatMap;
import it.unimi.dsi.fastutil.ints.Int2FloatOpenHashMap;
import it.unimi.dsi.fastutil.ints.Int2IntMap;
import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;
import lombok.ToString;
import org.itxtech.synapseapi.multiprotocol.AbstractProtocol;
import org.itxtech.synapseapi.multiprotocol.utils.EntityMetadataGenerator;
import org.itxtech.synapseapi.utils.ClassUtils;

@ToString
public class SetEntityDataPacket11940 extends Packet11940 {
    public static final int NETWORK_ID = ProtocolInfo.SET_ACTOR_DATA_PACKET;

    public long eid;
    public EntityMetadata metadata;
    public Int2IntMap intProperties = new Int2IntOpenHashMap();
    public Int2FloatMap floatProperties = new Int2FloatOpenHashMap();
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
        this.putEntityMetadata(this.metadata);
        this.putUnsignedVarInt(this.intProperties.size());
        for (Int2IntMap.Entry property : this.intProperties.int2IntEntrySet()) {
            this.putUnsignedVarInt(property.getIntKey());
            this.putVarInt(property.getIntValue());
        }
        this.putUnsignedVarInt(this.floatProperties.size());
        for (Int2FloatMap.Entry property : this.floatProperties.int2FloatEntrySet()) {
            this.putUnsignedVarInt(property.getIntKey());
            this.putLFloat(property.getFloatValue());
        }
        this.putUnsignedVarLong(this.frame);
    }

    @Override
    public DataPacket fromDefault(DataPacket pk, AbstractProtocol protocol, boolean netease) {
        ClassUtils.requireInstance(pk, SetEntityDataPacket.class);

        SetEntityDataPacket packet = (SetEntityDataPacket) pk;
        this.eid = packet.eid;
        this.metadata = EntityMetadataGenerator.generateFrom(packet.metadata, protocol, netease);
        this.intProperties = packet.intProperties;
        this.floatProperties = packet.floatProperties;

        return this;
    }

    public static Class<? extends DataPacket> getDefaultPacket() {
        return SetEntityDataPacket.class;
    }
}
