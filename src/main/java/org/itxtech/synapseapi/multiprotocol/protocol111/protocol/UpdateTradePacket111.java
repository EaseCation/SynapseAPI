package org.itxtech.synapseapi.multiprotocol.protocol111.protocol;

import cn.nukkit.network.protocol.DataPacket;
import cn.nukkit.network.protocol.ProtocolInfo;
import cn.nukkit.network.protocol.types.ContainerType;
import lombok.ToString;
import org.itxtech.synapseapi.utils.ClassUtils;

@ToString(exclude = "offers")
public class UpdateTradePacket111 extends Packet111 {

    public static final int NETWORK_ID = ProtocolInfo.UPDATE_TRADE_PACKET;

    public int windowId;
    public int windowType = ContainerType.TRADING; //Mojang hardcoded this -_-
    public int windowSize; //useless hardcoded to 0, seems to be part of a standard container header
    public int tradeTier;
    public long trader;
    public long player;
    public String displayName = "";
    public boolean newTradingUi;
    public boolean usingEconomyTrade;
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
        this.putByte((byte) windowId);
        this.putByte((byte) windowType);
        this.putVarInt(windowSize);
        this.putVarInt(tradeTier);
        this.putEntityUniqueId(trader);
        this.putEntityUniqueId(player);
        this.putString(displayName);
        this.putBoolean(newTradingUi);
        this.putBoolean(usingEconomyTrade);
        this.put(this.offers);
    }

    @Override
    public DataPacket fromDefault(DataPacket pk) {
        ClassUtils.requireInstance(pk, cn.nukkit.network.protocol.UpdateTradePacket.class);

        cn.nukkit.network.protocol.UpdateTradePacket packet = (cn.nukkit.network.protocol.UpdateTradePacket) pk;

        this.windowId = packet.windowId;
        this.windowType = packet.windowType;
        this.windowSize = packet.windowSize;
        this.tradeTier = packet.tradeTier;
        this.trader = packet.trader;
        this.player = packet.player;
        this.displayName = packet.displayName;
        this.newTradingUi = packet.newTradingUi;
        this.usingEconomyTrade = packet.usingEconomyTrade;
        this.offers = packet.offers;

        return this;
    }

    public static Class<? extends DataPacket> getDefaultPacket() {
        return cn.nukkit.network.protocol.UpdateTradePacket.class;
    }

}
