package org.itxtech.synapseapi.multiprotocol.protocol11940.protocol;

import cn.nukkit.entity.attribute.Attribute;
import cn.nukkit.entity.Entities;
import cn.nukkit.entity.data.EntityMetadata;
import cn.nukkit.entity.item.*;
import cn.nukkit.entity.mob.*;
import cn.nukkit.entity.passive.*;
import cn.nukkit.entity.projectile.*;
import cn.nukkit.entity.property.EntityPropertyRegistry;
import cn.nukkit.entity.weather.EntityLightning;
import cn.nukkit.network.protocol.AddEntityPacket;
import cn.nukkit.network.protocol.DataPacket;
import cn.nukkit.network.protocol.ProtocolInfo;
import cn.nukkit.network.protocol.types.EntityLink;
import com.google.common.collect.ImmutableMap;
import it.unimi.dsi.fastutil.Pair;
import it.unimi.dsi.fastutil.ints.Int2FloatMap;
import it.unimi.dsi.fastutil.ints.Int2FloatOpenHashMap;
import it.unimi.dsi.fastutil.ints.Int2IntMap;
import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;
import lombok.ToString;
import org.itxtech.synapseapi.multiprotocol.AbstractProtocol;
import org.itxtech.synapseapi.multiprotocol.utils.EntityMetadataGenerator;
import org.itxtech.synapseapi.utils.ClassUtils;

@ToString
public class AddEntityPacket11940 extends Packet11940 {
    public static final int NETWORK_ID = ProtocolInfo.ADD_ACTOR_PACKET;

