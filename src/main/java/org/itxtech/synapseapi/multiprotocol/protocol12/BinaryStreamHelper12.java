package org.itxtech.synapseapi.multiprotocol.protocol12;

import cn.nukkit.level.GameRule;
import cn.nukkit.level.GameRules;
import cn.nukkit.level.GameRules.Value;
import cn.nukkit.utils.BinaryStream;
import cn.nukkit.utils.BinaryStream.BinaryStreamHelper;
import org.itxtech.synapseapi.multiprotocol.AbstractProtocol;
import org.itxtech.synapseapi.multiprotocol.utils.AdvancedBinaryStreamHelper;

import java.util.List;
import java.util.Map.Entry;

public class BinaryStreamHelper12 extends BinaryStreamHelper implements AdvancedBinaryStreamHelper {

    public static BinaryStreamHelper12 create() {
        return new BinaryStreamHelper12();
    }

    protected AbstractProtocol protocol;

    @Override
    public final void setProtocol(AbstractProtocol protocol) {
        if (this.protocol == null) {
            this.protocol = protocol;
        }
    }

    @Override
    public final AbstractProtocol getProtocol() {
        return this.protocol;
    }

    @Override
    public void putGameRules(BinaryStream stream, GameRules gameRules) {
        if (gameRules == null) {
            stream.putUnsignedVarInt(0);
            return;
        }

        List<Entry<GameRule, Value>> rules = gameRules.getGameRules().entrySet().stream()
                .filter(entry -> entry.getKey().getProtocol() <= this.protocol.getProtocolStart())
                .toList();
        stream.putUnsignedVarInt(rules.size());
        rules.forEach(entry -> {
            stream.putString(entry.getKey().getName().toLowerCase());
            entry.getValue().write(stream);
        });
    }
}
