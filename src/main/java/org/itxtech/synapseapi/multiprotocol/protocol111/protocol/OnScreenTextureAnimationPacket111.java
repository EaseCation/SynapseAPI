package org.itxtech.synapseapi.multiprotocol.protocol111.protocol;

import cn.nukkit.network.protocol.ProtocolInfo;
import lombok.ToString;

@ToString
public class OnScreenTextureAnimationPacket111 extends Packet111 {

    public int effectId;

    @Override
    public int pid() {
        return ProtocolInfo.ON_SCREEN_TEXTURE_ANIMATION_PACKET;
    }

    @Override
    public void decode() {
    }

    @Override
    public void encode() {
        this.reset();
        this.putLInt(this.effectId);
    }
}
