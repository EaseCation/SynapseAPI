package org.itxtech.synapseapi.multiprotocol.protocol12610;

import cn.nukkit.math.BlockVector3;
import cn.nukkit.utils.BinaryStream;
import org.itxtech.synapseapi.multiprotocol.protocol126.BinaryStreamHelper126;

public class BinaryStreamHelper12610 extends BinaryStreamHelper126 {
    public static BinaryStreamHelper12610 create() {
        return new BinaryStreamHelper12610();
    }

    @Override
    public String getGameVersion() {
        return "1.26.10";
    }

    @Override
    public BlockVector3 getBlockVector3(BinaryStream stream) {
        return new BlockVector3(stream.getVarInt(), stream.getVarInt(), stream.getVarInt());
    }

    @Override
    public void putBlockVector3(BinaryStream stream, int x, int y, int z) {
        stream.putVarInt(x);
        stream.putVarInt(y);
        stream.putVarInt(z);
    }
}
