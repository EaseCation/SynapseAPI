package org.itxtech.synapseapi.multiprotocol.utils;

import cn.nukkit.entity.EntityFullNames;
import cn.nukkit.network.protocol.BatchPacket;
import cn.nukkit.network.protocol.DataPacket;
import lombok.extern.log4j.Log4j2;
import org.itxtech.synapseapi.multiprotocol.AbstractProtocol;
import org.itxtech.synapseapi.multiprotocol.protocol117.protocol.SyncEntityPropertyPacket117;
import org.itxtech.synapseapi.multiprotocol.utils.entityproperty.EntityPropertiesPaletteBase;
import org.itxtech.synapseapi.multiprotocol.utils.entityproperty.EntityPropertiesPaletteLegacy;
import org.itxtech.synapseapi.multiprotocol.utils.entityproperty.data.*;

import javax.annotation.Nullable;
import java.util.*;
import java.util.zip.Deflater;

/**
 * @deprecated use {@link cn.nukkit.entity.property.EntityPropertyRegistry} and {@link EntityPropertiesCache}
 */
@Deprecated
@Log4j2
public final class EntityPropertiesPalette {

    private static final Map<AbstractProtocol, EntityPropertiesPaletteInterface> palettes = new EnumMap<>(AbstractProtocol.class);
    private static final Map<AbstractProtocol, BatchPacket> PACKETS = new EnumMap<>(AbstractProtocol.class);

    static {
        log.debug("Loading entity properties...");

        EntityPropertiesPaletteInterface palette117 = new EntityPropertiesPaletteBase();
        EntityPropertiesPaletteInterface palette11970 = new EntityPropertiesPaletteBase(
            new EntityPropertiesTable(EntityFullNames.BEE,
                new EntityPropertyDataBool("minecraft:has_nectar")
            )
        );
        EntityPropertiesPaletteInterface palette12080 = new EntityPropertiesPaletteBase(palette11970,
            new EntityPropertiesTable(EntityFullNames.ARMADILLO,
                new EntityPropertyDataEnum("minecraft:armadillo_state",
                    "unrolled",
                    "rolled_up",
                    "rolled_up_peeking",
                    "rolled_up_relaxing",
                    "rolled_up_unrolling"
                )
            )
        );
        EntityPropertiesPaletteInterface palette12150 = new EntityPropertiesPaletteBase(palette12080,
                new EntityPropertiesTable(EntityFullNames.CREAKING,
                        new EntityPropertyDataEnum("minecraft:creaking_state",
                                "neutral",
                                "hostile_observed",
                                "hostile_unobserved",
                                "twitching",
                                "crumbling"
                        ),
                        new EntityPropertyDataInt("minecraft:creaking_swaying_ticks", 6, 0)
                )
        );
        EntityPropertiesPaletteInterface palette12170 = new EntityPropertiesPaletteBase(palette12150,
                new EntityPropertiesTable(EntityFullNames.CHICKEN,
                        new EntityPropertyDataEnum("minecraft:climate_variant",
                                "temperate",
                                "warm",
                                "cold"
                        )
                ),
                new EntityPropertiesTable(EntityFullNames.COW,
                        new EntityPropertyDataEnum("minecraft:climate_variant",
                                "temperate",
                                "warm",
                                "cold"
                        )
                ),
                new EntityPropertiesTable(EntityFullNames.EGG,
                        new EntityPropertyDataEnum("minecraft:climate_variant",
                                "temperate",
                                "warm",
                                "cold"
                        )
                ),
                new EntityPropertiesTable(EntityFullNames.PIG,
                        new EntityPropertyDataEnum("minecraft:climate_variant",
                                "temperate",
                                "warm",
                                "cold"
                        )
                ),
                new EntityPropertiesTable(EntityFullNames.WOLF,
                        new EntityPropertyDataEnum("minecraft:sound_variant",
                                "default",
                                "big",
                                "cute",
                                "grumpy",
                                "mad",
                                "puglin",
                                "sad"
                        )
                )
        );

/*
        register(AbstractProtocol.PROTOCOL_117, palette117, null);
        register(AbstractProtocol.PROTOCOL_117_10, palette117, null);
        register(AbstractProtocol.PROTOCOL_117_30, palette117, null);
        register(AbstractProtocol.PROTOCOL_117_40, palette117, null);
        register(AbstractProtocol.PROTOCOL_118, palette117, null);
        register(AbstractProtocol.PROTOCOL_118_10, palette117, null);
        register(AbstractProtocol.PROTOCOL_118_30, palette117, null);
        register(AbstractProtocol.PROTOCOL_118_30_NE, palette117, null);
        register(AbstractProtocol.PROTOCOL_119, palette117, null);
        register(AbstractProtocol.PROTOCOL_119_10, palette117, null);
        register(AbstractProtocol.PROTOCOL_119_20, palette117, null);
        register(AbstractProtocol.PROTOCOL_119_21, palette117, null);
        register(AbstractProtocol.PROTOCOL_119_30, palette117, null);
        register(AbstractProtocol.PROTOCOL_119_40, palette117, null);
        register(AbstractProtocol.PROTOCOL_119_50, palette117, null);
        register(AbstractProtocol.PROTOCOL_119_60, palette117, null);
        register(AbstractProtocol.PROTOCOL_119_63, palette117, null);
        register(AbstractProtocol.PROTOCOL_119_70, palette11970, null);
        register(AbstractProtocol.PROTOCOL_119_80, palette11970, null);
        register(AbstractProtocol.PROTOCOL_120, palette11970, null);
*/
        register(AbstractProtocol.PROTOCOL_120_10, palette11970, null);
        register(AbstractProtocol.PROTOCOL_120_30, palette11970, null);
        register(AbstractProtocol.PROTOCOL_120_40, palette11970, null);
        register(AbstractProtocol.PROTOCOL_120_50, palette11970, null);
        register(AbstractProtocol.PROTOCOL_120_60, palette11970, null);
        register(AbstractProtocol.PROTOCOL_120_70, palette11970, null);
        register(AbstractProtocol.PROTOCOL_120_80, palette12080, null);
        register(AbstractProtocol.PROTOCOL_121, palette12080, null);
        register(AbstractProtocol.PROTOCOL_121_2, palette12080, null);
        register(AbstractProtocol.PROTOCOL_121_20, palette12080, null);
        register(AbstractProtocol.PROTOCOL_121_30, palette12080, null);
        register(AbstractProtocol.PROTOCOL_121_40, palette12080, null);
        register(AbstractProtocol.PROTOCOL_121_50, palette12150, null);
        register(AbstractProtocol.PROTOCOL_121_60, palette12150, null);
        register(AbstractProtocol.PROTOCOL_121_70, palette12170, null);

        cachePackets();
    }

