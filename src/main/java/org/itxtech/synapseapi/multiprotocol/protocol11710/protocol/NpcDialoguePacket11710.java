package org.itxtech.synapseapi.multiprotocol.protocol11710.protocol;

import cn.nukkit.network.protocol.ProtocolInfo;
import lombok.ToString;

@ToString
public class NpcDialoguePacket11710 extends Packet11710 {
    public static final int NETWORK_ID = ProtocolInfo.NPC_DIALOGUE_PACKET;

    public static final int ACTION_OPEN = 0;
    public static final int ACTION_CLOSE = 1;

    public int npcEntityUniqueId;
    public int actionType = ACTION_OPEN;
    public String dialogue = "";
    public String sceneName = "";
    public String npcName = "";
    public String actionJson = "";

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
        this.putLLong(this.npcEntityUniqueId);
        this.putVarInt(this.actionType);
        this.putString(this.dialogue);
        this.putString(this.sceneName);
        this.putString(this.npcName);
        this.putString(this.actionJson);
    }
}
