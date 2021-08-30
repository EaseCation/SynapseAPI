package org.itxtech.synapseapi.multiprotocol.protocol11710.protocol;

import cn.nukkit.network.protocol.DataPacket;
import cn.nukkit.network.protocol.ProtocolInfo;
import cn.nukkit.network.protocol.ResourcePacksInfoPacket;
import cn.nukkit.resourcepacks.ResourcePack;
import lombok.ToString;
import org.itxtech.synapseapi.utils.ClassUtils;

@ToString
public class ResourcePacksInfoPacket11710 extends Packet11710 {

    public static final int NETWORK_ID = ProtocolInfo.RESOURCE_PACKS_INFO_PACKET;

    public boolean mustAccept = false;
    public boolean scripting = false;
    public boolean forceServerPacks = true;
    public ResourcePack[] behaviourPackEntries = new ResourcePack[0];
    public ResourcePack[] resourcePackEntries = new ResourcePack[0];

    @Override
    public void decode() {

    }

    @Override
    public void encode() {
        this.reset();
        this.putBoolean(this.mustAccept);
        this.putBoolean(this.scripting);
        this.putBoolean(this.forceServerPacks);

        encodePacks(this.behaviourPackEntries);
        encodePacks(this.resourcePackEntries);
    }

    private void encodePacks(ResourcePack[] packs) {
        this.putLShort(packs.length);
        for (ResourcePack entry : packs) {
            this.putString(entry.getPackId());
            this.putString(entry.getPackVersion());
            this.putLLong(entry.getPackSize());
            this.putString(""); // encryption key
            this.putString(""); // sub-pack name
            this.putString(""); // content identity
            this.putBoolean(false); // scripting
            if (entry.getPackType().equals("resources")) {
                this.putBoolean(false); // raytracing capable
            }
        }
    }

    @Override
    public int pid() {
        return NETWORK_ID;
    }

    @Override
    public DataPacket fromDefault(DataPacket pk) {
        ClassUtils.requireInstance(pk, ResourcePacksInfoPacket.class);

        ResourcePacksInfoPacket packet = (ResourcePacksInfoPacket) pk;
        this.mustAccept = packet.mustAccept;
        this.behaviourPackEntries = packet.behaviourPackEntries;
        this.resourcePackEntries = packet.resourcePackEntries;

        return this;
    }

    public static Class<? extends DataPacket> getDefaultPacket() {
        return ResourcePacksInfoPacket.class;
    }
}
