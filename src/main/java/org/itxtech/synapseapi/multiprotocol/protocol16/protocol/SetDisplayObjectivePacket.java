package org.itxtech.synapseapi.multiprotocol.protocol16.protocol;

import cn.nukkit.network.protocol.ProtocolInfo;
import lombok.ToString;

@ToString
public class SetDisplayObjectivePacket extends Packet16 {

    public static final int SORT_ORDER_ASCENDING = 0;
    public static final int SORT_ORDER_DESCENDING = 1;

    public String displaySlotName;
    public String objectiveName;
    public String objectiveDisplayName;
    public String criteriaName;
    public int sortOrder;

    @Override
    public int pid() {
        return ProtocolInfo.SET_DISPLAY_OBJECTIVE_PACKET;
    }

    @Override
    public void decode() {

    }

    @Override
    public void encode() {
        this.reset();
        this.putString(this.displaySlotName);
        this.putString(this.objectiveName);
        this.putString(this.objectiveDisplayName);
        this.putString(this.criteriaName);
        this.putByte((byte) this.sortOrder);
    }
}
