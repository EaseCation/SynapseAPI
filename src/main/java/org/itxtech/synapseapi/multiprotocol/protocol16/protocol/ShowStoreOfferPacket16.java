package org.itxtech.synapseapi.multiprotocol.protocol16.protocol;

import cn.nukkit.network.protocol.ProtocolInfo;
import lombok.ToString;

@ToString
public class ShowStoreOfferPacket16 extends Packet16 {

    public String productId;
    public boolean showAll;

    @Override
    public int pid() {
        return ProtocolInfo.SHOW_STORE_OFFER_PACKET;
    }

    @Override
    public void decode() {
    }

    @Override
    public void encode() {
        this.reset();
        this.putString(this.productId);
        this.putBoolean(this.showAll);
    }

}
