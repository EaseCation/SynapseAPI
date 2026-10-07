package org.itxtech.synapseapi.multiprotocol.protocol16.protocol;

import cn.nukkit.network.protocol.ProtocolInfo;
import cn.nukkit.utils.JsonUtil;
import lombok.ToString;
import org.itxtech.synapseapi.multiprotocol.common.netease.NetEaseJsonEvent;
import org.itxtech.synapseapi.multiprotocol.common.netease.NetEaseJsonServerEvent;

/**
 * author: MagicDroidX
 * Nukkit Project
 */
@ToString
public class NENetEaseJsonPacket16 extends Packet16 {
    public static final int NETWORK_ID = ProtocolInfo.PACKET_NETEASE_JSON;

    public NetEaseJsonEvent event;

    @Override
    public int pid() {
        return NETWORK_ID;
    }

    @Override
    public void decode() {
        this.event = JsonUtil.UNTRUSTED_JSON_MAPPER.readValue(this.getByteArray(), NetEaseJsonServerEvent.class);
    }

    @Override
    public void encode() {
        this.reset();
        this.putByteArray(this.event.toJsonAsBytes());
    }

}