    public static final ImmutableMap<Integer, String> LEGACY_IDS = ImmutableMap.<Integer, String>builder()
            .put(51, "minecraft:npc")
            .put(63, "minecraft:player")
            .put(EntityWitherSkeleton.NETWORK_ID, "minecraft:wither_skeleton")
            .put(EntityHusk.NETWORK_ID, "minecraft:husk")
            .put(EntityStray.NETWORK_ID, "minecraft:stray")
            .put(EntityWitch.NETWORK_ID, "minecraft:witch")
            .put(EntityZombieVillagerV1.NETWORK_ID, "minecraft:zombie_villager")
            .put(EntityBlaze.NETWORK_ID, "minecraft:blaze")
            .put(EntityMagmaCube.NETWORK_ID, "minecraft:magma_cube")
            .put(EntityGhast.NETWORK_ID, "minecraft:ghast")
            .put(EntityCaveSpider.NETWORK_ID, "minecraft:cave_spider")
            .put(EntitySilverfish.NETWORK_ID, "minecraft:silverfish")
            .put(EntityEnderman.NETWORK_ID, "minecraft:enderman")
            .put(EntitySlime.NETWORK_ID, "minecraft:slime")
            .put(EntityZombiePigman.NETWORK_ID, "minecraft:zombie_pigman")
            .put(EntitySpider.NETWORK_ID, "minecraft:spider")
            .put(EntitySkeleton.NETWORK_ID, "minecraft:skeleton")
            .put(EntityCreeper.NETWORK_ID, "minecraft:creeper")
            .put(EntityZombie.NETWORK_ID, "minecraft:zombie")
            .put(EntitySkeletonHorse.NETWORK_ID, "minecraft:skeleton_horse")
            .put(EntityMule.NETWORK_ID, "minecraft:mule")
            .put(EntityDonkey.NETWORK_ID, "minecraft:donkey")
            .put(EntityDolphin.NETWORK_ID, "minecraft:dolphin")
            .put(EntityTropicalFish.NETWORK_ID, "minecraft:tropicalfish")
            .put(EntityWolf.NETWORK_ID, "minecraft:wolf")
            .put(EntitySquid.NETWORK_ID, "minecraft:squid")
            .put(EntityDrowned.NETWORK_ID, "minecraft:drowned")
            .put(EntitySheep.NETWORK_ID, "minecraft:sheep")
            .put(EntityMooshroom.NETWORK_ID, "minecraft:mooshroom")
            .put(EntityPanda.NETWORK_ID, "minecraft:panda")
            .put(EntitySalmon.NETWORK_ID, "minecraft:salmon")
            .put(EntityPig.NETWORK_ID, "minecraft:pig")
            .put(EntityVillagerV1.NETWORK_ID, "minecraft:villager")
            .put(EntityCod.NETWORK_ID, "minecraft:cod")
            .put(EntityPufferfish.NETWORK_ID, "minecraft:pufferfish")
            .put(EntityCow.NETWORK_ID, "minecraft:cow")
            .put(EntityChicken.NETWORK_ID, "minecraft:chicken")
            .put(107, "minecraft:balloon")
            .put(EntityLlama.NETWORK_ID, "minecraft:llama")
            .put(20, "minecraft:iron_golem")
            .put(EntityRabbit.NETWORK_ID, "minecraft:rabbit")
            .put(21, "minecraft:snow_golem")
            .put(EntityBat.NETWORK_ID, "minecraft:bat")
            .put(EntityOcelot.NETWORK_ID, "minecraft:ocelot")
            .put(EntityHorse.NETWORK_ID, "minecraft:horse")
            .put(EntityCat.NETWORK_ID, "minecraft:cat")
            .put(EntityPolarBear.NETWORK_ID, "minecraft:polar_bear")
            .put(EntityZombieHorse.NETWORK_ID, "minecraft:zombie_horse")
            .put(EntityTurtle.NETWORK_ID, "minecraft:turtle")
            .put(EntityParrot.NETWORK_ID, "minecraft:parrot")
            .put(EntityGuardian.NETWORK_ID, "minecraft:guardian")
            .put(EntityElderGuardian.NETWORK_ID, "minecraft:elder_guardian")
            .put(EntityVindicator.NETWORK_ID, "minecraft:vindicator")
            .put(EntityWither.NETWORK_ID, "minecraft:wither")
            .put(EntityEnderDragon.NETWORK_ID, "minecraft:ender_dragon")
            .put(EntityShulker.NETWORK_ID, "minecraft:shulker")
            .put(EntityEndermite.NETWORK_ID, "minecraft:endermite")
            .put(EntityMinecartEmpty.NETWORK_ID, "minecraft:minecart")
            .put(EntityMinecartHopper.NETWORK_ID, "minecraft:hopper_minecart")
            .put(EntityMinecartTNT.NETWORK_ID, "minecraft:tnt_minecart")
            .put(EntityMinecartChest.NETWORK_ID, "minecraft:chest_minecart")
            .put(100, "minecraft:command_block_minecart")
            .put(61, "minecraft:armor_stand")
            .put(EntityItem.NETWORK_ID, "minecraft:item")
            .put(EntityPrimedTNT.NETWORK_ID, "minecraft:tnt")
            .put(EntityFallingBlock.NETWORK_ID, "minecraft:falling_block")
            .put(EntityExpBottle.NETWORK_ID, "minecraft:xp_bottle")
            .put(EntityXPOrb.NETWORK_ID, "minecraft:xp_orb")
            .put(70, "minecraft:eye_of_ender_signal")
            .put(EntityEndCrystal.NETWORK_ID, "minecraft:ender_crystal")
            .put(76, "minecraft:shulker_bullet")
            .put(EntityFishingHook.NETWORK_ID, "minecraft:fishing_hook")
            .put(79, "minecraft:dragon_fireball")
            .put(EntityArrow.NETWORK_ID, "minecraft:arrow")
            .put(EntitySnowball.NETWORK_ID, "minecraft:snowball")
            .put(EntityEgg.NETWORK_ID, "minecraft:egg")
            .put(EntityPainting.NETWORK_ID, "minecraft:painting")
            .put(EntityThrownTrident.NETWORK_ID, "minecraft:thrown_trident")
            .put(85, "minecraft:fireball")
            .put(EntityPotion.NETWORK_ID, "minecraft:splash_potion")
            .put(EntityEnderPearl.NETWORK_ID, "minecraft:ender_pearl")
            .put(88, "minecraft:leash_knot")
            .put(89, "minecraft:wither_skull")
            .put(91, "minecraft:wither_skull_dangerous")
            .put(EntityBoat.NETWORK_ID, "minecraft:boat")
            .put(EntityLightning.NETWORK_ID, "minecraft:lightning_bolt")
            .put(94, "minecraft:small_fireball")
            .put(102, "minecraft:llama_spit")
            .put(95, "minecraft:area_effect_cloud")
            .put(101, "minecraft:lingering_potion")
            .put(EntityFirework.NETWORK_ID, "minecraft:fireworks_rocket")
            .put(103, "minecraft:evocation_fang")
            .put(104, "minecraft:evocation_illager")
            .put(EntityVex.NETWORK_ID, "minecraft:vex")
            .put(56, "minecraft:agent")
            .put(106, "minecraft:ice_bomb")
            .put(EntityPhantom.NETWORK_ID, "minecraft:phantom")
            .put(62, "minecraft:tripod_camera")
            .put(EntityPillager.NETWORK_ID, "minecraft:pillager")
            .put(EntityWanderingTrader.NETWORK_ID, "minecraft:wandering_trader")
            .put(EntityRavager.NETWORK_ID, "minecraft:ravager")
            .put(EntityVillager.NETWORK_ID, "minecraft:villager_v2")
            .put(EntityZombieVillager.NETWORK_ID, "minecraft:zombie_villager_v2")
            .put(121, "minecraft:fox")
            .build();

