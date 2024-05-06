package org.itxtech.synapseapi.multiprotocol.protocol18.protocol;

import cn.nukkit.network.protocol.DataPacket;
import cn.nukkit.network.protocol.ProtocolInfo;
import cn.nukkit.network.protocol.UpdateTradePacket;
import cn.nukkit.network.protocol.types.ContainerType;
import lombok.ToString;

@ToString(exclude = "offers")
public class UpdateTradePacket18 extends Packet18 {
    public static final int NETWORK_ID = ProtocolInfo.UPDATE_TRADE_PACKET;

    public int windowId;
    public int windowType = ContainerType.TRADING; //Mojang hardcoded this -_-
    public int windowSize; //useless hardcoded to 0, seems to be part of a standard container header
    public boolean newTradingUi;
    public int tradeTier;
    public boolean recipeAddedOnUpdate;
    public long trader;
    public long player;
    public String displayName = "";
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
        this.putVarInt(newTradingUi ? 40 : 0);
        this.putVarInt(tradeTier);
        this.putBoolean(recipeAddedOnUpdate);
        this.putEntityUniqueId(trader);
        this.putEntityUniqueId(player);
        this.putString(displayName);
        this.put(this.offers);
    }

    @Override
    public DataPacket fromDefault(DataPacket pk) {
        UpdateTradePacket packet = (UpdateTradePacket) pk;
        this.windowId = packet.windowId;
        this.windowType = packet.windowType;
        this.windowSize = packet.windowSize;
        this.newTradingUi = packet.newTradingUi;
        this.tradeTier = packet.tradeTier;
        this.recipeAddedOnUpdate = packet.recipeAddedOnUpdate;
        this.trader = packet.trader;
        this.player = packet.player;
        this.displayName = packet.displayName;
        this.offers = packet.offers;
        return this;
    }

    public static Class<? extends DataPacket> getDefaultPacket() {
        return UpdateTradePacket.class;
    }
}
