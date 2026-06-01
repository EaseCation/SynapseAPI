package org.itxtech.synapseapi.command;

import cn.nukkit.Player;
import cn.nukkit.command.Command;
import cn.nukkit.command.CommandParser;
import cn.nukkit.command.CommandSender;
import cn.nukkit.command.PluginIdentifiableCommand;
import cn.nukkit.command.data.CommandParamType;
import cn.nukkit.command.data.CommandParameter;
import cn.nukkit.command.exceptions.CommandSyntaxException;
import cn.nukkit.plugin.Plugin;
import org.itxtech.synapseapi.SynapsePlayer;

import java.util.List;

public class FastTransferCommand extends Command implements PluginIdentifiableCommand {
    private final Plugin plugin;

    public FastTransferCommand(Plugin plugin) {
        super("fasttransfer", "Fast transfer", "/fasttransfer <player: target> <descriptor: string>");
        setPermission("synapseapi.command.fasttransfer");
        commandParameters.clear();
        commandParameters.put("default", new CommandParameter[]{
                CommandParameter.newType("player", CommandParamType.TARGET),
                CommandParameter.newType("descriptor", CommandParamType.STRING),
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
            List<Player> players = parser.parseTargetPlayers();
            String descriptor = parser.literal();

            for (Player player : players) {
                if (player instanceof SynapsePlayer synapsePlayer) {
                    synapsePlayer.transferByDescription(descriptor);
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
}
