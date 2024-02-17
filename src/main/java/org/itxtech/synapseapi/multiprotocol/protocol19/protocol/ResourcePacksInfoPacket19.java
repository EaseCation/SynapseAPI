package org.itxtech.synapseapi.multiprotocol.protocol19.protocol;

import cn.nukkit.network.protocol.DataPacket;
import cn.nukkit.network.protocol.ProtocolInfo;
import cn.nukkit.resourcepacks.ResourcePack;
import lombok.ToString;

@ToString
public class ResourcePacksInfoPacket19 extends Packet19 {

	public static final int NETWORK_ID = ProtocolInfo.RESOURCE_PACKS_INFO_PACKET;

    public boolean mustAccept = false;
    public boolean hasScripts = false;
    public ResourcePack[] behaviourPackEntries = new ResourcePack[0];
    public ResourcePack[] resourcePackEntries = new ResourcePack[0];

    @Override
    public void decode() {

    }

    @Override
    public void encode() {
        this.reset();
        this.putBoolean(this.mustAccept);
        this.putBoolean(this.hasScripts);
        encodePacks(this.behaviourPackEntries);
        encodePacks(this.resourcePackEntries);
    }

    private void encodePacks(ResourcePack[] packs) {
        this.putLShort(packs.length);
        for (ResourcePack entry : packs) {
            this.putString(entry.getPackId());
            this.putString(entry.getPackVersion());
            this.putLLong(entry.getPackSize());
            this.putString(entry.getEncryptionKey());
            this.putString(""); // sub-pack name
            this.putString(!entry.getEncryptionKey().isEmpty() ? entry.getPackId() : ""); // content identity
            this.putBoolean(false); // has scripts
        }
    }

    @Override
    public int pid() {
        return NETWORK_ID;
    }

    public static Class<? extends DataPacket> getDefaultPacket() {
        return cn.nukkit.network.protocol.ResourcePacksInfoPacket.class;
    }

}
