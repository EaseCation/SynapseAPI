package org.itxtech.synapseapi.multiprotocol.protocol11910.protocol;

import cn.nukkit.network.protocol.ProtocolInfo;
import lombok.ToString;

@ToString
public class UpdateAdventureSettingsPacket11910 extends Packet11910 {
    public static final int NETWORK_ID = ProtocolInfo.UPDATE_ADVENTURE_SETTINGS_PACKET;

    public boolean noPvM;
    public boolean noMvP;
    public boolean worldImmutable;
    public boolean showNameTags;
    public boolean autoJump;

    @Override
    public int pid() {
        return NETWORK_ID;
    }

    @Override
    public void decode() {
    }

    @Override
    public void encode() {
        reset();
        putBoolean(noPvM);
        putBoolean(noMvP);
        putBoolean(worldImmutable);
        putBoolean(showNameTags);
        putBoolean(autoJump);
    }
}
