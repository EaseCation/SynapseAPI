package org.itxtech.synapseapi.multiprotocol.utils.item;

import cn.nukkit.item.ItemBlockID;
import it.unimi.dsi.fastutil.ints.Int2IntFunction;
import org.itxtech.synapseapi.multiprotocol.AbstractProtocol;

import java.util.EnumMap;
import java.util.Map;

public final class BlockItemFlattener {
    private static final AbstractProtocol BASE_GAME_VERSION = AbstractProtocol.PROTOCOL_118;
    private static final Map<AbstractProtocol, Int2IntFunction> DOWNGRADERS = new EnumMap<>(AbstractProtocol.class);

    public static int downgrade(AbstractProtocol protocol, int id) {
        Int2IntFunction func = DOWNGRADERS.get(protocol);
        if (func == null) {
            return id;
        }
        return func.applyAsInt(id);
    }

    static {
        DOWNGRADERS.put(AbstractProtocol.PROTOCOL_119_70, BlockItemFlattener::downgrader11970);
        DOWNGRADERS.put(AbstractProtocol.PROTOCOL_119_80, BlockItemFlattener::downgrader11980);
//        DOWNGRADERS.put(AbstractProtocol.PROTOCOL_120, BlockItemFlattener::downgrader120);
    }

    private static int downgrader11970(int id) {
        if (id <= ItemBlockID.BLACK_WOOL && id >= ItemBlockID.PINK_WOOL) {
            return ItemBlockID.WOOL;
        }
        return id;
    }

    private static int downgrader11980(int id) {
        if (id <= ItemBlockID.SPRUCE_LOG && id >= ItemBlockID.JUNGLE_LOG) {
            return ItemBlockID.LOG;
        }
        if (id == ItemBlockID.DARK_OAK_LOG) {
            return ItemBlockID.LOG2;
        }
        if (id <= ItemBlockID.ACACIA_FENCE && id >= ItemBlockID.SPRUCE_FENCE) {
            return ItemBlockID.FENCE;
        }
        return downgrader11970(id);
    }

    private static int downgrader120(int id) {
        if (id <= ItemBlockID.BRAIN_CORAL && id >= ItemBlockID.DEAD_HORN_CORAL) {
            return ItemBlockID.CORAL;
        }
        if (id <= ItemBlockID.ORANGE_CARPET && id >= ItemBlockID.BLACK_CARPET) {
            return ItemBlockID.CARPET;
        }
        return downgrader11980(id);
    }

    private BlockItemFlattener() {
        throw new IllegalStateException();
    }
}