    @Override
    public int pid() {
        return NETWORK_ID;
    }

    public long entityUniqueId;
    public long entityRuntimeId;
    public int type;
    public String id;
    public float x;
    public float y;
    public float z;
    public float speedX = 0f;
    public float speedY = 0f;
    public float speedZ = 0f;
    public float yaw;
    public float pitch;
    public float headYaw;
    public float bodyYaw = Float.MIN_VALUE;
    public EntityMetadata metadata = new EntityMetadata();
    public Int2IntMap intProperties = new Int2IntOpenHashMap();
    public Int2FloatMap floatProperties = new Int2FloatOpenHashMap();
    public Attribute[] attributes = new Attribute[0];
    public EntityLink[] links = new EntityLink[0];

    @Override
    public void decode() {
    }

    @Override
    public void encode() {
        this.reset();
        this.putEntityUniqueId(this.entityUniqueId);
        this.putEntityRuntimeId(this.entityRuntimeId);
        if (id == null) {
            id = Entities.getIdentifierByType(type);
        }
        this.putString(this.id);
        this.putVector3f(this.x, this.y, this.z);
        this.putVector3f(this.speedX, this.speedY, this.speedZ);
        this.putLFloat(this.pitch);
        this.putLFloat(this.yaw);
        this.putLFloat(this.headYaw);
        this.putLFloat(this.bodyYaw == Float.MIN_VALUE ? this.yaw : this.bodyYaw);
        this.putAttributeList(this.attributes);
        this.putEntityMetadata(this.metadata);
        this.putUnsignedVarInt(this.intProperties.size());
        for (Int2IntMap.Entry property : this.intProperties.int2IntEntrySet()) {
            this.putUnsignedVarInt(property.getIntKey());
            this.putVarInt(property.getIntValue());
        }
        this.putUnsignedVarInt(this.floatProperties.size());
        for (Int2FloatMap.Entry property : this.floatProperties.int2FloatEntrySet()) {
            this.putUnsignedVarInt(property.getIntKey());
            this.putLFloat(property.getFloatValue());
        }
        this.putUnsignedVarInt(this.links.length);
        for (EntityLink link : this.links) {
            this.putEntityLink(link);
        }
    }

    @Override
    public DataPacket fromDefault(DataPacket pk, AbstractProtocol protocol, boolean netease) {
        ClassUtils.requireInstance(pk, AddEntityPacket.class);

        AddEntityPacket packet = (AddEntityPacket) pk;

        this.entityUniqueId = packet.entityUniqueId;
        this.entityRuntimeId = packet.entityRuntimeId;
        this.type = packet.type;
        this.id = packet.id;
        this.x = packet.x;
        this.y = packet.y;
        this.z = packet.z;
        this.speedX = packet.speedX;
        this.speedY = packet.speedY;
        this.speedZ = packet.speedZ;
        this.pitch = packet.pitch;
        this.yaw = packet.yaw;
        this.headYaw = packet.headYaw;
        this.metadata = EntityMetadataGenerator.generateFrom(packet.metadata, protocol, netease);
        this.intProperties = packet.intProperties;
        this.floatProperties = packet.floatProperties;
        this.attributes = packet.attributes;
        this.links = packet.links;

        if (this.id == null) {
            this.id = Entities.getIdentifierByType(this.type);
        }

/*
        EntityPropertiesTable properties = EntityPropertiesPalette.getPalette(protocol, netease).getProperties(this.id);
        if (properties != null) {
            for (int i = 0; i < properties.size(); i++) {
                EntityPropertyData property = properties.get(i);
                if (property.getType() == EntityPropertyType.FLOAT) {
                    this.floatProperties.put(i, property.getDefaultFloatValue());
                } else {
                    this.intProperties.put(i, property.getDefaultIntValue());
                }
            }
        }
*/
        if (intProperties.isEmpty() && floatProperties.isEmpty()) {
            Pair<Int2IntMap, Int2FloatMap> propertyValues = EntityPropertyRegistry.getProperties(this.id).getDefaultValues();
            if (propertyValues != null) {
                this.intProperties = propertyValues.left();
                this.floatProperties = propertyValues.right();
            }
        }

        return this;
    }

    public static Class<? extends DataPacket> getDefaultPacket() {
        return AddEntityPacket.class;
    }
}
