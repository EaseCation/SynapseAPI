package org.itxtech.synapseapi.multiprotocol.protocol116100.protocol;

import cn.nukkit.item.Item;
import cn.nukkit.level.particle.Particle;
import cn.nukkit.math.Vector3f;
import cn.nukkit.network.protocol.DataPacket;
import cn.nukkit.network.protocol.LevelEventPacket;
import cn.nukkit.network.protocol.ProtocolInfo;
import org.itxtech.synapseapi.multiprotocol.AbstractProtocol;
import org.itxtech.synapseapi.multiprotocol.utils.AdvancedGlobalBlockPalette;
import org.itxtech.synapseapi.multiprotocol.utils.AdvancedRuntimeItemPalette;
import org.itxtech.synapseapi.multiprotocol.utils.ParticleIdTranslator;
import org.itxtech.synapseapi.utils.ClassUtils;

public class LevelEventPacket116100 extends Packet116100 {

    public static final int NETWORK_ID = ProtocolInfo.LEVEL_EVENT_PACKET;

    public static final int EVENT_SOUND_CLICK = 1000;
    public static final int EVENT_SOUND_CLICK_FAIL = 1001;
    public static final int EVENT_SOUND_SHOOT = 1002;
    public static final int EVENT_SOUND_DOOR = 1003;
    public static final int EVENT_SOUND_FIZZ = 1004;
    public static final int EVENT_SOUND_TNT = 1005;

    public static final int EVENT_SOUND_GHAST = 1007;
    public static final int EVENT_SOUND_BLAZE_SHOOT = 1008;
    public static final int EVENT_SOUND_GHAST_SHOOT = 1009;
    public static final int EVENT_SOUND_DOOR_BUMP = 1010;
    public static final int EVENT_SOUND_DOOR_CRASH = 1012;

    public static final int EVENT_SOUND_ENDERMAN_TELEPORT = 1018;

    public static final int EVENT_SOUND_ANVIL_BREAK = 1020;
    public static final int EVENT_SOUND_ANVIL_USE = 1021;
    public static final int EVENT_SOUND_ANVIL_FALL = 1022;

    public static final int EVENT_SOUND_ITEM_DROP = 1030;
    public static final int EVENT_SOUND_ITEM_THROWN = 1031;

    public static final int EVENT_SOUND_PORTAL = 1032;

    public static final int EVENT_SOUND_ITEM_FRAME_ITEM_ADDED = 1040;
    public static final int EVENT_SOUND_ITEM_FRAME_PLACED = 1041;
    public static final int EVENT_SOUND_ITEM_FRAME_REMOVED = 1042;
    public static final int EVENT_SOUND_ITEM_FRAME_ITEM_REMOVED = 1043;
    public static final int EVENT_SOUND_ITEM_FRAME_ITEM_ROTATED = 1044;

    public static final int EVENT_SOUND_CAMERA_TAKE_PICTURE = 1050;
    public static final int EVENT_SOUND_EXPERIENCE_ORB = 1051;
    public static final int EVENT_SOUND_TOTEM = 1052;

    public static final int EVENT_SOUND_ARMOR_STAND_BREAK = 1060;
    public static final int EVENT_SOUND_ARMOR_STAND_HIT = 1061;
    public static final int EVENT_SOUND_ARMOR_STAND_FALL = 1062;
    public static final int EVENT_SOUND_ARMOR_STAND_PLACE = 1063;

    public static final int EVENT_GUARDIAN_CURSE = 2006;

    public static final int EVENT_PARTICLE_BLOCK_FORCE_FIELD = 2008;

    public static final int EVENT_PARTICLE_PUNCH_BLOCK = 2014;

    public static final int EVENT_SOUND_BUTTON_CLICK = 3500;
    public static final int EVENT_SOUND_EXPLODE = 3501;
    public static final int EVENT_CAULDRON_DYE_ARMOR = 3502;
    public static final int EVENT_CAULDRON_CLEAN_ARMOR = 3503;
    public static final int EVENT_CAULDRON_FILL_POTION = 3504;
    public static final int EVENT_CAULDRON_TAKE_POTION = 3505;
    public static final int EVENT_SOUND_SPLASH = 3506;
    public static final int EVENT_CAULDRON_TAKE_WATER = 3507;
    public static final int EVENT_CAULDRON_ADD_DYE = 3508;
    public static final int EVENT_CAULDRON_CLEAN_BANNER = 3509;

