package org.itxtech.synapseapi.command;

import cn.nukkit.block.Block;
import cn.nukkit.command.Command;
import cn.nukkit.command.CommandParser;
import cn.nukkit.command.CommandSender;
import cn.nukkit.command.PluginIdentifiableCommand;
import cn.nukkit.command.data.CommandEnum;
import cn.nukkit.command.data.CommandFlag;
import cn.nukkit.command.data.CommandParamType;
import cn.nukkit.command.data.CommandParameter;
import cn.nukkit.command.exceptions.CommandSyntaxException;
import cn.nukkit.item.Item;
import cn.nukkit.item.ItemDurable;
import cn.nukkit.plugin.Plugin;
import org.itxtech.synapseapi.multiprotocol.AbstractProtocol;
import org.itxtech.synapseapi.multiprotocol.utils.AdvancedGlobalBlockPalette;
import org.itxtech.synapseapi.multiprotocol.utils.AdvancedRuntimeItemPalette;
import org.itxtech.synapseapi.multiprotocol.utils.item.BlockItemFlattener;

public class NetDataCommand extends Command implements PluginIdentifiableCommand {
    private final Plugin plugin;

    public NetDataCommand(Plugin plugin) {
        super("netdataquery", "Query network data value", "/netdataquery <type: DataType> <protocol: int> <netease: Boolean> <value: int> [auxValue: int]", "ndq");
        setPermission("synapseapi.command.netdataquery");
        commandData.flags.add(CommandFlag.TEST);
        commandData.flags.add(CommandFlag.HIDDEN_FROM_COMMAND_BLOCK_ORIGIN);
        commandData.flags.add(CommandFlag.NOT_CHEAT);
        commandParameters.clear();
        this.commandParameters.put("default", new CommandParameter[]{
                CommandParameter.newEnum("type", new CommandEnum("DataType", DataType.values())),
                CommandParameter.newType("protocol", CommandParamType.INT),
                CommandParameter.newEnum("netease", CommandEnum.ENUM_BOOLEAN),
                CommandParameter.newType("value", CommandParamType.INT),
                CommandParameter.newType("auxValue", true, CommandParamType.INT),
        });
        this.plugin = plugin;
    }

    @Override
    public boolean execute(CommandSender sender, String commandLabel, String[] args) {
        if (!testPermission(sender)) {
            return false;
        }

        CommandParser parser = new CommandParser(this, sender, args);
        try {
            DataType type = parser.parseEnum(DataType.class);
            int protocol = parser.parseInt(0, AbstractProtocol.LAST_ALLOW_LOGIN_PROTOCOL.getProtocolStart());
            boolean netease = parser.parseBoolean();
            int value = parser.parseInt();
            int auxValue = parser.parseIntOrDefault(0);

            AbstractProtocol version = AbstractProtocol.fromRealProtocol(protocol);

            switch (type) {
                case BLOCK_NETWORK_DATA -> {
                    int runtimeId = AdvancedGlobalBlockPalette.getRuntimeId(version, netease, value, auxValue);
                    if (runtimeId == Integer.MIN_VALUE) {
                        sender.sendMessage("Block ID not found");
                    } else if (runtimeId < 0) {
                        sender.sendMessage("Block runtime ID: " + -runtimeId + " (meta invalid)");
                    } else {
                        sender.sendMessage("Block runtime ID: " + runtimeId);
                    }
                }
                case BLOCK_NUKKIT_DATA -> {
                    int fullId = AdvancedGlobalBlockPalette.getLegacyId(version, netease, value);
                    if (fullId == -1) {
                        sender.sendMessage("Block runtime ID not found");
                        return true;
                    }
                    int id = Block.getIdFromFullId(fullId);
                    int meta = Block.getDamageFromFullId(fullId);
                    sender.sendMessage("Block ID: " + id + " meta: " + meta);
                }
                case ITEM_NETWORK_DATA -> {
                    Item item = Item.get(value, auxValue);
                    if (item.getClass() == Item.class) {
                        sender.sendMessage("Item ID not found");
                        return true;
                    }
                    boolean isBlock = value < 256 && value != 166;

                    int networkFullId;
                    try {
                        networkFullId = AdvancedRuntimeItemPalette.getNetworkFullId(version, netease, item);
                    } catch (IllegalArgumentException e) {
                        sender.sendMessage(e.getMessage());
                        return true;
                    }
                    int runtimeId = AdvancedRuntimeItemPalette.getNetworkId(version, netease, networkFullId);
                    if (version.getProtocolStart() >= AbstractProtocol.PROTOCOL_119_70.getProtocolStart()) {
                        if (value > 0 && runtimeId < 0) { //TODO: flat upgrade mapping, e.g. "minecraft:wool"
                            runtimeId >>= 1;
                        }
                    }

                    int runtimeAux = !isBlock && !(item instanceof ItemDurable) && !AdvancedRuntimeItemPalette.hasData(version, netease, networkFullId) ? item.getDamage() : 0;

                    Block block = isBlock ? item.getBlockUnsafe() : null;
                    if (block != null) {
                        int blockRuntimeId = AdvancedGlobalBlockPalette.getOrCreateRuntimeId(version, netease, block.getId(), block.getDamage());
                        sender.sendMessage("Item runtime ID: " + runtimeId + " auxVal: " + runtimeAux + " blockRuntimeId: " + blockRuntimeId);
                    } else {
                        sender.sendMessage("Item runtime ID: " + runtimeId + " auxVal: " + runtimeAux);
                    }
                }
                case ITEM_NUKKIT_DATA -> {
                    int networkFullId;
                    try {
                        networkFullId = AdvancedRuntimeItemPalette.getLegacyFullId(version, netease, value);
                    } catch (IllegalArgumentException e) {
                        sender.sendMessage(e.getMessage());
                        return true;
                    }
                    int flattenedId = AdvancedRuntimeItemPalette.getId(version, netease, networkFullId);
                    int id = BlockItemFlattener.downgrade(version, flattenedId);

                    if (AdvancedRuntimeItemPalette.hasData(version, netease, networkFullId)) {
                        if (id != flattenedId) {
                            sender.sendMessage("Item ID: " + id + " meta: " + AdvancedRuntimeItemPalette.getData(version, netease, networkFullId) + " (flattenedId: " + flattenedId + ")");
                        } else {
                            sender.sendMessage("Item ID: " + id + " meta: " + AdvancedRuntimeItemPalette.getData(version, netease, networkFullId));
                        }
                    } else if (id != flattenedId) {
                        sender.sendMessage("Item ID: " + id + " (flattenedId: " + flattenedId + ")");
                    } else {
                        sender.sendMessage("Item ID: " + id);
                    }
                }
            }
        } catch (CommandSyntaxException e) {
            sender.sendMessage(parser.getErrorMessage());
        }
        return true;
    }

    @Override
    public Plugin getPlugin() {
        return plugin;
    }

    public enum DataType {
        BLOCK_NETWORK_DATA,
        BLOCK_NUKKIT_DATA,
        ITEM_NETWORK_DATA,
        ITEM_NUKKIT_DATA,
    }
}
