package org.itxtech.synapseapi.multiprotocol.protocol12160;

import cn.nukkit.network.protocol.types.AbilityLayer;
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
    public void putAbilityLayer(BinaryStream stream, AbilityLayer layer) {
        stream.putLShort(layer.type);

        stream.putLInt(getAbilityFlags(layer.abilitiesSet));
        stream.putLInt(getAbilityFlags(layer.abilityValues));

        stream.putLFloat(layer.flySpeed);
        stream.putLFloat(layer.verticalFlySpeed);
        stream.putLFloat(layer.walkSpeed);
    }
}