    private static void cachePackets() {
        log.debug("cache entity properties...");

        for (AbstractProtocol protocol : AbstractProtocol.getValues()) {
            if (protocol.getProtocolStart() < AbstractProtocol.PROTOCOL_117.getProtocolStart()) {
                continue;
            }
            if (protocol.ordinal() < AbstractProtocol.FIRST_AVAILABLE_PROTOCOL.ordinal()) {
                // drop support for unavailable versions
                continue;
            }

            List<DataPacket> packets = new ArrayList<>();
            for (byte[] nbt : getCompiledPalette(protocol, false).values()) {
                SyncEntityPropertyPacket117 packet = new SyncEntityPropertyPacket117();
                packet.nbt = nbt;
                packet.setHelper(protocol.getHelper());
                packets.add(packet);
            }
            BatchPacket batch = BatchPacket.compress(Deflater.BEST_COMPRESSION, true, packets.toArray(new DataPacket[0]));

            PACKETS.put(protocol, batch);
        }
    }

    private static void register(AbstractProtocol protocol, EntityPropertiesPaletteInterface palette, EntityPropertiesPaletteInterface paletteNetEase) {
        Objects.requireNonNull(palette);
        palettes.put(protocol, palette);
    }

    public static void registerCustomProperties(EntityPropertiesTable properties) {
        for (EntityPropertiesPaletteInterface data : palettes.values()) {
            data.registerProperties(properties);
        }
    }

    public static void rebuildNetworkCache() {
        for (EntityPropertiesPaletteInterface data : palettes.values()) {
            data.rebuildNetworkCache();
        }

        cachePackets();
    }

    public static EntityPropertiesPaletteInterface getPalette(AbstractProtocol protocol, boolean netease) {
        return palettes.getOrDefault(protocol, EntityPropertiesPaletteLegacy.INSTANCE);
    }

    public static Set<String> getAllEntityIdentifiers(AbstractProtocol protocol, boolean netease) {
        return getPalette(protocol, netease).getAllEntityIdentifiers();
    }

    public static List<String> getAllPropertyNames(AbstractProtocol protocol, boolean netease, String entityIdentifier) {
        return getPalette(protocol, netease).getAllPropertyNames(entityIdentifier);
    }

    public static Optional<EntityPropertyData> getProperty(AbstractProtocol protocol, boolean netease, String entityIdentifier, String propertyName) {
        return getPalette(protocol, netease).getProperty(entityIdentifier, propertyName);
    }

    public static Map<String, byte[]> getCompiledPalette(AbstractProtocol protocol, boolean netease) {
        return getPalette(protocol, netease).getCompiledPalette();
    }

    /**
     * @return batch packet
     */
    @Nullable
    public static DataPacket getPacket(AbstractProtocol protocol) {
        return PACKETS.get(protocol);
    }

    public static void init() {
    }

    private EntityPropertiesPalette() {
        throw new IllegalStateException();
    }
}
