package org.itxtech.synapseapi.multiprotocol.protocol11910;

import cn.nukkit.network.protocol.types.AbilityLayer;
import cn.nukkit.network.protocol.types.PlayerAbility;
import cn.nukkit.utils.BinaryStream;
import org.itxtech.synapseapi.multiprotocol.protocol119.BinaryStreamHelper119;

import java.util.Set;

public class BinaryStreamHelper11910 extends BinaryStreamHelper119 {

    public static BinaryStreamHelper11910 create() {
        return new BinaryStreamHelper11910();
    }

    @Override
    public String getGameVersion() {
        return "1.19.10";
    }

    @Override
    public void putAbilityLayer(BinaryStream stream, AbilityLayer layer) {
        stream.putLShort(layer.type);

        stream.putLInt(getAbilityFlags(layer.abilitiesSet));
        stream.putLInt(getAbilityFlags(layer.abilityValues));

        stream.putLFloat(layer.flySpeed);
        stream.putLFloat(layer.walkSpeed);
    }

    protected static int getAbilityFlags(Set<PlayerAbility> abilities) {
        int flags = 0;
        for (PlayerAbility ability : abilities) {
            flags |= 1 << ability.ordinal();
        }
        return flags;
    }
}
