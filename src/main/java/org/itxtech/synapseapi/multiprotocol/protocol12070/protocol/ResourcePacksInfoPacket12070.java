package org.itxtech.synapseapi.multiprotocol.protocol12070.protocol;

import cn.nukkit.network.protocol.DataPacket;
import cn.nukkit.network.protocol.ProtocolInfo;
import cn.nukkit.network.protocol.ResourcePacksInfoPacket;
import cn.nukkit.resourcepacks.ResourcePack;
import lombok.ToString;

@ToString
public class ResourcePacksInfoPacket12070 extends Packet12070 {
    public static final int NETWORK_ID = ProtocolInfo.RESOURCE_PACKS_INFO_PACKET;

    public boolean mustAccept;
    public boolean hasAddonPacks;
    public boolean scripting;
    public boolean forceServerPacks = true;
    public ResourcePack[] behaviourPackEntries = new ResourcePack[0];
    public ResourcePack[] resourcePackEntries = new ResourcePack[0];
    public CDNEntry[] cdnEntries = new CDNEntry[0];

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
        this.putBoolean(this.mustAccept);
        this.putBoolean(this.hasAddonPacks);
        this.putBoolean(this.scripting);
        this.putBoolean(this.forceServerPacks);

        encodePacks(this.behaviourPackEntries);
        encodePacks(this.resourcePackEntries);

        this.putUnsignedVarInt(this.cdnEntries.length);
        for (CDNEntry entry : cdnEntries) {
            this.putString(entry.packId);
            this.putString(entry.remoteUrl);
        }
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
            this.putBoolean(false); // scripting
            if (entry.getPackType().equals("resources")) {
                this.putBoolean(entry.getCapabilities().contains("raytraced"));
            }
        }
    }

    @Override
    public DataPacket fromDefault(DataPacket pk) {
        ResourcePacksInfoPacket packet = (ResourcePacksInfoPacket) pk;
        this.mustAccept = packet.mustAccept;
        this.behaviourPackEntries = packet.behaviourPackEntries;
        this.resourcePackEntries = packet.resourcePackEntries;
        return this;
    }

    public static Class<? extends DataPacket> getDefaultPacket() {
        return ResourcePacksInfoPacket.class;
    }

    public record CDNEntry(String packId, String remoteUrl) {
    }
}
