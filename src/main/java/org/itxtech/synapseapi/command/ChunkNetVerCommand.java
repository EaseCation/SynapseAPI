package org.itxtech.synapseapi.command;

import cn.nukkit.command.Command;
import cn.nukkit.command.CommandSender;
import cn.nukkit.command.PluginIdentifiableCommand;
import cn.nukkit.command.data.CommandFlag;
import cn.nukkit.level.Level;
import cn.nukkit.level.format.generic.ChunkRequestTask;
import cn.nukkit.plugin.Plugin;

public class ChunkNetVerCommand extends Command implements PluginIdentifiableCommand {
    private final Plugin plugin;

    public ChunkNetVerCommand(Plugin plugin) {
        super("chunknetv", "Inspect chunk network versions", "/chunknetv");
        setPermission("synapseapi.command.chunknetv");
        commandData.flags.add(CommandFlag.TEST);
        commandData.flags.add(CommandFlag.HIDDEN_FROM_COMMAND_BLOCK_ORIGIN);
        commandData.flags.add(CommandFlag.NOT_CHEAT);
        commandParameters.clear();
        this.plugin = plugin;
    }

    @Override
    public boolean execute(CommandSender sender, String commandLabel, String[] args) {
        if (!testPermission(sender)) {
            return false;
        }

        StringBuilder result = new StringBuilder("Global Preloaded: " + ChunkRequestTask.getPreloadVersions());
        for (Level level : sender.getServer().getLevels().values()) {
            result.append('\n');
            result.append(level.getFolderName());
            result.append(": ");
            result.append(level.getRequestChunkVersions().keySet());
        }
        sender.sendMessage(result.toString());
        return true;
    }

    @Override
    public Plugin getPlugin() {
        return plugin;
    }
}
