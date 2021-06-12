package org.itxtech.synapseapi.multiprotocol.protocol117;

import cn.nukkit.level.GameRule;
import cn.nukkit.level.GameRules;
import cn.nukkit.utils.BinaryStream;
import org.itxtech.synapseapi.multiprotocol.protocol116220.BinaryStreamHelper116220;

import java.util.Map;

public class BinaryStreamHelper117 extends BinaryStreamHelper116220 {

    public static BinaryStreamHelper117 create() {
        return new BinaryStreamHelper117();
    }

    @Override
    public String getGameVersion() {
        return "1.17.0";
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
            stream.putString(gameRule.getName().toLowerCase());
            stream.putBoolean(false); // isEditable
            value.write(stream);
        });
    }
}
