package org.itxtech.synapseapi.network.protocol.spp;


import cn.nukkit.utils.BinaryStream;
import io.netty.channel.Channel;
import javax.annotation.Nullable;

public abstract class SynapseDataPacket extends BinaryStream implements Cloneable {

    public boolean isEncoded = false;

    /** 本机 Synapse 解帧后的接入时间，不进入协议编码；零表示入口未记录。 */
    public long receivedNanos;

    /** 原 TCP 来源只在本机保存，重连不能给旧排队包重新绑定来源。 */
    @Nullable
    public Channel receivedChannel;


    public abstract byte pid();

    public abstract void decode();

    public abstract void encode();

    @Override
    public void reset() {
        super.reset();
    }

    public SynapseDataPacket clean() {
        this.setBuffer(null);

        this.isEncoded = false;
        this.receivedNanos = 0;
        this.receivedChannel = null;
        this.offset = 0;
        return this;
    }

    @Override
    public SynapseDataPacket clone() {
        try {
            return (SynapseDataPacket) super.clone();
        } catch (CloneNotSupportedException e) {
            return null;
        }
    }

}
