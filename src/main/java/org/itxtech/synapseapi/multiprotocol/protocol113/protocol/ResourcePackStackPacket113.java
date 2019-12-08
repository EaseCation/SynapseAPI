package org.itxtech.synapseapi.multiprotocol.protocol113.protocol;

import cn.nukkit.network.protocol.DataPacket;
import cn.nukkit.network.protocol.ProtocolInfo;
import cn.nukkit.resourcepacks.ResourcePack;

public class ResourcePackStackPacket113 extends Packet113 {

    public static final int NETWORK_ID = ProtocolInfo.RESOURCE_PACK_STACK_PACKET;

    public boolean mustAccept = false;
    public ResourcePack[] behaviourPackStack = new ResourcePack[0];
    public ResourcePack[] resourcePackStack = new ResourcePack[0];
    public boolean isExperimental = false;
    public String gameVersion = "1.13.0";

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
        this.putBoolean(this.isExperimental);
        this.putString(this.gameVersion);
    }

    @Override
    public int pid() {
        return NETWORK_ID;
    }

    public static Class<? extends DataPacket> getDefaultPacket() {
        return cn.nukkit.network.protocol.ResourcePackStackPacket.class;
    }
}
