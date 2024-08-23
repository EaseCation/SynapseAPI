package org.itxtech.synapseapi.multiprotocol.protocol12130.protocol;

import cn.nukkit.network.protocol.DataPacket;
import cn.nukkit.network.protocol.ProtocolInfo;
import cn.nukkit.network.protocol.ResourcePacksInfoPacket;
import cn.nukkit.resourcepacks.ResourcePack;
import lombok.ToString;
import org.apache.commons.lang3.ArrayUtils;
import org.itxtech.synapseapi.multiprotocol.AbstractProtocol;

@ToString
public class ResourcePacksInfoPacket12130 extends Packet12130 {
    public static final int NETWORK_ID = ProtocolInfo.RESOURCE_PACKS_INFO_PACKET;

    public boolean mustAccept;
    public boolean hasAddonPacks;
    public boolean scripting;
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

        this.putLShort(resourcePackEntries.length);
        for (ResourcePack entry : resourcePackEntries) {
            this.putString(entry.getPackId());
            this.putString(entry.getPackVersion());
            this.putLLong(entry.getPackSize());
            this.putString(entry.getEncryptionKey());
            this.putString(""); // sub-pack name
            this.putString(!entry.getEncryptionKey().isEmpty() ? entry.getPackId() : ""); // content identity
            this.putBoolean(false); // scripting
            this.putBoolean(false); // addon: Indicates this pack is part of an Add-On. Helps clients determine if the pack must be downloaded to join the server as Add-On packs are required to play without issues.
            this.putBoolean(entry.getCapabilities().contains("raytraced"));
        }

        this.putUnsignedVarInt(this.cdnEntries.length);
        for (CDNEntry entry : cdnEntries) {
            this.putString(entry.packId);
            this.putString(entry.remoteUrl);
        }
    }

    @Override
    public DataPacket fromDefault(DataPacket pk, AbstractProtocol protocol, boolean netease) {
        ResourcePacksInfoPacket packet = (ResourcePacksInfoPacket) pk;
        this.mustAccept = packet.mustAccept;
        this.resourcePackEntries = netease ? ArrayUtils.addAll(packet.resourcePackEntries, packet.behaviourPackEntries) : packet.resourcePackEntries;
        return this;
    }

    public static Class<? extends DataPacket> getDefaultPacket() {
        return ResourcePacksInfoPacket.class;
    }

    public record CDNEntry(String packId, String remoteUrl) {
    }
}
