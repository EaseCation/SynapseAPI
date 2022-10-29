package org.itxtech.synapseapi.multiprotocol.utils;

import cn.nukkit.Server;
import cn.nukkit.inventory.*;
import cn.nukkit.network.protocol.BatchPacket;
import cn.nukkit.network.protocol.CraftingDataPacket;
import cn.nukkit.network.protocol.DataPacket;
import cn.nukkit.utils.MainLogger;
import lombok.extern.log4j.Log4j2;
import org.itxtech.synapseapi.multiprotocol.AbstractProtocol;
import org.itxtech.synapseapi.multiprotocol.PacketRegister;

import java.util.EnumMap;
import java.util.Map;
import java.util.zip.Deflater;

@Log4j2
public final class CraftingPacketManager {

    private static BatchPacket originPacket;
    private static final Map<AbstractProtocol, BatchPacket[]> packets = new EnumMap<>(AbstractProtocol.class);

    public static void rebuildPacket() {
        log.debug("Loading crafting data...");

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

        pk.tryEncode();
        originPacket = pk.compress(Deflater.BEST_COMPRESSION);

        for (AbstractProtocol protocol : AbstractProtocol.values()) {
            if (protocol.ordinal() >= AbstractProtocol.PROTOCOL_14.ordinal()) {
                DataPacket pk0 = PacketRegister.getCompatiblePacket(pk, protocol, false);
                if (pk0 != null) {
                    pk0.setHelper(protocol.getHelper());
                    pk0.tryEncode();
                    if (protocol.ordinal() >= AbstractProtocol.PROTOCOL_116.ordinal()) {
                        pk0 = pk0.compress(Deflater.BEST_COMPRESSION, true);
                    } else {
                        pk0 = pk0.compress(Deflater.BEST_COMPRESSION);
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
                        pkNE = pkNE.compress(Deflater.BEST_COMPRESSION, true);
                    } else {
                        pkNE = pkNE.compress(Deflater.BEST_COMPRESSION);
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
        return packets.getOrDefault(protocol, new BatchPacket[]{originPacket, originPacket})[netease ? 1 : 0];
    }

}
