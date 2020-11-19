package org.itxtech.synapseapi.multiprotocol.utils;

import cn.nukkit.item.Item;

public class RuntimeItemPaletteLegacy implements AdvancedRuntimeItemPaletteInterface {

    public static final RuntimeItemPaletteLegacy INSTANCE = new RuntimeItemPaletteLegacy();

    private RuntimeItemPaletteLegacy() {
        // NOOP
    }

    @Override
    public int getNetworkFullId(Item item) {
        return item.getId();
    }

    @Override
    public int getLegacyFullId(int networkId) {
        return networkId;
    }

    @Override
    public int getId(int fullId) {
        return fullId;
    }

    @Override
    public int getData(int fullId) {
        return 0;
    }

    @Override
    public int getNetworkId(int networkFullId) {
        return networkFullId;
    }

    @Override
    public boolean hasData(int id) {
        return false;
    }

    @Override
    public byte[] getCompiledData() throws UnsupportedOperationException {
        throw new UnsupportedOperationException();
    }
}
