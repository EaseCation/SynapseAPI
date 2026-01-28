package org.itxtech.synapseapi.multiprotocol.protocol12160;

import cn.nukkit.network.protocol.types.AbilityLayer;
import cn.nukkit.network.protocol.types.StructureBlockType;
import cn.nukkit.network.protocol.types.StructureEditorData;
import cn.nukkit.utils.BinaryStream;
import org.itxtech.synapseapi.multiprotocol.protocol12150.BinaryStreamHelper12150;

public class BinaryStreamHelper12160 extends BinaryStreamHelper12150 {
    public static BinaryStreamHelper12160 create() {
        return new BinaryStreamHelper12160();
    }

    @Override
    public String getGameVersion() {
        return "1.21.60";
    }

    @Override
    public boolean isNetEase() {
        return false;
    }

    @Override
    public void putAbilityLayer(BinaryStream stream, AbilityLayer layer) {
        stream.putLShort(layer.type);

        stream.putLInt(getAbilityFlags(layer.abilitiesSet));
        stream.putLInt(getAbilityFlags(layer.abilityValues));

        stream.putLFloat(layer.flySpeed);
        stream.putLFloat(layer.verticalFlySpeed);
        stream.putLFloat(layer.walkSpeed);
    }

    @Override
    public StructureEditorData getStructureEditorData(BinaryStream stream) {
        StructureEditorData data = new StructureEditorData();
        data.name = stream.getString();
        data.filteredName = stream.getString();
        data.dataField = stream.getString();
        data.includePlayers = stream.getBoolean();
        data.boundingBoxVisible = stream.getBoolean();
        data.type = StructureBlockType.getValues()[stream.getVarInt()];
        data.settings = getStructureSettings(stream);
        data.redstoneSaveToDisk = stream.getVarInt() != 0;
        return data;
    }
}
