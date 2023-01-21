package org.itxtech.synapseapi.multiprotocol.protocol116100.protocol;

import cn.nukkit.network.protocol.DataPacket;
import cn.nukkit.network.protocol.ProtocolInfo;
import cn.nukkit.network.protocol.ResourcePackStackPacket;
import cn.nukkit.resourcepacks.ResourcePack;
import lombok.ToString;
import org.itxtech.synapseapi.multiprotocol.AbstractProtocol;
import org.itxtech.synapseapi.multiprotocol.protocol113.protocol.Packet113;
import org.itxtech.synapseapi.utils.ClassUtils;

@ToString
public class ResourcePackStackPacket116100 extends Packet113 {

    public static final int NETWORK_ID = ProtocolInfo.RESOURCE_PACK_STACK_PACKET;

    public boolean mustAccept = false;
    public ResourcePack[] behaviourPackStack = new ResourcePack[0];
    public ResourcePack[] resourcePackStack = new ResourcePack[0];
    public String gameVersion = "1.16.100";

    @Override
    public void decode() {

    }

    @Override
    public void encode() {
        this.reset();
        this.putBoolean(this.mustAccept);

        this.putUnsignedVarInt(this.behaviourPackStack.length);
        for (ResourcePack entry : this.behaviourPackStack) {
            this.putString(entry.getPackId());
            this.putString(entry.getPackVersion());
            this.putString("");
        }

        this.putUnsignedVarInt(this.resourcePackStack.length);
        for (ResourcePack entry : this.resourcePackStack) {
            this.putString(entry.getPackId());
            this.putString(entry.getPackVersion());
            this.putString("");
        }

        this.putString("*");//this.putString(this.helper.getGameVersion());
        this.putLInt(0); // Experiments length
        this.putBoolean(false); //Were experiments previously toggled
    }

    @Override
    public int pid() {
        return NETWORK_ID;
    }

    @Override
    public DataPacket fromDefault(DataPacket pk, AbstractProtocol protocol, boolean netease) {
        ClassUtils.requireInstance(pk, ResourcePackStackPacket.class);

        ResourcePackStackPacket packet = (ResourcePackStackPacket) pk;
        this.mustAccept = packet.mustAccept;
        this.behaviourPackStack = packet.behaviourPackStack;
        this.resourcePackStack = packet.resourcePackStack;

        return this;
    }

    public static Class<? extends DataPacket> getDefaultPacket() {
        return ResourcePackStackPacket.class;
    }
}
