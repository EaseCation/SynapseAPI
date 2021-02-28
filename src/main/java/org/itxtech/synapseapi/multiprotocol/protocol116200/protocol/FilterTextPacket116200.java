package org.itxtech.synapseapi.multiprotocol.protocol116200.protocol;

import cn.nukkit.network.protocol.ProtocolInfo;
import lombok.ToString;

/**
 * 用于铁砧重命名物品时的NSFW内容过滤. 客户端每键入一个字符都会发一个FilterTextPacket, 服务端需要响应过滤后的字符串.
 */
@ToString
public class FilterTextPacket116200 extends Packet116200 {

    public static final int NETWORK_ID = ProtocolInfo.FILTER_TEXT_PACKET;

    public String text;
    public boolean fromServer;

    @Override
    public int pid() {
        return NETWORK_ID;
    }

    @Override
    public void decode() {
        this.text = this.getString();
        this.fromServer = this.getBoolean();
    }

    @Override
    public void encode() {
        this.reset();
        this.putString(this.text);
        this.putBoolean(this.fromServer);
    }
}
