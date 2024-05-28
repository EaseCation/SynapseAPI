package org.itxtech.synapseapi.command;

import cn.nukkit.command.Command;
import cn.nukkit.command.CommandParser;
import cn.nukkit.command.CommandSender;
import cn.nukkit.command.PluginIdentifiableCommand;
import cn.nukkit.command.data.CommandFlag;
import cn.nukkit.command.data.CommandParamType;
import cn.nukkit.command.data.CommandParameter;
import cn.nukkit.command.exceptions.CommandSyntaxException;
import cn.nukkit.network.protocol.types.CommandOriginData.Origin;
import cn.nukkit.plugin.Plugin;
import org.itxtech.synapseapi.utils.CommunityTool;

public class CommunityToolCommand extends Command implements PluginIdentifiableCommand {
    private final Plugin plugin;

    public CommunityToolCommand(Plugin plugin) {
        super("communitytool", "Export community data", "/communitytool <protocol: int>");
        setPermission("synapseapi.command.communitytool");
        commandData.flags.add(CommandFlag.TEST);
        commandData.flags.add(CommandFlag.HIDDEN_FROM_COMMAND_BLOCK_ORIGIN);
        commandData.flags.add(CommandFlag.NOT_CHEAT);
        commandParameters.clear();
        commandParameters.put("default", new CommandParameter[]{
                CommandParameter.newType("protocol", CommandParamType.INT),
        });
        this.plugin = plugin;
    }

    @Override
    public boolean execute(CommandSender sender, String commandLabel, String[] args) {
        if (!testPermission(sender)) {
            return false;
        }
        if (sender.getCommandOrigin() != Origin.DEDICATED_SERVER) {
            return false;
        }

        CommandParser parser = new CommandParser(this, sender, args);
        try {
            int protocol = parser.parseInt();

            plugin.getLogger().info("exporting...");
            try {
                CommunityTool.remapResourcesToNetease(plugin.getDataFolder().toPath().resolve("community"), protocol);
                plugin.getLogger().info("community data exported");
            } catch (Exception e) {
                plugin.getLogger().error("Failed to export community data", e);
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
}
