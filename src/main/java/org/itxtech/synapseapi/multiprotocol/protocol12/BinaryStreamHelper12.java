package org.itxtech.synapseapi.multiprotocol.protocol12;

import cn.nukkit.level.GameRule;
import cn.nukkit.level.GameRules;
import cn.nukkit.utils.BinaryStream;
import cn.nukkit.utils.BinaryStream.BinaryStreamHelper;
import org.itxtech.synapseapi.multiprotocol.AbstractProtocol;
import org.itxtech.synapseapi.multiprotocol.utils.AdvancedBinaryStreamHelper;

import java.util.Map;

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

        Map<GameRule, GameRules.Value> rules = gameRules.getGameRules();
        stream.putUnsignedVarInt(rules.size());
        rules.forEach((gameRule, value) -> {
            if (gameRule.getProtocol() <= this.protocol.getProtocolStart()) {
                stream.putString(gameRule.getName().toLowerCase());
                value.write(stream);
            }
        });
    }
}
