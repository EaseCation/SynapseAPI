package org.itxtech.synapseapi.multiprotocol.utils;

import cn.nukkit.item.Item;
import cn.nukkit.item.RuntimeItemPaletteInterface.Entry;
import cn.nukkit.nbt.tag.CompoundTag;
import cn.nukkit.network.protocol.DataPacket;

import java.util.Map;

public interface AdvancedRuntimeItemPaletteInterface {

    int getNetworkFullId(Item item);

    int getLegacyFullId(int networkId);

    int getId(int fullId);

    int getData(int fullId);

    int getNetworkId(int networkFullId);

    String getString(Item item);

    int getLegacyFullIdByName(String name);

    int getNetworkIdByName(String name);

    int getNetworkIdByNameTodo(String name);

    boolean hasData(int id);

    byte[] getCompiledData();

    void buildNetworkCache();

    DataPacket createItemRegistryPacket(Map<String, CompoundTag> componentDefinitions);

    void registerItem(Entry entry);
}
