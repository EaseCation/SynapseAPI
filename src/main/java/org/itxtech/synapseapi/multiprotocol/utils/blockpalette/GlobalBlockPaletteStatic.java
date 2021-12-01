package org.itxtech.synapseapi.multiprotocol.utils.blockpalette;

import cn.nukkit.level.GlobalBlockPaletteInterface;
import org.itxtech.synapseapi.multiprotocol.utils.AdvancedGlobalBlockPaletteInterface;

import java.util.NoSuchElementException;

public class GlobalBlockPaletteStatic implements GlobalBlockPaletteInterface {

    private final AdvancedGlobalBlockPaletteInterface palette;

    public GlobalBlockPaletteStatic(AdvancedGlobalBlockPaletteInterface palette) {
        this.palette = palette;
    }

    @Override
    public int getOrCreateRuntimeId0(int id, int meta) {
        return this.palette.getOrCreateRuntimeId(id, meta);
    }

    @Override
    public int getOrCreateRuntimeId0(int legacyId) throws NoSuchElementException {
        return this.palette.getOrCreateRuntimeId(legacyId);
    }

    @Override
    public int getLegacyId0(int runtimeId) {
        return this.palette.getLegacyId(runtimeId);
    }
}
