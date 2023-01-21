package org.itxtech.synapseapi.multiprotocol.protocol11910.protocol;

import cn.nukkit.Player;
import cn.nukkit.command.data.CommandPermission;
import cn.nukkit.network.protocol.ProtocolInfo;
import cn.nukkit.network.protocol.types.PlayerAbility;
import lombok.AllArgsConstructor;
import lombok.ToString;

import java.util.Map;
import java.util.Map.Entry;

@ToString
public class UpdateAbilitiesPacket11910 extends Packet11910 {
    public static final int NETWORK_ID = ProtocolInfo.UPDATE_ABILITIES_PACKET;

    public static final int LAYER_CACHE = 0;
    public static final int LAYER_BASE = 1;
    public static final int LAYER_SPECTATOR = 2;
    public static final int LAYER_COMMANDS = 3;
    /**
     * @since 1.19.40
     */
    public static final int LAYER_EDITOR = 4;

    public long entityUniqueId;
    public int playerPermission = Player.PERMISSION_MEMBER;
    public CommandPermission commandPermission = CommandPermission.ALL;
    public AbilityLayer[] abilityLayers;

    @Override
    public int pid() {
        return NETWORK_ID;
    }

    @Override
    public void decode() {
    }

    @Override
    public void encode() {
        reset();
        putLLong(entityUniqueId);
        putByte((byte) playerPermission);
        putByte((byte) commandPermission.ordinal());

        putByte((byte) abilityLayers.length);
        for (AbilityLayer layer : abilityLayers) {
            putLShort(layer.type);

            int keyFlags = 0;
            int valueFlags = 0;
            for (Entry<PlayerAbility, Boolean> entry : layer.abilities.entrySet()) {
                int abilityBit = 1 << entry.getKey().ordinal();
                keyFlags |= abilityBit;
                if (entry.getValue()) {
                    valueFlags |= abilityBit;
                }
            }

            putLInt(keyFlags);
            putLInt(valueFlags);

            putLFloat(layer.flySpeed);
            putLFloat(layer.walkSpeed);
        }
    }

    @ToString
    @AllArgsConstructor
    public static class AbilityLayer {
        public int type;
        public Map<PlayerAbility, Boolean> abilities;
        public float flySpeed;
        public float walkSpeed;
    }
}
