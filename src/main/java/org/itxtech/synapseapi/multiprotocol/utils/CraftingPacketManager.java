package org.itxtech.synapseapi.multiprotocol.utils;

import cn.nukkit.Server;
import cn.nukkit.inventory.*;
import cn.nukkit.network.protocol.BatchPacket;
import cn.nukkit.network.protocol.BatchPacket.Track;
import cn.nukkit.network.protocol.CraftingDataPacket;
import cn.nukkit.network.protocol.DataPacket;
import cn.nukkit.utils.MainLogger;
import lombok.extern.log4j.Log4j2;
import org.itxtech.synapseapi.multiprotocol.AbstractProtocol;
import org.itxtech.synapseapi.multiprotocol.PacketRegister;
import org.itxtech.synapseapi.multiprotocol.protocol16.protocol.CompatibilityPacket16;

import java.util.EnumMap;
import java.util.Map;
import java.util.zip.Deflater;

@Log4j2
public final class CraftingPacketManager {

    private static final BatchPacket[] originPacket = new BatchPacket[2];
    private static final Map<AbstractProtocol, BatchPacket[]> packets = new EnumMap<>(AbstractProtocol.class);

    public static void rebuildPacket() {
        log.debug("cache crafting data...");

        CraftingDataPacket pk = new CraftingDataPacket();
        pk.cleanRecipes = true;

        for (Recipe recipe : Server.getInstance().getCraftingManager().getRecipes()) {
            if (recipe instanceof ShapedRecipe) {
                pk.addShapedRecipe((ShapedRecipe) recipe);
            } else if (recipe instanceof ShapelessRecipe) {
                pk.addShapelessRecipe((ShapelessRecipe) recipe);
            }
        }

        for (FurnaceRecipe recipe : Server.getInstance().getCraftingManager().getFurnaceRecipes().values()) {
            pk.addFurnaceRecipe(recipe);
        }

        for (MultiRecipe recipe : Server.getInstance().getCraftingManager().getMultiRecipes().values()) {
            pk.addMultiRecipe(recipe);
        }

        for (BrewingRecipe recipe : Server.getInstance().getCraftingManager().getBrewingRecipes().values()) {
            pk.addBrewingRecipe(recipe);
        }

        for (ContainerRecipe recipe : Server.getInstance().getCraftingManager().getContainerRecipes().values()) {
            pk.addContainerRecipe(recipe);
        }

        for (MaterialReducerRecipe recipe : Server.getInstance().getCraftingManager().getMaterialReducerRecipes().values()) {
            pk.addMaterialReducerRecipe(recipe);
        }

        for (SmithingTransformRecipe recipe : Server.getInstance().getCraftingManager().getSmithingRecipes().values()) {
            pk.addSmithingTransformRecipe(recipe);
        }

        for (SmithingTrimRecipe recipe : Server.getInstance().getCraftingManager().getSmithingTrimRecipes().values()) {
            pk.addSmithingTrimRecipe(recipe);
        }

        pk.tryEncode();
        BatchPacket origin = pk.compress(Deflater.BEST_COMPRESSION);
        originPacket[0] = origin;
        originPacket[1] = origin;

        for (AbstractProtocol protocol : AbstractProtocol.getValues()) {
            if (protocol.ordinal() < AbstractProtocol.FIRST_AVAILABLE_PROTOCOL.ordinal()) {
                // drop support for unavailable versions
                continue;
            }

            if (protocol.ordinal() >= AbstractProtocol.PROTOCOL_14.ordinal()) {
                DataPacket pk0 = PacketRegister.getCompatiblePacket(pk, protocol, false);
                if (pk0 != null) {
                    pk0.setHelper(protocol.getHelper());
                    pk0.tryEncode();
                    if (protocol.ordinal() >= AbstractProtocol.PROTOCOL_116.ordinal()) {
                        BatchPacket batch = pk0.compress(Deflater.BEST_COMPRESSION, true);
                        batch.tracks = new Track[]{new Track(pk0 instanceof CompatibilityPacket16 ? ((CompatibilityPacket16) pk0).origin.pid() : pk0.pid(), pk0.getCount())};
                        pk0 = batch;
                    } else {
                        BatchPacket batch = pk0.compress(Deflater.BEST_COMPRESSION);
                        batch.tracks = new Track[]{new Track(pk0 instanceof CompatibilityPacket16 ? ((CompatibilityPacket16) pk0).origin.pid() : pk0.pid(), pk0.getCount())};
                        pk0 = batch;
                    }
                } else {
                    MainLogger.getLogger().warning("CraftingDataPacket for version " + protocol.name() + " with null compatible packet!");
                }
                DataPacket pkNE = PacketRegister.getCompatiblePacket(pk, protocol, true);
                if (pkNE != null) {
                    pkNE.setHelper(protocol.getHelper());
                    pkNE.neteaseMode = true;
                    pkNE.tryEncode();
                    if (protocol.ordinal() >= AbstractProtocol.PROTOCOL_116.ordinal()) {
                        BatchPacket batch = pkNE.compress(Deflater.BEST_COMPRESSION, true);
                        batch.tracks = new Track[]{new Track(pkNE instanceof CompatibilityPacket16 ? ((CompatibilityPacket16) pkNE).origin.pid() : pkNE.pid(), pkNE.getCount())};
                        pkNE = batch;
                    } else {
                        BatchPacket batch = pkNE.compress(Deflater.BEST_COMPRESSION);
                        batch.tracks = new Track[]{new Track(pkNE instanceof CompatibilityPacket16 ? ((CompatibilityPacket16) pkNE).origin.pid() : pkNE.pid(), pkNE.getCount())};
                        pkNE = batch;
                    }
                } else {
                    MainLogger.getLogger().warning("CraftingDataPacket for version " + protocol.name() + "(NetEase) with null compatible packet!");
                }
                MainLogger.getLogger().debug("Registering CraftingDataPacket for version " + protocol.name());
                packets.put(protocol, new BatchPacket[]{(BatchPacket) pk0, (BatchPacket) pkNE});
            }
        }
    }

    public static BatchPacket getCachedCraftingPacket(AbstractProtocol protocol, boolean netease) {
        return packets.getOrDefault(protocol, originPacket)[netease ? 1 : 0];
    }

}
