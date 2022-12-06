package org.itxtech.synapseapi.multiprotocol.utils;

import cn.nukkit.item.Item;

public interface AdvancedRuntimeItemPaletteInterface {

    int getNetworkFullId(Item item);

    int getLegacyFullId(int networkId);

    int getId(int fullId);

    int getData(int fullId);

    int getNetworkId(int networkFullId);

    String getString(Item item);

    int getLegacyFullIdByName(String name);

    boolean hasData(int id);

    byte[] getCompiledData();
}
