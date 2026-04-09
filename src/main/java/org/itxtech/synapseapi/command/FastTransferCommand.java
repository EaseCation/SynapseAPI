package org.itxtech.synapseapi.command;

import cn.nukkit.command.Command;
import cn.nukkit.command.CommandParser;
import cn.nukkit.command.CommandSender;
import cn.nukkit.command.PluginIdentifiableCommand;
import cn.nukkit.command.data.CommandParamType;
import cn.nukkit.command.data.CommandParameter;
import cn.nukkit.command.exceptions.CommandSyntaxException;
import cn.nukkit.lang.TranslationContainer;
import cn.nukkit.plugin.Plugin;
import org.itxtech.synapseapi.SynapsePlayer;

public class FastTransferCommand extends Command implements PluginIdentifiableCommand {
    private final Plugin plugin;

    public FastTransferCommand(Plugin plugin) {
        super("fasttransfer", "Fast transfer", "/fasttransfer <descriptor: string>");
        setPermission("synapseapi.command.fasttransfer");
        commandParameters.clear();
        commandParameters.put("default", new CommandParameter[]{
                CommandParameter.newType("descriptor", CommandParamType.STRING),
        });
        this.plugin = plugin;
    }

    @Override
    public boolean execute(CommandSender sender, String commandLabel, String[] args) {
        if (!testPermission(sender)) {
            return false;
        }

        if (!(sender instanceof SynapsePlayer player)) {
            sender.sendMessage(new TranslationContainer("nukkit.command.generic.ingame"));
            return false;
        }

        CommandParser parser = new CommandParser(this, sender, args);
        try {
            String descriptor = parser.literal();

            player.transferByDescription(descriptor);
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
