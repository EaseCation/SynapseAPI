package org.itxtech.synapseapi.multiprotocol.protocol14.protocol;

import cn.nukkit.network.protocol.ProtocolInfo;
import cn.nukkit.resourcepacks.ResourcePack;

public class ResourcePacksInfoPacket14 extends Packet14 {

	public static final int NETWORK_ID = ProtocolInfo.RESOURCE_PACKS_INFO_PACKET;

    public boolean mustAccept = false;
    public ResourcePack[] behaviourPackEntries = new ResourcePack[0];
    public ResourcePack[] resourcePackEntries = new ResourcePack[0];

    @Override
    public void decode() {

    }

    @Override
    public void encode() {
        this.reset();
        this.putBoolean(this.mustAccept);

        this.putLShort(this.behaviourPackEntries.length);
        for (ResourcePack entry : this.behaviourPackEntries) {
            this.putString(entry.getPackId());
            this.putString(entry.getPackVersion());
            this.putLLong(entry.getPackSize());
            this.putString(""); //content key
            this.putString(""); //sub pack name
        }

        this.putLShort(this.resourcePackEntries.length);
        for (ResourcePack entry : this.resourcePackEntries) {
            this.putString(entry.getPackId());
            this.putString(entry.getPackVersion());
            this.putLLong(entry.getPackSize());
            this.putString(""); //content key
            this.putString(""); //sub pack name
        }
    }

    @Override
    public int pid() {
        return NETWORK_ID;
    }
}