    public static final int EVENT_PARTICLE_SHOOT = 2000;
    public static final int EVENT_PARTICLE_DESTROY = 2001;
    public static final int EVENT_PARTICLE_SPLASH = 2002;
    public static final int EVENT_PARTICLE_EYE_DESPAWN = 2003;
    public static final int EVENT_PARTICLE_SPAWN = 2004;
    public static final int EVENT_PARTICLE_BONEMEAL = 2005;

    public static final int EVENT_START_RAIN = 3001;
    public static final int EVENT_START_THUNDER = 3002;
    public static final int EVENT_STOP_RAIN = 3003;
    public static final int EVENT_STOP_THUNDER = 3004;

    public static final int EVENT_SOUND_CAULDRON = 3501;
    public static final int EVENT_SOUND_CAULDRON_DYE_ARMOR = 3502;
    public static final int EVENT_SOUND_CAULDRON_FILL_POTION = 3504;
    public static final int EVENT_SOUND_CAULDRON_FILL_WATER = 3506;

    public static final int EVENT_BLOCK_START_BREAK = 3600;
    public static final int EVENT_BLOCK_STOP_BREAK = 3601;

    public static final int EVENT_SET_DATA = 4000;

    public static final int EVENT_PLAYERS_SLEEPING = 9800;

    public static final int EVENT_ADD_PARTICLE_MASK = 0x4000;

    public int evid;
    public float x = 0;
    public float y = 0;
    public float z = 0;
    public int data = 0;

    @Override
    public int pid() {
        return NETWORK_ID;
    }

    @Override
    public void decode() {
        this.evid = this.getVarInt();
        Vector3f v = this.getVector3f();
        this.x = v.x;
        this.y = v.y;
        this.z = v.z;
        this.data = this.getVarInt();
    }

    @Override
    public void encode() {
        this.reset();
        this.putVarInt(this.evid);
        this.putVector3f(this.x, this.y, this.z);
        this.putVarInt(this.data);
    }

    @Override
    public DataPacket fromDefault(DataPacket pk, AbstractProtocol protocol, boolean netease) {
        ClassUtils.requireInstance(pk, LevelEventPacket.class);

        LevelEventPacket packet = (LevelEventPacket) pk;
        this.evid = packet.evid;
        this.x = packet.x;
        this.y = packet.y;
        this.z = packet.z;

        if (packet.evid == EVENT_PARTICLE_DESTROY || packet.evid == (short) (EVENT_ADD_PARTICLE_MASK | Particle.TYPE_TERRAIN)) {
            this.data = AdvancedGlobalBlockPalette.getOrCreateRuntimeId(protocol, netease, packet.data & 0xff, packet.data >> 8);
        } else if (packet.evid == EVENT_PARTICLE_PUNCH_BLOCK) {
            this.data = AdvancedGlobalBlockPalette.getOrCreateRuntimeId(protocol, netease, packet.data & 0xff, packet.data >> 8 & 0xff) | (packet.data >> 16 & 0xff << 4);
        } else if (packet.evid == (short) (EVENT_ADD_PARTICLE_MASK | Particle.TYPE_ITEM_BREAK)) {
            int damage = packet.data & 0xffff;
            this.data = AdvancedRuntimeItemPalette.getNetworkId(protocol, netease, AdvancedRuntimeItemPalette.getNetworkFullId(protocol, netease, Item.get(packet.data >> 16, damage))) << 16 | damage;
        } else {
            this.data = packet.data;
        }

        if ((this.evid & EVENT_ADD_PARTICLE_MASK) == EVENT_ADD_PARTICLE_MASK) {
            this.evid = (short) (EVENT_ADD_PARTICLE_MASK | ParticleIdTranslator.translateTo(protocol, this.evid ^ EVENT_ADD_PARTICLE_MASK));
        }

        return this;
    }

    public static Class<? extends DataPacket> getDefaultPacket() {
        return LevelEventPacket.class;
    }
}
