package org.itxtech.synapseapi.multiprotocol.protocol16.protocol;

import cn.nukkit.network.protocol.DataPacket;
import cn.nukkit.utils.Binary;

public class CompatibilityPacket16 extends Packet16 {

    public DataPacket origin;

    @Override
    public int pid() {
        return -1;
    }

    @Override
    public void encode() {
        super.superReset();
        origin.setHelper(this.helper);
        origin.neteaseMode = this.neteaseMode;
        origin.tryEncode();
        byte[] pid = Binary.subBytes(origin.getBuffer(), 0, 1);
        this.put(pid);
        byte[] data = Binary.subBytes(origin.getBuffer(), 3, origin.getBuffer().length);
        this.put(data);
    }

    @Override
    public void decode() {

    }
}
