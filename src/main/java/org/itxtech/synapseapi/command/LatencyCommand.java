package org.itxtech.synapseapi.command;

import cn.nukkit.command.Command;
import cn.nukkit.command.CommandSender;
import cn.nukkit.command.PluginIdentifiableCommand;
import cn.nukkit.lang.TranslationContainer;
import cn.nukkit.plugin.Plugin;
import org.itxtech.synapseapi.SynapsePlayer;

/**
 * 用于没有ModUI的环境.
 */
public class LatencyCommand extends Command implements PluginIdentifiableCommand {
    private final Plugin plugin;

    public LatencyCommand(Plugin plugin) {
        super("latency", "RTT telemetry", "/latency");
        setPermission("synapseapi.command.latency");
        commandParameters.clear();
        this.plugin = plugin;
    }

    @Override
    public boolean execute(CommandSender sender, String commandLabel, String[] args) {
        if (!testPermission(sender)) {
            return false;
        }

        if (!(sender instanceof SynapsePlayer)) {
            sender.sendMessage(new TranslationContainer("nukkit.command.generic.ingame"));
            return false;
        }

        SynapsePlayer player = (SynapsePlayer) sender;
        int latencyMs = (int) (player.getLatency() / 1_000_000);
        int latencyTicks = latencyMs / 50;
        player.sendMessage("RTT: " + latencyTicks + " ticks (" + latencyMs + " ms)\nTPS: " + sender.getServer().getTicksPerSecond());
        return true;
    }

    @Override
    public Plugin getPlugin() {
        return plugin;
    }
}
