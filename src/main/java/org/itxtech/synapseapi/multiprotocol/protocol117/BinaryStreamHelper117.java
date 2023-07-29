package org.itxtech.synapseapi.multiprotocol.protocol117;

import cn.nukkit.level.GameRule;
import cn.nukkit.level.GameRules;
import cn.nukkit.utils.BinaryStream;
import org.itxtech.synapseapi.multiprotocol.protocol116220.BinaryStreamHelper116220;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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

        List<Map.Entry<GameRule, GameRules.Value>> rules = gameRules.getGameRules().entrySet().stream()
                .filter(entry -> entry.getKey().getProtocol() <= this.protocol.getProtocolStart())
                .toList();
        stream.putUnsignedVarInt(rules.size());
        rules.forEach(entry -> {
            stream.putString(entry.getKey().getName().toLowerCase());
            stream.putBoolean(false); // isEditable
            entry.getValue().write(stream);
        });
    }
}
