package org.itxtech.synapseapi.multiprotocol.protocol11910.protocol;

import cn.nukkit.Player;
import cn.nukkit.command.data.CommandPermission;
import cn.nukkit.network.protocol.ProtocolInfo;
import cn.nukkit.network.protocol.types.PlayerAbility;
import lombok.AllArgsConstructor;
import lombok.ToString;

import java.util.Set;

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
    /**
     * @since 1.21.20
     */
    public static final int LAYER_LOADING_SCREEN = 5;

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

            putLInt(getFlags(layer.abilitiesSet));
            putLInt(getFlags(layer.abilityValues));

            putLFloat(layer.flySpeed);
            putLFloat(layer.walkSpeed);
        }
    }

    private static int getFlags(Set<PlayerAbility> abilities) {
        int flags = 0;
        for (PlayerAbility ability : abilities) {
            flags |= 1 << ability.ordinal();
        }
        return flags;
    }

    @ToString
    @AllArgsConstructor
    public static class AbilityLayer {
        public int type;
        public Set<PlayerAbility> abilitiesSet;
        public Set<PlayerAbility> abilityValues;
        public float flySpeed;
        public float walkSpeed;
    }
}
