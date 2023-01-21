package org.itxtech.synapseapi.multiprotocol.protocol111.protocol;

import cn.nukkit.network.protocol.DataPacket;
import cn.nukkit.network.protocol.ProtocolInfo;
import lombok.ToString;
import org.itxtech.synapseapi.utils.ClassUtils;

@ToString(exclude = "offers")
public class UpdateTradePacket111 extends Packet111 {

    public static final int NETWORK_ID = ProtocolInfo.UPDATE_TRADE_PACKET;

    public byte windowId;
    public byte windowType = 15; //trading id
    public int unknownVarInt1; // hardcoded to 0
    public int tradeTier;
    public long trader;
    public long player;
    public String displayName;
    public boolean screen2;
    public boolean isWilling;
    public byte[] offers;

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
        this.putByte(windowId);
        this.putByte(windowType);
        this.putVarInt(unknownVarInt1);
        this.putVarInt(tradeTier);
        this.putEntityUniqueId(player);
        this.putEntityUniqueId(trader);
        this.putString(displayName);
        this.putBoolean(screen2);
        this.putBoolean(isWilling);
        this.put(this.offers);
    }

    @Override
    public DataPacket fromDefault(DataPacket pk) {
        ClassUtils.requireInstance(pk, cn.nukkit.network.protocol.UpdateTradePacket.class);

        cn.nukkit.network.protocol.UpdateTradePacket packet = (cn.nukkit.network.protocol.UpdateTradePacket) pk;

        this.windowId = packet.windowId;
        this.windowType = packet.windowType;
        this.unknownVarInt1 = packet.unknownVarInt1;
        this.trader = packet.trader;
        this.player = packet.player;
        this.displayName = packet.displayName;
        this.offers = packet.offers;

        return this;
    }

    public static Class<? extends DataPacket> getDefaultPacket() {
        return cn.nukkit.network.protocol.UpdateTradePacket.class;
    }

}
