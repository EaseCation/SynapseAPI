package org.itxtech.synapseapi.multiprotocol.protocol117;

import cn.nukkit.level.GameRule;
import cn.nukkit.level.GameRules;
import cn.nukkit.math.Rotation;
import cn.nukkit.network.protocol.types.StructureAnimationMode;
import cn.nukkit.network.protocol.types.StructureMirror;
import cn.nukkit.network.protocol.types.StructureSettings;
import cn.nukkit.utils.BinaryStream;
import org.itxtech.synapseapi.multiprotocol.protocol116220.BinaryStreamHelper116220;

import java.util.List;
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
    public void putGameRules(BinaryStream stream, GameRules gameRules, boolean network) {
        if (gameRules == null) {
            stream.putUnsignedVarInt(0);
            return;
        }

        List<Map.Entry<GameRule, GameRules.Value>> rules = gameRules.getGameRules().entrySet().stream()
                .filter(entry -> entry.getKey().getProtocol() <= this.protocol.getProtocolStart())
                .toList();
        stream.putUnsignedVarInt(rules.size());
        rules.forEach(entry -> {
            stream.putString(entry.getKey().getBedrockName());
            stream.putBoolean(false); // isEditable
            entry.getValue().write(stream, network);
        });
    }

    @Override
    public StructureSettings getStructureSettings(BinaryStream stream) {
        StructureSettings settings = new StructureSettings();
        settings.paletteName = stream.getString();
        settings.ignoreEntities = stream.getBoolean();
        settings.ignoreBlocks = stream.getBoolean();
        settings.size = stream.getBlockVector3();
        settings.offset = stream.getBlockVector3();
        settings.lastEditedByEntityUniqueId = stream.getEntityUniqueId();
        settings.rotation = Rotation.getValues()[stream.getByte()];
        settings.mirror = StructureMirror.getValues()[stream.getByte()];
        settings.animationMode = StructureAnimationMode.getValues()[stream.getByte()];
        settings.animationSeconds = stream.getLFloat();
        settings.integrityValue = stream.getLFloat();
        settings.integritySeed = stream.getLInt();
        settings.pivot = stream.getVector3f();
        return settings;
    }
}
