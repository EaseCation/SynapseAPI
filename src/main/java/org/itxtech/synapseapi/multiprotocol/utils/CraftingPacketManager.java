package org.itxtech.synapseapi.multiprotocol.utils;

import cn.nukkit.Server;
import cn.nukkit.inventory.*;
import cn.nukkit.network.protocol.BatchPacket;
import cn.nukkit.network.protocol.CraftingDataPacket;
import cn.nukkit.network.protocol.DataPacket;
import org.itxtech.synapseapi.multiprotocol.AbstractProtocol;
import org.itxtech.synapseapi.multiprotocol.PacketRegister;

import java.util.HashMap;
import java.util.Map;
import java.util.zip.Deflater;

public final class CraftingPacketManager {

    private static BatchPacket originPacket;
    private static Map<AbstractProtocol, BatchPacket> packets = new HashMap<>();

    public static void rebuildPacket() {
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

        for (BrewingRecipe recipe : Server.getInstance().getCraftingManager().getBrewingRecipes().values()) {
            pk.addBrewingRecipe(recipe);
        }

        for (ContainerRecipe recipe : Server.getInstance().getCraftingManager().getContainerRecipes().values()) {
            pk.addContainerRecipe(recipe);
        }

        pk.tryEncode();
        originPacket = pk.compress(Deflater.BEST_COMPRESSION);

        for (AbstractProtocol protocol : AbstractProtocol.values()) {
            if (protocol.ordinal() >= AbstractProtocol.PROTOCOL_14.ordinal()) {
                DataPacket pk0 = PacketRegister.getCompatiblePacket(pk, protocol, false);
                if (pk0 != null) {
                    pk0.setHelper(protocol.getHelper());
                    pk0.tryEncode();
                    if (protocol.ordinal() >= AbstractProtocol.PROTOCOL_116.ordinal())
                        packets.put(protocol, pk0.compress(Deflater.BEST_COMPRESSION, true));
                    else packets.put(protocol, pk0.compress(Deflater.BEST_COMPRESSION));
                }
            }
        }
    }

    public static BatchPacket getCachedCraftingPacket(AbstractProtocol protocol) {
        return packets.getOrDefault(protocol, originPacket);
    }

}
