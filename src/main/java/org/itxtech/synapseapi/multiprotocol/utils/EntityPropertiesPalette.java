package org.itxtech.synapseapi.multiprotocol.utils;

import org.itxtech.synapseapi.multiprotocol.AbstractProtocol;
import org.itxtech.synapseapi.multiprotocol.utils.entityproperty.EntityPropertiesPaletteBase;
import org.itxtech.synapseapi.multiprotocol.utils.entityproperty.EntityPropertiesPaletteLegacy;
import org.itxtech.synapseapi.multiprotocol.utils.entityproperty.data.EntityPropertiesTable;
import org.itxtech.synapseapi.multiprotocol.utils.entityproperty.data.EntityPropertyData;
import org.itxtech.synapseapi.multiprotocol.utils.entityproperty.data.EntityPropertyDataBool;
import org.itxtech.synapseapi.multiprotocol.utils.entityproperty.data.EntityPropertyDataEnum;

import java.util.*;

public final class EntityPropertiesPalette {

    public static final Map<AbstractProtocol, EntityPropertiesPaletteInterface[]> palettes = new EnumMap<>(AbstractProtocol.class);

    static {
        EntityPropertiesPaletteInterface palette117 = new EntityPropertiesPaletteBase();
        EntityPropertiesPaletteInterface palette11970 = new EntityPropertiesPaletteBase(
            new EntityPropertiesTable("minecraft:bee",
                new EntityPropertyDataBool("minecraft:has_nectar")
            )
        );
        EntityPropertiesPaletteInterface palette12080 = new EntityPropertiesPaletteBase(
            new EntityPropertiesTable("minecraft:bee",
                new EntityPropertyDataBool("minecraft:has_nectar")
            ),
            new EntityPropertiesTable("minecraft:armadillo",
                new EntityPropertyDataEnum("minecraft:armadillo_state",
                    "unrolled",
                    "rolled_up",
                    "rolled_up_peeking",
                    "rolled_up_relaxing",
                    "rolled_up_unrolling"
                )
            )
        );

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
    }

    private static void register(AbstractProtocol protocol, EntityPropertiesPaletteInterface palette, EntityPropertiesPaletteInterface paletteNetEase) {
        Objects.requireNonNull(palette);
        EntityPropertiesPaletteInterface[] data = paletteNetEase != null
            ? new EntityPropertiesPaletteInterface[]{palette, paletteNetEase}
            : new EntityPropertiesPaletteInterface[]{palette};
        palettes.put(protocol, data);
    }

    public static void registerCustomProperties(EntityPropertiesTable properties) {
        for (EntityPropertiesPaletteInterface[] versions : palettes.values()) {
            for (EntityPropertiesPaletteInterface data : versions) {
                if (data == null) {
                    continue;
                }
                data.registerProperties(properties);
            }
        }
    }

    public static void rebuildNetworkCache() {
        for (EntityPropertiesPaletteInterface[] versions : palettes.values()) {
            for (EntityPropertiesPaletteInterface data : versions) {
                if (data == null) {
                    continue;
                }
                data.rebuildNetworkCache();
            }
        }
    }

    public static EntityPropertiesPaletteInterface getPalette(AbstractProtocol protocol, boolean netease) {
        EntityPropertiesPaletteInterface[] interfaces = palettes.get(protocol);
        if (interfaces == null) {
            return EntityPropertiesPaletteLegacy.INSTANCE;
        }
        if (netease && interfaces.length > 1) {
            return interfaces[1];
        } else {
            return interfaces[0];
        }
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

    public static void init() {
    }

    private EntityPropertiesPalette() {
        throw new IllegalStateException();
    }
}
